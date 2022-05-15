package cbdc.test.api

import cbdc.corda.state.CENTRAL_BANK_VAULT_ID
import gov.cbc.cbdc.utilities.domain.entity.TransferAttribute
import gov.cbc.cbdc.utilities.domain.entity.authorization.Authorization
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.junit.jupiter.SpringExtension
import java.util.UUID

@ExtendWith(SpringExtension::class)
@SpringBootTest(
    classes = [ApiTestClient::class],
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT
)
class ConsumerApiTest : BaseApiTest() {
    @BeforeEach
    fun init() {
        initCB()
        adminClient.mint(txnId(), mintAndBurnAttr(5.0.cbdc))
        createWallets()
    }

    @Test
    fun `transfer and get balance`() {
        txClient.issue(
            txnId(),
            TransferAttribute.builder()
                .senderID(CENTRAL_BANK_VAULT_ID)
                .recipientID(wallet1ID)
                .amount(quantity.quantity.longValueExact())
                .build()
        )
        txClient.transfer(
            txnId(),
            TransferAttribute.builder()
                .senderID(wallet1ID)
                .recipientID(wallet2ID)
                .amount(quantity.quantity.longValueExact())
                .build()
        )
        walletClient.getTotal(wallet2ID).apply {
            assertEquals(wallet2Balance + quantity.quantity.longValueExact(), this?.balance)
        }
    }

    private val auth = Authorization.builder()
        .authID("1")
        .operation("authorize")
        .senderID(wallet1ID)
        .authorizedAgencyID(authorizedID)
        .recipientID(wallet2ID)
        .remark("remark")
        .build()

    @Test
    fun `authorize and unauthorize`() {
        val authID = UUID.randomUUID().toString()
        auth.authID = authID
        contractClient.add(auth)
        contractClient.get("authorize", authID).let {
            assertNotNull(it)
            assertEquals(auth, it)
        }

        contractClient.cancel("authorize", authID, "void remark!")
        contractClient.get("authorize", authID).let {
            assertNotNull(it)
            assertEquals("void remark!", it.voidRemark)
        }
    }

    @Test
    fun `authorize pay`() {
        val authID = UUID.randomUUID().toString()
        auth.authID = authID
        contractClient.add(auth)
        contractClient.get("authorize", "1").let {
            assertNotNull(it)
            assertEquals(auth, it)
        }

        val transfer = TransferAttribute.builder()
            .senderID(wallet1ID)
            .recipientID(wallet2ID)
            .authorizedAgencyID(authorizedID)
            .amount(500)
            .remark("remark")
            .build()
        txClient.transfer("txnId", transfer)
    }

    @Test
    fun `wallet already exists exception`() {
        val walletId = walletId()
        walletClient.add(walletId, newWallet())
        Thread.sleep(5000)
        assertThrows<Exception> { walletClient.add(walletId, newWallet()) }
    }
}
