package cbdc.corda.flow

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class QueryTransactionFlowTest : CBDCFlowTest() {
    @Test
    fun happy() {
        val stx = nodeA.runFlow(InitialiseCBVaultFlow(NewVaultK(null, null)))
        val txId = stx.id.toString()

        assertEquals(nodeA.runFlow(QueryTransactionFlow(txId)), stx)
    }
}
