package cbdc.corda.flow

import cbdc.corda.contract.WalletContract
import cbdc.corda.schema.CBDCSchemaV1
import cbdc.corda.state.CertInfoK
import cbdc.corda.state.LimitK
import cbdc.corda.state.OtpDataK
import cbdc.corda.state.WalletState
import cbdc.corda.state.WalletState.Companion.defaultAnonymousBalanceLimit
import cbdc.corda.state.WalletState.Companion.defaultCertBalanceLimit
import cbdc.corda.state.WalletState.Companion.defaultCertTxnLimit
import cbdc.corda.state.WalletState.Companion.defaultPubKeyBalanceLimit
import cbdc.corda.state.WalletState.Companion.defaultPubKeyTxnLimit
import cbdc.corda.state.WalletTypeK
import co.paralleluniverse.fibers.Suspendable
import net.corda.core.contracts.UniqueIdentifier
import net.corda.core.flows.StartableByRPC
import net.corda.core.node.services.vault.QueryCriteria
import net.corda.core.node.services.vault.builder
import net.corda.core.serialization.CordaSerializable
import net.corda.core.transactions.SignedTransaction
import net.corda.core.transactions.TransactionBuilder
import java.lang.IllegalArgumentException
import java.time.Instant

@StartableByRPC
abstract class UpsertWalletFlow(
    private val id: String
) : BaseTxFlow() {

    abstract fun latestWalletState(wallet: WalletState?): WalletState?

    protected val now
        get() = preferredTimeWindow.midpoint!!

    @Suspendable
    override fun call(): SignedTransaction {
        val timeWindow = preferredTimeWindow

        val existingWalletState = serviceHub.vaultService.queryBy(
            contractStateType = WalletState::class.java,
            criteria = QueryCriteria.VaultCustomQueryCriteria(
                expression = builder { CBDCSchemaV1.PersistentWallet::id.equal(id) }
            )
        ).states.singleOrNull()

        val walletState = latestWalletState(existingWalletState?.state?.data)

        val tx = TransactionBuilder(notary).setTimeWindow(timeWindow)

        if (walletState != null && existingWalletState != null) {
            tx.addInputState(existingWalletState)
            tx.addOutputState(walletState)
            tx.addCommand(WalletContract.Commands.Update, serviceHub.myInfo.legalIdentities.first().owningKey)
        } else if (walletState != null && existingWalletState == null) {
            tx.addOutputState(walletState)
            tx.addCommand(WalletContract.Commands.Create, serviceHub.myInfo.legalIdentities.first().owningKey)
        } else if (walletState == null && existingWalletState != null) {
            tx.addInputState(existingWalletState)
            tx.addCommand(WalletContract.Commands.Remove, serviceHub.myInfo.legalIdentities.first().owningKey)
        }

        // Sign the transaction
        val ptx = serviceHub.signInitialTransaction(tx)

        // Notarise and record the transaction in all parties' vaults
        return subFlow(BroadcastFinalityFlow.Initiator(ptx))
    }
}

@StartableByRPC
class InitialiseWalletFlow(private val id: String, private val new: NewWalletK) : UpsertWalletFlow(id) {
    override fun latestWalletState(wallet: WalletState?): WalletState? {
        return new.toWalletState(UniqueIdentifier(), id, now)
    }
}

@StartableByRPC
class UpdatePubKeyFlow(private val id: String, private val pubKey: ByteArray) : UpsertWalletFlow(id) {
    override fun latestWalletState(wallet: WalletState?): WalletState {
        if (wallet == null) throw IllegalArgumentException("no existing wallet state found with id: $id")
        else return wallet.updateWallet(now, pubKey = pubKey)
    }
}

@StartableByRPC
class UpdateCertFlow(private val id: String, private val cert: ByteArray) : UpsertWalletFlow(id) {
    override fun latestWalletState(wallet: WalletState?): WalletState? {
        if (wallet == null) throw IllegalArgumentException("no existing wallet state found with id: $id")
        else return wallet.updateWallet(now, cert = cert)
    }
}

@StartableByRPC
class UpdateLimitFlow(private val id: String, private val limit: LimitK) : UpsertWalletFlow(id) {
    override fun latestWalletState(wallet: WalletState?): WalletState {
        if (wallet == null) throw IllegalArgumentException("no existing wallet state found with id: $id")
        else return wallet.updateWallet(now, limit = limit)
    }
}

