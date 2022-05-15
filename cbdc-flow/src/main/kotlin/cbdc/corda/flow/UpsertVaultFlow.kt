package cbdc.corda.flow

import cbdc.corda.contract.VaultContract
import cbdc.corda.schema.CBDCSchemaV1
import cbdc.corda.state.CENTRAL_BANK_VAULT_ID
import cbdc.corda.state.CertInfoK
import cbdc.corda.state.VaultState
import cbdc.corda.state.VaultType
import co.paralleluniverse.fibers.Suspendable
import net.corda.core.contracts.UniqueIdentifier
import net.corda.core.flows.StartableByRPC
import net.corda.core.node.services.vault.QueryCriteria
import net.corda.core.node.services.vault.builder
import net.corda.core.serialization.CordaSerializable
import net.corda.core.transactions.SignedTransaction
import net.corda.core.transactions.TransactionBuilder
import java.time.Instant

abstract class UpsertVaultFlow(
    protected val id: String
) : BaseTxFlow() {

    abstract fun latestVaultState(oldState: VaultState?): VaultState?

    protected val now
        get() = preferredTimeWindow.midpoint!!

    @Suspendable
    override fun call(): SignedTransaction {
        val timeWindow = preferredTimeWindow

        val existingVaultState = serviceHub.vaultService.queryBy(
            contractStateType = VaultState::class.java,
            criteria = QueryCriteria.VaultCustomQueryCriteria(
                expression = builder { CBDCSchemaV1.PersistentVault::id.equal(id) }
            )
        ).states.singleOrNull()

        val vaultState = latestVaultState(existingVaultState?.state?.data)

        val tx = TransactionBuilder(notary).setTimeWindow(timeWindow)

        if (vaultState != null && existingVaultState != null) {
            tx.addInputState(existingVaultState)
            tx.addOutputState(vaultState)
            tx.addCommand(VaultContract.Commands.Update, serviceHub.myInfo.legalIdentities.first().owningKey)
        } else if (vaultState != null && existingVaultState == null) {
            tx.addOutputState(vaultState)
            tx.addCommand(VaultContract.Commands.Create, serviceHub.myInfo.legalIdentities.first().owningKey)
        } else if (vaultState == null && existingVaultState != null) {
            tx.addInputState(existingVaultState)
            tx.addCommand(VaultContract.Commands.Remove, serviceHub.myInfo.legalIdentities.first().owningKey)
        }

        // Sign the transaction
        val ptx = serviceHub.signInitialTransaction(tx)

        // Notarise and record the transaction in all parties' vaults
        return subFlow(BroadcastFinalityFlow.Initiator(ptx))
    }

    protected fun requiresNoExistingVault(oldState: VaultState?) {
        if (oldState != null)
            throw IllegalArgumentException("Vault already exists: $id")
    }

    protected fun requiresExistingVault(oldState: VaultState?) {
        if (oldState == null)
            throw IllegalArgumentException("Vault does not exist: $id")
    }
}

@StartableByRPC
class InitialiseCBVaultFlow(private val new: NewVaultK) : UpsertVaultFlow(CENTRAL_BANK_VAULT_ID) {
    override fun latestVaultState(oldState: VaultState?): VaultState {
        requiresNoExistingVault(oldState)
        return new.toVaultState(UniqueIdentifier(), CENTRAL_BANK_VAULT_ID, now, VaultType.CENTRAL_BANK)
    }
}

@StartableByRPC
class InitialiseBankVaultFlow(
    id: String,
    private val new: NewVaultK
) : UpsertVaultFlow(id) {
    override fun latestVaultState(oldState: VaultState?): VaultState {
        requiresNoExistingVault(oldState)
        return new.toVaultState(UniqueIdentifier(), id, now, VaultType.COMMERCIAL_BANK)
    }
}

@StartableByRPC
class FreezeVaultFlow(id: String) : UpsertVaultFlow(id) {
    override fun latestVaultState(oldState: VaultState?): VaultState? {
        requiresExistingVault(oldState)
        return oldState!!.updateVault(now, operation = VaultOperations.FREEZE)
    }
}

@StartableByRPC
class UnfreezeVaultFlow(id: String) : UpsertVaultFlow(id) {
    override fun latestVaultState(oldState: VaultState?): VaultState? {
        requiresExistingVault(oldState)
        return oldState!!.updateVault(now, operation = VaultOperations.UNFREEZE)
    }
}

@StartableByRPC
class DisableVaultFlow(id: String) : UpsertVaultFlow(id) {
    override fun latestVaultState(oldState: VaultState?): VaultState? {
        requiresExistingVault(oldState)
        return oldState!!.updateVault(now, operation = VaultOperations.DISABLE)
    }
}

@StartableByRPC
class UpdateAgencyCertFlow(
    id: String,
    private val certInfo: CertInfoK
) : UpsertVaultFlow(id) {
    override fun latestVaultState(oldState: VaultState?): VaultState {
        requiresExistingVault(oldState)
        return oldState!!.updateVault(now, agencyCertInfo = certInfo)
    }
}

@StartableByRPC
class UpdateVaultCertFlow(
    id: String,
    private val certInfo: CertInfoK
) : UpsertVaultFlow(id) {
    override fun latestVaultState(oldState: VaultState?): VaultState {
        requiresExistingVault(oldState)
        return oldState!!.updateVault(now, vaultCertInfo = certInfo)
    }
}

@StartableByRPC
class RemoveVaultFlow(id: String) : UpsertVaultFlow(id) {
    override fun latestVaultState(oldState: VaultState?): VaultState? {
        return null
    }
}

private fun VaultState.updateVault(
    now: Instant,
    operation: VaultOperations? = null,
    vaultCertInfo: CertInfoK? = null,
    agencyCertInfo: CertInfoK? = null
): VaultState {
    val frozen = when (operation) {
        VaultOperations.FREEZE -> true
        VaultOperations.UNFREEZE -> false
        else -> null
    }

    val disabled = if (operation == VaultOperations.DISABLE) true else null

    val updatedFrozen = frozen ?: this.setting.frozen
    val updatedDisabled = disabled ?: this.setting.disabled
    val updatedVaultCertInfo = vaultCertInfo ?: this.vaultCertInfo
    val updatedAgencyCertInfo = agencyCertInfo ?: this.agencyCertInfo
    val updatedSetting = this.setting.copy(
        frozen = updatedFrozen,
        disabled = updatedDisabled,
        vaultCertInfo = updatedVaultCertInfo,
        agencyCertInfo = updatedAgencyCertInfo,
        settingLastModified = now
    )

    return VaultState.new(this.status, updatedSetting, this.type)
}

@CordaSerializable
enum class VaultOperations {
    FREEZE,
    UNFREEZE,
    DISABLE
}

@CordaSerializable
data class NewVaultK(
    val vaultCertInfo: CertInfoK?,
    val agencyCertInfo: CertInfoK?
) {
    fun toVaultState(linearId: UniqueIdentifier, id: String, now: Instant, type: VaultType) = VaultState(
        linearId = linearId,
        id = id,
        type = type,

        balance = 0L,
        totalCount = 0L,
        statusLastModified = null,

        vaultCertID = vaultCertInfo?.certID,
        vaultCertPubKey = vaultCertInfo?.pubKey,
        vaultCertNotAfter = vaultCertInfo?.notAfter,
        agencyCertID = agencyCertInfo?.certID,
        agencyCertPubKey = agencyCertInfo?.pubKey,
        agencyCertNotAfter = agencyCertInfo?.notAfter,
        seedKey = null,
        frozen = false,
        disabled = false,
        createTime = now,
        settingLastModified = null
    )
}
