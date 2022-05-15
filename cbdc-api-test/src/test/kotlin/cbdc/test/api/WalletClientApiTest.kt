package cbdc.test.api

import gov.cbc.cbdc.utilities.domain.enums.WalletType
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.junit.jupiter.SpringExtension

@ExtendWith(SpringExtension::class)
@SpringBootTest(
    classes = [ApiTestClient::class],
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT
)
class WalletClientApiTest : BaseApiTest() {

    @Test
    fun `add wallets`() {
        val registeredWallet = newWallet(WalletType.REGISTERED)
        val anonymousWallet = newWallet(WalletType.ANONYMOUS)
        walletClient.add("wallet1", registeredWallet)
        walletClient.add("wallet2", anonymousWallet)
    }
}