@StartableByRPC
class FreezeWalletFlow(private val id: String) : UpsertWalletFlow(id) {
    override fun latestWalletState(wallet: WalletState?): WalletState {
        if (wallet == null) throw IllegalArgumentException("no existing wallet state found with id: $id")
        else return wallet.updateWallet(now, operation = WalletOperations.FREEZE)
    }
}

@StartableByRPC
class UnfreezeWalletFlow(private val id: String) : UpsertWalletFlow(id) {
    override fun latestWalletState(wallet: WalletState?): WalletState {
        if (wallet == null) throw IllegalArgumentException("no existing wallet state found with id: $id")
        else return wallet.updateWallet(now, operation = WalletOperations.UNFREEZE)
    }
}

@StartableByRPC
class UpdateDisplayNameFlow(private val id: String, private val dn: String) : UpsertWalletFlow(id) {
    override fun latestWalletState(wallet: WalletState?): WalletState {
        if (wallet == null) throw IllegalArgumentException("no existing wallet state found with id: $id")
        else return wallet.updateWallet(now, displayName = dn)
    }
}

@StartableByRPC
class UpgradeWalletFlow(private val id: String) : UpsertWalletFlow(id) {
    override fun latestWalletState(wallet: WalletState?): WalletState {
        if (wallet == null) throw IllegalArgumentException("no existing wallet state found with id: $id")
        else return wallet.updateWallet(now, upgrade = true)
    }
}

@StartableByRPC
class DisableWalletFlow(private val id: String) : UpsertWalletFlow(id) {
    override fun latestWalletState(wallet: WalletState?): WalletState {
        if (wallet == null) throw IllegalArgumentException("no existing wallet state found with id: $id")
        else return wallet.updateWallet(now, operation = WalletOperations.DISABLE)
    }
}

@StartableByRPC
class UpdateOtpDataFlow(private val id: String, private val otpData: OtpDataK) : UpsertWalletFlow(id) {
    override fun latestWalletState(wallet: WalletState?): WalletState {
        if (wallet == null) throw IllegalArgumentException("no existing wallet state found with id: $id")
        else return wallet.updateWallet(now, otpData = otpData)
    }
}

@StartableByRPC
class UpdateDivDataFlow(private val id: String, private val divData: String) : UpsertWalletFlow(id) {
    override fun latestWalletState(wallet: WalletState?): WalletState {
        if (wallet == null) throw IllegalArgumentException("no existing wallet state found with id: $id")
        else return wallet.updateWallet(now, divData = divData)
    }
}

@StartableByRPC
class RemoveWalletFlow(private val id: String) : UpsertWalletFlow(id) {
    override fun latestWalletState(wallet: WalletState?): WalletState? {
        if (wallet == null) throw IllegalArgumentException("no existing wallet state found with id: $id")
        return null
    }
}

private fun WalletState.updateWallet(
    now: Instant,
    pubKey: ByteArray? = null,
    cert: ByteArray? = null,
    operation: WalletOperations? = null,
    limit: LimitK? = null,
    displayName: String? = null,
    upgrade: Boolean? = null,
    otpData: OtpDataK? = null,
    divData: String? = null
): WalletState {
    val frozen = when (operation) {
        WalletOperations.FREEZE -> true
        WalletOperations.UNFREEZE -> false
        else -> null
    }

    val disabled = if (operation == WalletOperations.DISABLE) true else null

    // Status
    val updatedBalanceLimit = limit?.balanceLimit ?: this.balanceLimit
    val updatedDisplayName = displayName ?: this.dn

    // Setting
    val updatedPubKey = pubKey ?: this.setting.pubKey
    val updatedCertTxnLimit = limit?.certTxnLimit ?: this.setting.certTxnLimit
    val updatedKeyTxnLimit = limit?.keyTxnLimit ?: this.setting.keyTxnLimit
    val updatedFrozen = frozen ?: this.setting.frozen
    val updatedType = if (upgrade == true) WalletTypeK.REGISTERED else setting.typeK
    val updatedDisabled = disabled ?: this.setting.disabled
    val updatedDivData = divData ?: this.setting.divData
    val updatedCertPubKey = cert ?: this.setting.certInfo.pubKey
    val updatedCertInfo = this.setting.certInfo.copy(pubKey = updatedCertPubKey)

    // OtpData
    val updatedOtp = otpData ?: this.otpData

    val updatedStatus = this.status.copy(
        balanceLimit = updatedBalanceLimit,
        dn = updatedDisplayName,
        statusLastModified = now
    )

    val updatedSetting = this.setting.copy(
        pubKey = updatedPubKey,
        certTxnLimit = updatedCertTxnLimit,
        keyTxnLimit = updatedKeyTxnLimit,
        frozen = updatedFrozen,
        disabled = updatedDisabled,
        typeK = updatedType,
        divData = updatedDivData,
        certInfo = updatedCertInfo,
        settingLastModified = now
    )

    return WalletState.new(updatedStatus, updatedSetting, updatedOtp)
}

