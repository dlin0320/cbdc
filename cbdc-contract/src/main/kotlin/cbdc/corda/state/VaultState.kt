package cbdc.corda.state

import cbdc.corda.contract.VaultContract
import cbdc.corda.schema.CBDCSchemaV1
import cbdc.corda.state.generic.AddressId
import cbdc.corda.state.generic.AddressState
import cbdc.corda.state.generic.BroadcastedState
import net.corda.core.contracts.BelongsToContract
import net.corda.core.contracts.UniqueIdentifier
import net.corda.core.schemas.MappedSchema
import net.corda.core.schemas.PersistentState
import net.corda.core.schemas.QueryableState
import net.corda.core.serialization.CordaSerializable
import java.time.Instant
import java.util.Date

const val CENTRAL_BANK_VAULT_ID = "CB-Minter" // CB vault has a fixed wallet id

@BelongsToContract(VaultContract::class)
data class VaultState(
    // Corda
    override val linearId: UniqueIdentifier,
    override val id: AddressId,
    val type: VaultType,

    // Status
    val balance: Long?,
    val totalCount: Long?,
    val statusLastModified: Instant?,

    // Setting
    val vaultCertID: String?,
    val vaultCertPubKey: ByteArray?,
    val vaultCertNotAfter: Date?,
    val agencyCertID: String?,
    val agencyCertPubKey: ByteArray?,
    val agencyCertNotAfter: Date?,
    val seedKey: ByteArray?,
    val frozen: Boolean?,
    val disabled: Boolean?,
    val createTime: Instant?,
    val settingLastModified: Instant?
) : AddressState, BroadcastedState, QueryableState {
    companion object {
        fun new(status: VaultStatusK, setting: VaultSettingK, type: VaultType): VaultState {
            return VaultState(
                linearId = UniqueIdentifier(),
                id = status.id,
                type = type,

                balance = status.balance,
                totalCount = status.totalCount,
                statusLastModified = status.statusLastModified,

                vaultCertID = setting.vaultCertInfo?.certID,
                vaultCertPubKey = setting.vaultCertInfo?.pubKey,
                vaultCertNotAfter = setting.vaultCertInfo?.notAfter,
                agencyCertID = setting.agencyCertInfo?.certID,
                agencyCertPubKey = setting.agencyCertInfo?.pubKey,
                agencyCertNotAfter = setting.agencyCertInfo?.notAfter,
                seedKey = setting.seedKey,
                frozen = setting.frozen,
                disabled = setting.disabled,
                createTime = setting.createTime,
                settingLastModified = setting.settingLastModified
            )
        }
    }

    val vaultCertInfo: CertInfoK
        get() = CertInfoK(vaultCertID, vaultCertPubKey, vaultCertNotAfter)

    val agencyCertInfo: CertInfoK
        get() = CertInfoK(agencyCertID, agencyCertPubKey, agencyCertNotAfter)

    val status: VaultStatusK
        get() = VaultStatusK(id, balance, totalCount, statusLastModified)

    val setting: VaultSettingK
        get() = VaultSettingK(id, vaultCertInfo, agencyCertInfo, seedKey, frozen, disabled, createTime, settingLastModified)

    override fun generateMappedObject(schema: MappedSchema): PersistentState {
        return when (schema) {
            CBDCSchemaV1 -> CBDCSchemaV1.PersistentVault(
                linearId = linearId.toString(),
                id = id,
                type = type,
                balance = balance,
                totalCount = totalCount,
                statusLastModified = statusLastModified,
                vaultCertID = vaultCertID,
                vaultCertPubKey = vaultCertPubKey?.toString(),
                vaultCertNotAfter = vaultCertNotAfter?.toInstant(),
                agencyCertID = agencyCertID,
                agencyCertPubKey = agencyCertPubKey?.toString(),
                agencyCertNotAfter = agencyCertNotAfter?.toInstant(),
                seedKey = seedKey?.toString(),
                frozen = frozen,
                disabled = disabled,
                createTime = createTime,
                settingLastModified = settingLastModified
            )
            else -> throw IllegalArgumentException("Unsupported schema")
        }
    }

    override fun supportedSchemas(): Iterable<MappedSchema> = listOf(CBDCSchemaV1)

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as VaultState

        if (linearId != other.linearId) return false
        if (id != other.id) return false
        if (balance != other.balance) return false
        if (totalCount != other.totalCount) return false
        if (statusLastModified != other.statusLastModified) return false
        if (vaultCertID != other.vaultCertID) return false
        if (vaultCertPubKey != null) {
            if (other.vaultCertPubKey == null) return false
            if (!vaultCertPubKey.contentEquals(other.vaultCertPubKey)) return false
        } else if (other.vaultCertPubKey != null) return false
        if (vaultCertNotAfter != other.vaultCertNotAfter) return false
        if (agencyCertID != other.agencyCertID) return false
        if (agencyCertPubKey != null) {
            if (other.agencyCertPubKey == null) return false
            if (!agencyCertPubKey.contentEquals(other.agencyCertPubKey)) return false
        } else if (other.agencyCertPubKey != null) return false
        if (agencyCertNotAfter != other.agencyCertNotAfter) return false
        if (seedKey != null) {
            if (other.seedKey == null) return false
            if (!seedKey.contentEquals(other.seedKey)) return false
        } else if (other.seedKey != null) return false
        if (frozen != other.frozen) return false
        if (disabled != other.disabled) return false
        if (createTime != other.createTime) return false
        if (settingLastModified != other.settingLastModified) return false

        return true
    }

    override fun hashCode(): Int {
        var result = linearId.hashCode()
        result = 31 * result + id.hashCode()
        result = 31 * result + (balance?.hashCode() ?: 0)
        result = 31 * result + (totalCount?.hashCode() ?: 0)
        result = 31 * result + (statusLastModified?.hashCode() ?: 0)
        result = 31 * result + (vaultCertID?.hashCode() ?: 0)
        result = 31 * result + (vaultCertPubKey?.contentHashCode() ?: 0)
        result = 31 * result + (vaultCertNotAfter?.hashCode() ?: 0)
        result = 31 * result + (agencyCertID?.hashCode() ?: 0)
        result = 31 * result + (agencyCertPubKey?.contentHashCode() ?: 0)
        result = 31 * result + (agencyCertNotAfter?.hashCode() ?: 0)
        result = 31 * result + (seedKey?.contentHashCode() ?: 0)
        result = 31 * result + (frozen?.hashCode() ?: 0)
        result = 31 * result + (disabled?.hashCode() ?: 0)
        result = 31 * result + (createTime?.hashCode() ?: 0)
        result = 31 * result + (settingLastModified?.hashCode() ?: 0)
        return result
    }
}

