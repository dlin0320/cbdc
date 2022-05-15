package cbdc.corda.flow

import net.corda.core.crypto.SecureHash
import net.corda.core.flows.StartableByRPC
import net.corda.core.transactions.SignedTransaction

@StartableByRPC
class QueryTransactionFlow(private val txId: String) : BaseTxFlow() {
    override fun call(): SignedTransaction {
        return serviceHub.validatedTransactions.getTransaction(SecureHash.parse(txId))!!
    }
}
