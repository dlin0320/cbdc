package cbdc.corda.contract

import cbdc.corda.contract.generic.BaseContract
import cbdc.corda.state.NumberState
import net.corda.core.contracts.TypeOnlyCommandData
import net.corda.core.transactions.LedgerTransaction

class Number : BaseContract<NumberState, Number.Commands>(NumberState::class, Commands::class) {
    sealed class Commands : TypeOnlyCommandData() {
        object Create : Commands()
        object Delete : Commands()
    }

    override fun verify(tx: LedgerTransaction) {
        TODO("Not yet implemented")
    }
}
