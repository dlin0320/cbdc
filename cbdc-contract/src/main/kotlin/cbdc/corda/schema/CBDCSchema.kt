package cbdc.corda.schema

import cbdc.corda.state.VaultType
import cbdc.corda.state.WalletTypeK
import cbdc.corda.state.generic.AddressId
import net.corda.core.schemas.MappedSchema
import net.corda.core.schemas.PersistentState
import java.time.Instant
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.Index
import javax.persistence.Table

object CBDCSchema

object CBDCSchemaV1 : MappedSchema(
    schemaFamily = CBDCSchema.javaClass, version = 1,
    mappedTypes = listOf(
        PersistentToken::class.java,
        PersistentWallet::class.java,
        PersistentVault::class.java,
        PersistentInstruction::class.java,
        PersistentAuthorization::class.java,
        PersistentNumber::class.java,
        PersistentPlan::class.java,
        PersistentDummy::class.java
    )
) {
    override val migrationResource = "cbdc-state.changelog-master"

    @Entity
    @Table
    (
        name = "token_state",
        indexes = [
            Index(columnList = "owner"),
            Index(columnList = "quantity"),
            Index(columnList = "token")
        ]
    )
    class PersistentToken(
        @Column(name = "owner")
        val owner: String? = null,

        @Column(name = "quantity", columnDefinition = "NUMERIC(19,2)")
        val quantity: Long? = null,

        @Column(name = "token")
        val token: String? = null
    ) : PersistentState()

    @Entity
    @Table
    (
        name = "wallet_state",
        indexes = [
            Index(columnList = "linear_id"),
            Index(columnList = "id"),
            Index(columnList = "dn"),
            Index(columnList = "balance"),
            Index(columnList = "balance_limit"),
            Index(columnList = "total_count"),
            Index(columnList = "status_last_modified"),
            Index(columnList = "phone_number"),
            Index(columnList = "type"),
            Index(columnList = "mcc"),
            Index(columnList = "cert_txn_limit"),
            Index(columnList = "key_txn_limit"),
            Index(columnList = "cert_id"),
            Index(columnList = "cert_pub_key"),
            Index(columnList = "not_after"),
            Index(columnList = "pub_key"),
            Index(columnList = "div_data"),
            Index(columnList = "frozen"),
            Index(columnList = "disabled"),
            Index(columnList = "create_time"),
            Index(columnList = "setting_last_modified"),
            Index(columnList = "otp"),
            Index(columnList = "otp_create_time")
        ]
    )
    class PersistentWallet(
        @Column(name = "linear_id")
        val linearId: String? = null,

        @Column(name = "id")
        val id: AddressId? = null,

        @Column(name = "dn")
        val dn: String? = null,

        @Column(name = "balance")
        val balance: Long? = null,

        @Column(name = "balance_limit", nullable = true)
        val balanceLimit: Long? = null,

        @Column(name = "total_count")
        val totalCount: Long? = null,

        @Column(name = "status_last_modified")
        val statusLastModified: Instant? = null,

        @Column(name = "phone_number")
        val phoneNumber: String? = null,

        @Column(name = "type")
        val type: WalletTypeK? = null,

        @Column(name = "mcc")
        val mcc: String? = null,

        @Column(name = "cert_txn_limit")
        val certTxnLimit: Long? = null,

        @Column(name = "key_txn_limit")
        val keyTxnLimit: Long? = null,

        @Column(name = "cert_id")
        val certID: String? = null,

        @Column(name = "cert_pub_key")
        val certPubKey: String? = null,

        @Column(name = "not_after")
        val notAfter: Instant? = null,

        @Column(name = "pub_key")
        val pubKey: String? = null,

        @Column(name = "div_data")
        val divData: String? = null,

        @Column(name = "frozen")
        val frozen: Boolean? = null,

        @Column(name = "disabled")
        val disabled: Boolean? = null,

        @Column(name = "create_time")
        val createTime: Instant? = null,

        @Column(name = "setting_last_modified")
        val settingLastModified: Instant? = null,

        @Column(name = "otp")
        val otp: String? = null,

        @Column(name = "otp_create_time")
        val otpCreateTime: Instant? = null
    ) : PersistentState()

    @Entity
    @Table
    (
        name = "vault_state",
        indexes = [
            Index(columnList = "linear_id"),
            Index(columnList = "id"),
            Index(columnList = "type"),
            Index(columnList = "balance"),
            Index(columnList = "total_count"),
            Index(columnList = "status_last_modified"),
            Index(columnList = "vault_cert_id"),
            Index(columnList = "vault_cert_pub_key"),
            Index(columnList = "vault_cert_not_after"),
            Index(columnList = "agency_cert_id"),
            Index(columnList = "agency_cert_pub_key"),
            Index(columnList = "agency_cert_not_after"),
            Index(columnList = "seed_key"),
            Index(columnList = "frozen"),
            Index(columnList = "disabled"),
            Index(columnList = "create_time"),
            Index(columnList = "setting_last_modified")
        ]
    )
    class PersistentVault(
        @Column(name = "linear_id")
        val linearId: String? = null,

        @Column(name = "id")
        val id: AddressId? = null,

        @Column(name = "type")
        val type: VaultType? = null,

        @Column(name = "balance")
        val balance: Long? = null,

        @Column(name = "total_count")
        val totalCount: Long? = null,

        @Column(name = "status_last_modified")
        val statusLastModified: Instant? = null,

        @Column(name = "vault_cert_id")
        val vaultCertID: String? = null,

        @Column(name = "vault_cert_pub_key")
        val vaultCertPubKey: String? = null,

        @Column(name = "vault_cert_not_after")
        val vaultCertNotAfter: Instant? = null,

        @Column(name = "agency_cert_id")
        val agencyCertID: String? = null,

        @Column(name = "agency_cert_pub_key")
        val agencyCertPubKey: String? = null,

        @Column(name = "agency_cert_not_after")
        val agencyCertNotAfter: Instant? = null,

        @Column(name = "seed_key")
        val seedKey: String? = null,

        @Column(name = "frozen")
        val frozen: Boolean? = null,

        @Column(name = "disabled")
        val disabled: Boolean? = null,

        @Column(name = "create_time")
        val createTime: Instant? = null,

        @Column(name = "setting_last_modified")
        val settingLastModified: Instant? = null
    ) : PersistentState()

    @Entity
    @Table
    (
        name = "instruction_state",
        indexes = [
            Index(columnList = "txn_id"),
            Index(columnList = "sender_id"),
            Index(columnList = "receiver_id"),
            Index(columnList = "timestamp")
        ]
    )
    class PersistentInstruction(
        @Column(name = "txn_id")
        val txnId: String? = null,

        @Column(name = "sender_id")
        val senderId: String? = null,

        @Column(name = "receiver_id")
        val receiverId: String? = null,

        @Column(name = "timestamp")
        val timestamp: Instant? = null
    ) : PersistentState()

    @Entity
    @Table
    (
        name = "authorization_state",
        indexes = [
            Index(columnList = "auth_id"),
            Index(columnList = "operation"),
            Index(columnList = "sender_id"),
            Index(columnList = "authorized_agency_id"),
            Index(columnList = "recipient_id"),
            Index(columnList = "remark"),
            Index(columnList = "void_remark"),
            Index(columnList = "status"),
            Index(columnList = "create_time")
        ]
    )
    class PersistentAuthorization(
        @Column(name = "auth_id")
        val authId: String? = null,

        @Column(name = "operation")
        val operation: String? = null,

        @Column(name = "sender_id")
        val senderId: String? = null,

        @Column(name = "authorized_agency_id")
        val authorizedAgencyId: String? = null,

        @Column(name = "recipient_id")
        val recipientId: String? = null,

        @Column(name = "remark")
        val remark: String? = null,

        @Column(name = "void_remark")
        val voidRemark: String? = null,

        @Column(name = "status")
        val status: Boolean? = null,

        @Column(name = "create_time")
        val createTime: Instant? = null
    ) : PersistentState()

    @Entity
    @Table
    (
        name = "number_state",
        indexes = [
            Index(columnList = "id"),
            Index(columnList = "wallet_id"),
            Index(columnList = "cvc"),
            Index(columnList = "amount"),
            Index(columnList = "enabled"),
            Index(columnList = "create_time")
        ]
    )
    class PersistentNumber(
        @Column(name = "id")
        val id: String? = null,

        @Column(name = "wallet_id")
        val walletId: String? = null,

        @Column(name = "cvc")
        val cvc: String? = null,

        @Column(name = "amount")
        val amount: Long? = null,

        @Column(name = "enabled")
        val enabled: Boolean? = null,

        @Column(name = "create_time")
        val createTime: Instant? = null
    ) : PersistentState()

    @Entity
    @Table
    (
        name = "plan_state",
        indexes = [
            Index(columnList = "operation"),
            Index(columnList = "amount_limit"),
            Index(columnList = "cert_allow"),
            Index(columnList = "key_allow"),
            Index(columnList = "remark")
        ]
    )
    class PersistentPlan(
        @Column(name = "operation")
        val operation: String? = null,

        @Column(name = "amount_limit")
        val amountLimit: Long? = null,

        @Column(name = "cert_allow")
        val certAllow: Boolean? = null,

        @Column(name = "key_allow")
        val keyAllow: Boolean? = null,

        @Column(name = "remark")
        val remark: String? = null
    ) : PersistentState()

    @Entity
    @Table
    (
        name = "dummy_state",
        indexes = [
            Index(columnList = "id")
        ]
    )
    class PersistentDummy(
        @Column(name = "id")
        val id: String? = null
    ) : PersistentState()
}