@CordaSerializable
enum class VaultType {
    CENTRAL_BANK,
    COMMERCIAL_BANK,
    MERCHANT,
    PUBLIC
}

@CordaSerializable
data class VaultStatusK(
    val id: String,
    val balance: Long?,
    val totalCount: Long?,
    val statusLastModified: Instant?
)

@CordaSerializable
data class VaultSettingK(
    val id: String,
    val vaultCertInfo: CertInfoK?,
    val agencyCertInfo: CertInfoK?,
    val seedKey: ByteArray?,
    val frozen: Boolean?,
    val disabled: Boolean?,
    val createTime: Instant?,
    val settingLastModified: Instant?
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as VaultSettingK

        if (id != other.id) return false
        if (vaultCertInfo != other.vaultCertInfo) return false
        if (agencyCertInfo != other.agencyCertInfo) return false
        if (seedKey != null) {
            if (other.seedKey == null) return false
            if (!seedKey.contentEquals(other.seedKey)) return false
        } else if (other.seedKey != null) return false
        if (frozen != other.frozen) return false
        if (disabled != other.disabled) return false
        if (createTime != other.createTime) return false
        if (settingLastModified != other.settingLastModified) return false

        return true
    }

    override fun hashCode(): Int {
        var result = id.hashCode()
        result = 31 * result + (vaultCertInfo?.hashCode() ?: 0)
        result = 31 * result + (agencyCertInfo?.hashCode() ?: 0)
        result = 31 * result + (seedKey?.contentHashCode() ?: 0)
        result = 31 * result + (frozen?.hashCode() ?: 0)
        result = 31 * result + (disabled?.hashCode() ?: 0)
        result = 31 * result + (createTime?.hashCode() ?: 0)
        result = 31 * result + (settingLastModified?.hashCode() ?: 0)
        return result
    }
}

@CordaSerializable
data class CertInfoK(
    val certID: String?,
    val pubKey: ByteArray?,
    val notAfter: Date?
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as CertInfoK

        if (certID != other.certID) return false
        if (pubKey != null) {
            if (other.pubKey == null) return false
            if (!pubKey.contentEquals(other.pubKey)) return false
        } else if (other.pubKey != null) return false
        if (notAfter != other.notAfter) return false

        return true
    }

    override fun hashCode(): Int {
        var result = certID?.hashCode() ?: 0
        result = 31 * result + (pubKey?.contentHashCode() ?: 0)
        result = 31 * result + (notAfter?.hashCode() ?: 0)
        return result
    }
}
