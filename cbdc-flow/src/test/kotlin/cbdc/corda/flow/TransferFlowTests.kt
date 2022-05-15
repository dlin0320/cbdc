package cbdc.corda.flow

import cbdc.corda.contract.Instruction
import cbdc.corda.flow.util.BalanceInsufficientException
import cbdc.corda.flow.vault.CommonQueries.queryBalance
import cbdc.corda.state.CertInfoK
import cbdc.corda.state.LimitK
import cbdc.corda.state.WalletTypeK
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.io.File
import java.util.UUID
import kotlin.test.assertEquals

class TransferFlowTests : CBDCFlowTest() {
    private val walletA = "Wallet-A-${UUID.randomUUID()}"
    private val walletB = "Wallet-B-${UUID.randomUUID()}"

    private fun Int.nextId(max: Int): String {
        return if (this == max) "1"
        else "${this + 1}"
    }

    @BeforeEach
    override fun setup() {
        super.setup()
        nodeA.runFlow(
            InitialiseWalletFlow(
                walletA,
                NewWalletK(null, 10000, null, WalletTypeK.REGISTERED, null, null, null, CertInfoK("", byteArrayOf(), null), null)
            )
        )
        nodeA.runFlow(
            InitialiseWalletFlow(
                walletB,
                NewWalletK(null, 10000, null, WalletTypeK.REGISTERED, null, null, null, CertInfoK("", byteArrayOf(), null), null)
            )
        )
    }

    @Disabled
    @Test
    fun `multi transfers`() {
        File("./transfer_timestamps.txt").delete()
        val max = 10
        for (i in 1..max) {
            nodeA.runFlow(
                InitialiseWalletFlow(
                    "$i",
                    NewWalletK(null, 10000, null, WalletTypeK.REGISTERED, null, null, null, CertInfoK("", byteArrayOf(), null), null)
                )
            )
        }

        nodeA.issueTokensAt("1", 1.cbdc)

        for (i in 1..max) {
            when (i % 3) {
                0 -> {
                    nodeA.runFlow(TransferFlow(Instruction.Events.TRANSFER, "", "$i", i.nextId(max), 1.cbdc))
                }
                1 -> {
                    nodeB.runFlow(TransferFlow(Instruction.Events.TRANSFER, "", "$i", i.nextId(max), 1.cbdc))
                }
                2 -> {
                    nodeC.runFlow(TransferFlow(Instruction.Events.TRANSFER, "", "$i", i.nextId(max), 1.cbdc))
                }
            }
        }
    }

    @Test
    fun `transfer works`() {
        nodeA.issueTokensAt(walletA, 1.cbdc)
        nodeA.issueTokensAt(walletB, 3.3333.cbdc) // issue to another wallet
        nodeA.runFlow(TransferFlow(Instruction.Events.TRANSFER, "", walletA, walletB, 0.67.cbdc))
        allPeers.forEach {
            it.transactionEx {
                assertEquals(0.33.cbdc, services.queryBalance(walletA))
                assertEquals(4.cbdc, services.queryBalance(walletB))
            }
        }
    }

    @Test
    fun `transfer - receiver balance exceeded`() {
        val limit = LimitK(10000, null, null)
        nodeA.issueTokensAt(walletA, 100.cbdc)
        nodeA.issueTokensAt(walletB, 3.3333.cbdc) // issue to another wallet
        nodeA.runFlow(UpdateLimitFlow(walletB, limit))
        assertThrows<IllegalArgumentException> {
            nodeA.runFlow(TransferFlow(Instruction.Events.TRANSFER, "", walletA, walletB, 100.cbdc))
        }
    }

    @Test
    fun `transfer - sender balance insufficient`() {
        nodeA.issueTokensAt(walletA, 1.cbdc)
        nodeA.issueTokensAt(walletB, 3.3333.cbdc) // issue to another wallet
        assertThrows<BalanceInsufficientException> {
            nodeA.runFlow(TransferFlow(Instruction.Events.TRANSFER, "", walletA, walletB, 1.07.cbdc))
        }
    }

    @Test
    fun `transfer - receiver does not exist`() {
        val walletC = "Wallet-C-${UUID.randomUUID()}"
        nodeA.issueTokensAt(walletA, 1.cbdc)
        nodeA.issueTokensAt(walletC, 3.3333.cbdc) // issue to another wallet
        assertThrows<IllegalArgumentException> {
            nodeA.runFlow(TransferFlow(Instruction.Events.TRANSFER, "", walletA, walletC, 0.67.cbdc))
        }
    }
}
