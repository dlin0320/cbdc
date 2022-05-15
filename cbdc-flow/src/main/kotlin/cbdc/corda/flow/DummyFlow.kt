package cbdc.corda.flow

import cbdc.corda.contract.Dummy
import cbdc.corda.state.DummyState
import co.paralleluniverse.fibers.Suspendable
import net.corda.core.flows.StartableByRPC
import net.corda.core.transactions.SignedTransaction
import net.corda.core.transactions.TransactionBuilder

@StartableByRPC
class DummyFlow(val id: String, val broadcast: Boolean, val notarise: Boolean) : BaseTxFlow() {
    @Suspendable
    override fun call(): SignedTransaction {
        val tx = TransactionBuilder(notary)
            .addOutputState(DummyState(id))
            .addCommand(Dummy.Commands.Command, myParty.owningKey)
        if (notarise) tx.setTimeWindow(preferredTimeWindow)

        val ptx = serviceHub.signInitialTransaction(tx)

        return if (broadcast) {
            subFlow(BroadcastFinalityFlow.Initiator(ptx))
        } else {
            subFlow(MyFinalityFlow.Notarise(ptx))
        }
    }
}

@StartableByRPC
class DummyFlowWithSimpleBroadcast(val id: String, val notarise: Boolean) : BaseTxFlow() {
    @Suspendable
    override fun call(): SignedTransaction {
        val tx = TransactionBuilder(notary)
            .addOutputState(DummyState(id))
            .addCommand(Dummy.Commands.Command, myParty.owningKey)
        if (notarise) tx.setTimeWindow(preferredTimeWindow)

        val ptx = serviceHub.signInitialTransaction(tx)

        return subFlow(SimpleBroadcastFinalityFlow.Initiator(ptx))
    }
}