@CordaSerializable
enum class WalletOperations {
    FREEZE,
    UNFREEZE,
    DISABLE
}

@CordaSerializable
data class NewWalletK(
    val dn: String?,
    val balanceLimit: Long?,
    val phoneNumber: String?,
    val typeK: WalletTypeK?,
    val mcc: String?,
    val certTxnLimit: Long?,
    val keyTxnLimit: Long?,
    val certInfo: CertInfoK?,
    val pubKey: ByteArray?
) {
    private val _certTxnLimit: Long?
        get() {
            return certTxnLimit
                ?: if (certInfo != null) defaultCertTxnLimit
                else null
        }

    private val _keyTxnLimit: Long?
        get() {
            return keyTxnLimit
                ?: if (pubKey != null) defaultPubKeyTxnLimit
                else null
        }

    private val _balanceLimit: Long?
        get() {
            return balanceLimit ?: when {
                typeK == WalletTypeK.ANONYMOUS -> defaultAnonymousBalanceLimit
                certInfo != null -> defaultCertBalanceLimit
                pubKey != null -> defaultPubKeyBalanceLimit
                else -> null
            }
        }

    fun toWalletState(linearId: UniqueIdentifier, id: String, now: Instant) = WalletState(
        linearId = linearId,
        id = id,
        dn = dn,
        balanceLimit = _balanceLimit,
        phoneNumber = phoneNumber,
        typeK = typeK,
        mcc = mcc,
        certTxnLimit = _certTxnLimit,
        keyTxnLimit = _keyTxnLimit,
        certID = certInfo?.certID,
        certPubKey = certInfo?.pubKey,
        notAfter = certInfo?.notAfter,
        pubKey = pubKey,
        createTime = now,
        balance = 0L,
        disabled = false,
        divData = null,
        frozen = false,
        otp = null,
        otpCreateTime = null,
        settingLastModified = null,
        statusLastModified = null,
        totalCount = 0L
    )

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as NewWalletK

        if (dn != other.dn) return false
        if (balanceLimit != other.balanceLimit) return false
        if (phoneNumber != other.phoneNumber) return false
        if (typeK != other.typeK) return false
        if (mcc != other.mcc) return false
        if (certTxnLimit != other.certTxnLimit) return false
        if (keyTxnLimit != other.keyTxnLimit) return false
        if (certInfo != other.certInfo) return false
        if (pubKey != null) {
            if (other.pubKey == null) return false
            if (!pubKey.contentEquals(other.pubKey)) return false
        } else if (other.pubKey != null) return false

        return true
    }

    override fun hashCode(): Int {
        var result = dn?.hashCode() ?: 0
        result = 31 * result + (balanceLimit?.hashCode() ?: 0)
        result = 31 * result + (phoneNumber?.hashCode() ?: 0)
        result = 31 * result + (typeK?.hashCode() ?: 0)
        result = 31 * result + (mcc?.hashCode() ?: 0)
        result = 31 * result + (certTxnLimit?.hashCode() ?: 0)
        result = 31 * result + (keyTxnLimit?.hashCode() ?: 0)
        result = 31 * result + (certInfo?.hashCode() ?: 0)
        result = 31 * result + (pubKey?.contentHashCode() ?: 0)
        return result
    }
}
