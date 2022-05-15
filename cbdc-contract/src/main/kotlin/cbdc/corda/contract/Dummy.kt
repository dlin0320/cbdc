package cbdc.corda.contract

import cbdc.corda.contract.generic.BaseContract
import cbdc.corda.state.AuthorizationState
import net.corda.core.contracts.TypeOnlyCommandData
import net.corda.core.transactions.LedgerTransaction

class Dummy : BaseContract<AuthorizationState, Dummy.Commands>(AuthorizationState::class, Commands::class) {
    companion object {
        val name = Dummy::class.qualifiedName!!
    }

    sealed class Commands : TypeOnlyCommandData() {
        object Command : Commands()
    }

    override fun verify(tx: LedgerTransaction) {
    }
}
