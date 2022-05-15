package cbdc.corda.state

import cbdc.corda.contract.WalletContract
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

@BelongsToContract(WalletContract::class)
data class WalletState(
    // Corda
    override val linearId: UniqueIdentifier,
    override val id: AddressId,

    // Status
    val dn: String?,
    val balance: Long?,
    val balanceLimit: Long?,
    val totalCount: Long?,
    val statusLastModified: Instant?,

    // Setting
    val phoneNumber: String?,
    val typeK: WalletTypeK?,
    val mcc: String?,
    val certTxnLimit: Long?,
    val keyTxnLimit: Long?,
    val certID: String?,
    val certPubKey: ByteArray?,
    val notAfter: Date?,
    val pubKey: ByteArray?,
    val divData: String?,
    val frozen: Boolean?,
    val disabled: Boolean?,
//    not saved for now due to unknown liquibase type
//    val cvc: List<String>?,
    val createTime: Instant?,
    val settingLastModified: Instant?,

    // Otp
    val otp: String?,
    val otpCreateTime: Instant?
) : AddressState, BroadcastedState, QueryableState {
    companion object {
        const val defaultCertTxnLimit = 50000000L

        const val defaultCertBalanceLimit = 9223372036854775807L

        const val defaultPubKeyTxnLimit = 5000000L

        const val defaultPubKeyBalanceLimit = 5000000L

        const val defaultAnonymousTxnLimit = 3000L

        const val defaultAnonymousBalanceLimit = 10000L

        fun new(status: WalletStatusK, setting: WalletSettingK, otpData: OtpDataK): WalletState {
            return WalletState(
                linearId = UniqueIdentifier(),
                id = status.id,

                dn = status.dn,
                balance = status.balance,
                balanceLimit = status.balanceLimit,
                totalCount = status.totalCount,
                statusLastModified = status.statusLastModified,

                phoneNumber = setting.phoneNumber,
                typeK = setting.typeK,
                mcc = setting.mcc,
                certTxnLimit = setting.certTxnLimit,
                keyTxnLimit = setting.keyTxnLimit,
                certID = setting.certInfo.certID,
                certPubKey = setting.certInfo.pubKey,
                notAfter = setting.certInfo.notAfter,
                pubKey = setting.pubKey,
                divData = setting.divData,
                frozen = setting.frozen,
                disabled = setting.disabled,
//                cvc = setting.cvc,
                createTime = setting.createTime,
                settingLastModified = setting.settingLastModified,

                otp = otpData.otp,
                otpCreateTime = otpData.createTime
            )
        }
    }

    val certInfo = CertInfoK(certID, certPubKey, notAfter)

    val status = WalletStatusK(id, dn, balance, balanceLimit, totalCount, statusLastModified)

    val setting = WalletSettingK(id, phoneNumber, typeK, mcc, certTxnLimit, keyTxnLimit, certInfo, pubKey, divData, frozen, disabled, createTime, settingLastModified)

    val otpData = OtpDataK(otp, createTime)

    override fun generateMappedObject(schema: MappedSchema): PersistentState {
        return when (schema) {
            CBDCSchemaV1 -> CBDCSchemaV1.PersistentWallet(
                linearId = linearId.toString(),
                id = id,
                dn = dn,
                balance = balance,
                balanceLimit = balanceLimit,
                totalCount = totalCount,
                statusLastModified = statusLastModified,
                phoneNumber = phoneNumber,
                type = typeK,
                mcc = mcc,
                certTxnLimit = certTxnLimit,
                keyTxnLimit = keyTxnLimit,
                certID = certID,
                certPubKey = certPubKey?.toString(),
                notAfter = notAfter?.toInstant(),
                pubKey = pubKey?.toString(),
                divData = divData,
                frozen = frozen,
                disabled = disabled,
                createTime = createTime,
                settingLastModified = settingLastModified,
                otp = otp,
                otpCreateTime = otpCreateTime
            )
            else -> throw IllegalArgumentException("Unsupported schema")
        }
    }

    override fun supportedSchemas() = listOf(CBDCSchemaV1)

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as WalletState

        if (linearId != other.linearId) return false
        if (id != other.id) return false
        if (dn != other.dn) return false
        if (balance != other.balance) return false
        if (balanceLimit != other.balanceLimit) return false
        if (totalCount != other.totalCount) return false
        if (statusLastModified != other.statusLastModified) return false
        if (typeK != other.typeK) return false
        if (mcc != other.mcc) return false
        if (certTxnLimit != other.certTxnLimit) return false
        if (keyTxnLimit != other.keyTxnLimit) return false
        if (certID != other.certID) return false
        if (certPubKey != null) {
            if (other.certPubKey == null) return false
            if (!certPubKey.contentEquals(other.certPubKey)) return false
        } else if (other.certPubKey != null) return false
        if (notAfter != other.notAfter) return false
        if (pubKey != null) {
            if (other.pubKey == null) return false
            if (!pubKey.contentEquals(other.pubKey)) return false
        } else if (other.pubKey != null) return false
        if (divData != other.divData) return false
        if (frozen != other.frozen) return false
        if (disabled != other.disabled) return false
        if (createTime != other.createTime) return false
        if (settingLastModified != other.settingLastModified) return false
        if (certInfo != other.certInfo) return false
        if (setting != other.setting) return false

        return true
    }

    override fun hashCode(): Int {
        var result = linearId.hashCode()
        result = 31 * result + id.hashCode()
        result = 31 * result + (dn?.hashCode() ?: 0)
        result = 31 * result + (balance?.hashCode() ?: 0)
        result = 31 * result + (balanceLimit?.hashCode() ?: 0)
        result = 31 * result + (totalCount?.hashCode() ?: 0)
        result = 31 * result + (statusLastModified?.hashCode() ?: 0)
        result = 31 * result + (typeK?.hashCode() ?: 0)
        result = 31 * result + (mcc?.hashCode() ?: 0)
        result = 31 * result + (certTxnLimit?.hashCode() ?: 0)
        result = 31 * result + (keyTxnLimit?.hashCode() ?: 0)
        result = 31 * result + (certID?.hashCode() ?: 0)
        result = 31 * result + (certPubKey?.contentHashCode() ?: 0)
        result = 31 * result + (notAfter?.hashCode() ?: 0)
        result = 31 * result + (pubKey?.contentHashCode() ?: 0)
        result = 31 * result + (divData?.hashCode() ?: 0)
        result = 31 * result + (frozen?.hashCode() ?: 0)
        result = 31 * result + (disabled?.hashCode() ?: 0)
        result = 31 * result + (createTime?.hashCode() ?: 0)
        result = 31 * result + (settingLastModified?.hashCode() ?: 0)
        result = 31 * result + certInfo.hashCode()
        result = 31 * result + setting.hashCode()
        return result
    }
}

