package cbdc.test.api

import cbdc.corda.state.CENTRAL_BANK_VAULT_ID
import gov.cbc.cbdc.utilities.domain.entity.account.NewWallet
import gov.cbc.cbdc.utilities.domain.enums.WalletType
import net.corda.core.internal.join
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.junit.jupiter.SpringExtension
import java.util.concurrent.Executors

@ActiveProfiles(
    value = ["corda"]
)
@ExtendWith(SpringExtension::class)
@SpringBootTest(
    classes = [ApiTestClient::class],
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT
)
class LoadTest : BaseApiTest() {
    // Test variables
    private val user = WalletUser.NONFUNCTIONAL_TEST
    private val npWalletWithPubKeyBalanceLimit = 5000000L
    private val npWalletWithPubKeyTxnLimit = 5000000L
    private val senders = 6000L
    private val recipients = 6000L
    private val mintQuantity = (senders * npWalletWithPubKeyBalanceLimit).cbdc
    private val defaultThreads = 6
    private val newWallets = mutableMapOf<String, Long>()

    private fun newExecutor(threads: Int = defaultThreads) = Executors.newFixedThreadPool(threads)

    private fun walletType(type: TestWalletType): String {
        return when (type) {
            TestWalletType.SENDER -> "10"
            TestWalletType.RECIPIENT -> "20"
        }
    }

    private fun walletUser(type: WalletUser): String {
        return when (type) {
            WalletUser.NEIL -> "100"
            WalletUser.BORIS -> "200"
            WalletUser.DAISY -> "300"
            WalletUser.NONFUNCTIONAL_TEST -> "900"
        }
    }

    private fun newWallet() = NewWallet.builder()
        .dn("test")
        .balanceLimit(npWalletWithPubKeyBalanceLimit)
        .phoneNumber("0987654321")
        .type(WalletType.valueOf("REGISTERED"))
        .mcc("mcc")
        .keyTxnLimit(npWalletWithPubKeyTxnLimit)
        .certInfo(certInfo())
        .pubKey(byteArrayOf(0, 0, 0, 0))
        .build()

    private fun walletID(type: TestWalletType, user: WalletUser, i: Long): String {
        val wallet = "TW"
        val agencyNumber = "000"
        val checkCode = "0"
        val serialNumber = "0".repeat(10 - i.toString().length) + i.toString()
        return "$wallet-$agencyNumber-${walletType(type)}${walletUser(user)}$serialNumber$checkCode"
    }

    private fun initWallets(type: TestWalletType, wallets: Long) {
        val executor = newExecutor()
        for (i in 1..wallets) {
            executor.submit {
                val id = walletID(type, user, i)
                walletClient.getStatus(id) ?: walletClient.add(id, newWallet())
                println("init ${type.name} $i")
            }
        }
        executor.join()
    }

    private fun checkWallet(id: String, balance: Long) {
        val wallet = walletClient.getStatus(id)
        if (wallet != null) {
            assertEquals(wallet.balance, balance)
        } else {
            println("added new wallet $id")
            newWallets[id] = balance
            walletClient.add(id, newWallet())
        }
    }

    @Test
    fun `create cb and wallets`() {
        initCB()
        initWallets(TestWalletType.SENDER, senders)
        initWallets(TestWalletType.RECIPIENT, recipients)
    }

    @Test
    fun mint() {
        val executor = newExecutor()

        for (i in 1..defaultThreads) {
            executor.submit {
                adminClient.mint(txnId(), mintAndBurnAttr((npWalletWithPubKeyBalanceLimit * (senders / defaultThreads)).cbdc))
            }
        }
        executor.join()
    }

    @Test
    fun issue() {
        val executor = newExecutor()

        for (i in 1..senders) {
            executor.submit {
                val to = walletID(TestWalletType.SENDER, user, i)
                txClient.issue(txnId(), issueAttr(to, npWalletWithPubKeyBalanceLimit.cbdc))
                println("issued $i")
            }
        }
        executor.join()
    }

    @Test
    fun check() {
        val executor = newExecutor()

        for (i in 1..senders) {
            executor.submit {
                val id = walletID(TestWalletType.SENDER, user, i)
                checkWallet(id, npWalletWithPubKeyBalanceLimit)
            }
        }
        for (i in 1..recipients) {
            executor.submit {
                val id = walletID(TestWalletType.RECIPIENT, user, i)
                checkWallet(id, 0L)
            }
        }
        executor.join()

        newWallets.forEach {
            walletClient.getStatus(it.key).apply {
                assertEquals(this!!.balance, it.value)
            }
        }
    }

    @Test
    fun recycle() {
        val executor = newExecutor()

        for (i in 1..recipients) {
            executor.submit {
                val from = walletID(TestWalletType.RECIPIENT, user, i)
                walletClient.getStatus(from)?.apply {
                    if (this.balance != 0L) txClient.redeem(txnId(), redeemAttr(from, this.balance.cbdc))
                }
                println("recycle $i")
            }
        }
        executor.join()

        assertTrue(cbClient.get(CENTRAL_BANK_VAULT_ID)!!.balance >= mintQuantity.quantity.longValueExact())
    }
}

enum class TestWalletType {
    SENDER, RECIPIENT
}

enum class WalletUser {
    NEIL, BORIS, DAISY, NONFUNCTIONAL_TEST
}
