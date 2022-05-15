package cbdc.corda.state.generic

import net.corda.core.contracts.ContractState
import net.corda.core.identity.AbstractParty

interface BroadcastedState : ContractState {
    override val participants: List<AbstractParty> get() = emptyList()
}
