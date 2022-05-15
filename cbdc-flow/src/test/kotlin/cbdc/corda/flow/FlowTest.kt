package cbdc.corda.flow

import net.corda.core.flows.FlowLogic
import net.corda.core.node.StatesToRecord
import net.corda.core.transactions.SignedTransaction
import net.corda.core.transactions.TransactionBuilder
import net.corda.core.utilities.getOrThrow
import net.corda.testing.node.MockNetwork
import net.corda.testing.node.MockNetworkParameters
import net.corda.testing.node.StartedMockNode
import net.corda.testing.node.TestCordapp
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach

abstract class FlowTest {
    protected lateinit var network: MockNetwork
    protected lateinit var nodeA: StartedMockNode
    protected lateinit var nodeB: StartedMockNode
    protected lateinit var nodeC: StartedMockNode
    protected val allPeers get() = listOf(nodeA, nodeB, nodeC)

    @BeforeEach
    open fun setup() {
        network = MockNetwork(
            MockNetworkParameters(
                cordappsForAllNodes = listOf(
                    TestCordapp.findCordapp("cbdc.corda.contract"),
                    TestCordapp.findCordapp("cbdc.corda.flow")
                )
            )
        )
        nodeA = network.createPartyNode()
        nodeB = network.createPartyNode()
        nodeC = network.createPartyNode()
        network.runNetwork()
    }

    @AfterEach
    fun tearDown() {
        network.stopNodes()
    }

    fun <T : Any> StartedMockNode.runFlow(flowLogic: FlowLogic<T>): T {
        val future = startFlow(flowLogic)
        network.runNetwork()
        return future.getOrThrow()
    }

    fun TransactionBuilder.signedBy(vararg nodes: StartedMockNode): SignedTransaction {
        var stx = nodes[0].services.signInitialTransaction(this)

        for (node in nodes.drop(1)) {
            stx += node.services.createSignature(stx)
        }

        stx += network.defaultNotaryNode.services.createSignature(stx)

        return stx
    }

    fun SignedTransaction.recordedBy(statesToRecord: StatesToRecord, vararg nodes: StartedMockNode): SignedTransaction = apply {
        for (node in nodes) {
            node.transaction {
                node.services.recordTransactions(statesToRecord, listOf(this))
            }
        }
        network.defaultNotaryNode.transaction {
            network.defaultNotaryNode.services.recordTransactions(this)
        }
    }

    fun TransactionBuilder.signedAndRecordedBy(vararg nodes: StartedMockNode): SignedTransaction =
        @Suppress("SpreadOperator")
        this.signedBy(*nodes).recordedBy(StatesToRecord.ALL_VISIBLE, *allPeers.toTypedArray())

    fun <T> StartedMockNode.transactionEx(block: StartedMockNode.() -> T) = this.transaction { block.invoke(this) }
}
