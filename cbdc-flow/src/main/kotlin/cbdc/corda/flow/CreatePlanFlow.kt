package cbdc.corda.flow

import cbdc.corda.contract.Plan
import cbdc.corda.state.PlanState
import net.corda.core.flows.StartableByRPC
import net.corda.core.transactions.TransactionBuilder

@StartableByRPC
class CreatePlanFlow(private val plan: PlanState) : BaseFlow<PlanState>() {
    override fun call(): PlanState {
        val tx = TransactionBuilder(notary)
            .setTimeWindow(preferredTimeWindow)
            .addCommand(Plan.Commands.Create, myParty.owningKey)
            .addOutputState(plan)

        val ptx = serviceHub.signInitialTransaction(tx)
        return subFlow(BroadcastFinalityFlow.Initiator(ptx))
            .coreTransaction
            .outputsOfType(PlanState::class.java)
            .single()
    }
}
