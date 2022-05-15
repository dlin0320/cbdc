package cbdc.test.api

import cbdc.corda.state.CENTRAL_BANK_VAULT_ID
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.junit.jupiter.SpringExtension

@ExtendWith(SpringExtension::class)
@SpringBootTest(
    classes = [ApiTestClient::class],
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT
)
class PreLoadTest : BaseApiTest() {

    @BeforeEach
    fun init() {
        initCB()
        createWallets()
    }

    @Test
    fun `happy`() {
        val vaultBalance = cbClient.get(CENTRAL_BANK_VAULT_ID)!!.balance

        adminClient.mint(txnId(), mintAndBurnAttr(quantity = 10.0.cbdc))
        cbClient.get(CENTRAL_BANK_VAULT_ID).apply {
            assertEquals(vaultBalance + 10.0.cbdc.quantity.longValueExact(), this?.balance)
        }

        txClient.transfer(txnId(), transferAttr(CENTRAL_BANK_VAULT_ID, wallet1ID, quantity))
        walletClient.getTotal(wallet1ID).apply {
            assertEquals(wallet1Balance + quantity.quantity.longValueExact(), this?.balance)
        }
        txClient.transfer(txnId(), transferAttr(CENTRAL_BANK_VAULT_ID, wallet2ID, quantity))
        walletClient.getTotal(wallet2ID).apply {
            assertEquals(wallet2Balance + quantity.quantity.longValueExact(), this?.balance)
        }
        txClient.transfer(txnId(), transferAttr(wallet1ID, wallet2ID, quantity))
        walletClient.getTotal(wallet2ID).apply {
            assertEquals(10L, this?.balance)
        }
    }
}