@CordaSerializable
enum class WalletTypeK {
    REGISTERED,
    ANONYMOUS
}

@CordaSerializable
data class WalletStatusK(
    val id: String,
    val dn: String?,
    val balance: Long?,
    val balanceLimit: Long?,
    val totalCount: Long?,
    val statusLastModified: Instant?
)

@CordaSerializable
data class WalletSettingK(
    val id: String?,
    val phoneNumber: String?,
    val typeK: WalletTypeK?,
    val mcc: String?,
    val certTxnLimit: Long?,
    val keyTxnLimit: Long?,
    val certInfo: CertInfoK,
    val pubKey: ByteArray?,
    val divData: String?,
    val frozen: Boolean?,
    val disabled: Boolean?,
//    val cvc: List<String>?,
    val createTime: Instant?,
    val settingLastModified: Instant?
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as WalletSettingK

        if (id != other.id) return false
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
        if (divData != other.divData) return false
        if (frozen != other.frozen) return false
        if (disabled != other.disabled) return false
//        if (cvc != other.cvc) return false
        if (createTime != other.createTime) return false
        if (settingLastModified != other.settingLastModified) return false

        return true
    }

    override fun hashCode(): Int {
        var result = id?.hashCode() ?: 0
        result = 31 * result + (phoneNumber?.hashCode() ?: 0)
        result = 31 * result + (typeK?.hashCode() ?: 0)
        result = 31 * result + (mcc?.hashCode() ?: 0)
        result = 31 * result + (certTxnLimit?.hashCode() ?: 0)
        result = 31 * result + (keyTxnLimit?.hashCode() ?: 0)
        result = 31 * result + certInfo.hashCode()
        result = 31 * result + (pubKey?.contentHashCode() ?: 0)
        result = 31 * result + (divData?.hashCode() ?: 0)
        result = 31 * result + (frozen?.hashCode() ?: 0)
        result = 31 * result + (disabled?.hashCode() ?: 0)
//        result = 31 * result + (cvc?.hashCode() ?: 0)
        result = 31 * result + (createTime?.hashCode() ?: 0)
        result = 31 * result + (settingLastModified?.hashCode() ?: 0)
        return result
    }
}

@CordaSerializable
data class LimitK(
    val balanceLimit: Long?,
    val certTxnLimit: Long?,
    val keyTxnLimit: Long?
)

@CordaSerializable
data class OtpDataK(
    val otp: String?,
    val createTime: Instant?
)
