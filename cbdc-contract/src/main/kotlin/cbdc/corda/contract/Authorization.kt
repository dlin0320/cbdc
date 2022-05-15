package cbdc.corda.contract

import cbdc.corda.contract.generic.BaseContract
import cbdc.corda.state.AuthorizationState
import net.corda.core.contracts.TypeOnlyCommandData
import net.corda.core.transactions.LedgerTransaction

class Authorization : BaseContract<AuthorizationState, Authorization.Commands>(AuthorizationState::class, Commands::class) {
    companion object {
        val name = Authorization::class.qualifiedName!!
    }

    sealed class Commands : TypeOnlyCommandData() {
        object Authorize : Commands()
        object Void : Commands()
    }

    override fun verify(tx: LedgerTransaction) {
        TODO("Not yet implemented")
    }
}
