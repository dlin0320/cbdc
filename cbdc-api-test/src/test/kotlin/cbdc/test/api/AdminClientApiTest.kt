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
class AdminClientApiTest : BaseApiTest() {

    private fun cbBalance() = vaultClient.getTotal(CENTRAL_BANK_VAULT_ID)!!

    @BeforeEach
    fun initCBVault() {
        initCB()
    }

    @Test
    fun mint() {
        val initialBalance = cbBalance()
        adminClient.mint(txnId(), mintAndBurnAttr(3.0.cbdc))
        cbBalance().apply {
            assertEquals(initialBalance.balance + 3.0.cbdc.quantity.longValueExact(), balance)
        }
    }

    @Test
    fun burn() {
        val initialBalance = cbBalance()
        adminClient.burn(txnId(), mintAndBurnAttr(2.0.cbdc))
        cbBalance().apply {
            assertEquals(initialBalance.balance - 2.0.cbdc.quantity.longValueExact(), balance)
        }
    }
}
