package cbdc.corda.contract

import cbdc.corda.contract.generic.BaseContract
import cbdc.corda.state.PlanState
import net.corda.core.contracts.TypeOnlyCommandData
import net.corda.core.transactions.LedgerTransaction

class Plan : BaseContract<PlanState, Plan.Commands>(PlanState::class, Commands::class) {
    companion object {
        val name = Plan::class.qualifiedName!!
    }

    sealed class Commands : TypeOnlyCommandData() {
        object Create : Commands()
    }

    override fun verify(tx: LedgerTransaction) {
        TODO("Not yet implemented")
    }
}
