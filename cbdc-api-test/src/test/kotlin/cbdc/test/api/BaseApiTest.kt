package cbdc.test.api

import cbdc.corda.state.CENTRAL_BANK_VAULT_ID
import cbdc.corda.state.generic.AddressId
import cbdc.corda.state.generic.Amount
import gov.cbc.cbdc.utilities.client.AdminClient
import gov.cbc.cbdc.utilities.client.CBClient
import gov.cbc.cbdc.utilities.client.ContractClient
import gov.cbc.cbdc.utilities.client.LedgerClient
import gov.cbc.cbdc.utilities.client.NumberClient
import gov.cbc.cbdc.utilities.client.OtpClient
import gov.cbc.cbdc.utilities.client.PlanClient
import gov.cbc.cbdc.utilities.client.TestClient
import gov.cbc.cbdc.utilities.client.TransactionClient
import gov.cbc.cbdc.utilities.client.VaultClient
import gov.cbc.cbdc.utilities.client.WalletClient
import gov.cbc.cbdc.utilities.domain.entity.CertInfo
import gov.cbc.cbdc.utilities.domain.entity.TransferAttribute
import gov.cbc.cbdc.utilities.domain.entity.account.CBVault
import gov.cbc.cbdc.utilities.domain.entity.account.NewVault
import gov.cbc.cbdc.utilities.domain.entity.account.NewWallet
import gov.cbc.cbdc.utilities.domain.enums.WalletType
import org.springframework.beans.factory.annotation.Autowired
import java.math.BigDecimal
import java.time.Instant
import java.util.Date
import java.util.Random

open class BaseApiTest {

    @Autowired
    lateinit var cbClient: CBClient

    @Autowired
    lateinit var testClient: TestClient

    @Autowired
    lateinit var txClient: TransactionClient

    @Autowired
    lateinit var ledgerClient: LedgerClient

    @Autowired
    lateinit var adminClient: AdminClient

    @Autowired
    lateinit var walletClient: WalletClient

    @Autowired
    lateinit var contractClient: ContractClient

    @Autowired
    lateinit var vaultClient: VaultClient

    @Autowired
    lateinit var otpClient: OtpClient

    @Autowired
    lateinit var numberClient: NumberClient

    @Autowired
    lateinit var planClient: PlanClient

    companion object {
        const val authorizedID = "TW-F-000-0000000000000000"
        const val wallet1ID = "TW-B-001-0000000000000000"
        const val wallet2ID = "TW-B-002-0000000000000000"
        val Double.cbdc get() = Quantity(BigDecimal.valueOf(this))
        val Long.cbdc get() = Quantity(BigDecimal.valueOf(this))
        val quantity = 5.0.cbdc
        var wallet1Balance = 0L
        var wallet2Balance = 0L
    }

    val cbVault: CBVault?
        get() = cbClient.get(CENTRAL_BANK_VAULT_ID)

    fun initCB() {
        cbClient.get(CENTRAL_BANK_VAULT_ID) ?: testClient.addCentralBank()
    }

    fun createWallets() {
        wallet1Balance = walletClient.getStatus(wallet1ID)?.balance ?: {
            walletClient.add(wallet1ID, newWallet())
            0L
        }.invoke()

        wallet2Balance = walletClient.getStatus(wallet2ID)?.balance ?: {
            walletClient.add(wallet2ID, newWallet())
            0L
        }.invoke()
    }

    fun mintAndBurnAttr(quantity: Quantity) = TransferAttribute.builder()
        .amount(quantity.quantity.longValueExact())
        .cvc(quantity.toAmount().token)
        .remark("REMARK")
        .build()

    fun redeemAttr(senderId: AddressId, quantity: Quantity) = TransferAttribute.builder()
        .amount(quantity.quantity.longValueExact())
        .cvc(quantity.toAmount().token)
        .senderID(senderId)
        .recipientID(CENTRAL_BANK_VAULT_ID)
        .remark("REMARK")
        .build()

    fun issueAttr(recipientId: AddressId, quantity: Quantity) = TransferAttribute.builder()
        .amount(quantity.quantity.longValueExact())
        .cvc(quantity.toAmount().token)
        .senderID(CENTRAL_BANK_VAULT_ID)
        .recipientID(recipientId)
        .remark("REMARK")
        .build()

    fun transferAttr(senderId: AddressId, recipientId: AddressId, quantity: Quantity) = TransferAttribute.builder()
        .amount(quantity.quantity.longValueExact())
        .cvc(quantity.toAmount().token)
        .senderID(senderId)
        .recipientID(recipientId)
        .remark("REMARK")
        .build()

    private val rand = Random()
    fun randomIdOf(length: Int): String {
        val buf = StringBuffer()
        repeat(length) {
            buf.append(rand.nextInt(10))
        }
        return buf.toString()
    }

    fun vaultId() = "B-001-${randomIdOf(16)}"
    fun txnId() = "${vaultId()}-${Instant.now().toEpochMilli()}-${randomIdOf(3)}"

    fun newVault() = NewVault.builder()
        .agencyCertInfo(certInfo())
        .vaultCertInfo(certInfo())
        .build()

    fun certInfo() = CertInfo.builder()
        .certID("CERT_ID")
        .notAfter(Date.from(Instant.now().plusSeconds(86400 * 7)))
        .pubKey(byteArrayOf(0, 0, 0, 0))
        .build()

    fun newWallet(type: WalletType = WalletType.ANONYMOUS) = NewWallet.builder()
        .certInfo(certInfo())
        .type(type)
        .build()

    fun walletId() = "TW-B-001-${randomIdOf(16)}"
}

data class Quantity(
    val quantity: BigDecimal
) {
    fun toAmount() = Amount.fromDecimal(quantity)
}
