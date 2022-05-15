package cbdc.test.api

import cbdc.corda.state.CENTRAL_BANK_VAULT_ID
import gov.cbc.cbdc.utilities.domain.entity.LedgerState
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.junit.jupiter.SpringExtension
import java.time.Instant

@ExtendWith(SpringExtension::class)
@SpringBootTest(
    classes = [ApiTestClient::class],
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT
)
class LedgerClientApiTest : BaseApiTest() {

    @BeforeEach
    fun init() {
        initCB()
        createWallets()
        txClient.transfer(txnId(), transferAttr(CENTRAL_BANK_VAULT_ID, wallet1ID, quantity))
        txClient.transfer(txnId(), transferAttr(CENTRAL_BANK_VAULT_ID, wallet2ID, quantity))
    }

    @Test
    fun `query transaction with id`() {
        val txnId = txnId()
        txClient.transfer(txnId, transferAttr(wallet1ID, wallet2ID, 3.0.cbdc))
        ledgerClient.get(txnId).let {
            assertNotNull(it)
            assertEquals(txnId, it!!.id)
        }
    }

    @Test
    fun `query transaction with time range`() {
        val from = Instant.now()
        val txnId = txnId()
        txClient.transfer(txnId, transferAttr(wallet1ID, wallet2ID, 3.0.cbdc))
        val to = Instant.now()

        ledgerClient.getList(wallet1ID, from.toString(), to.toString()).let {
            val txnIds = it.map(LedgerState::getId)
            assertTrue(txnId in txnIds)
            it.forEach { tx ->
                tx.txnTime in from..to
            }
        }
    }
}
