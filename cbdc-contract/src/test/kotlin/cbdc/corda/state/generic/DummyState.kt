package cbdc.corda.state.generic

import cbdc.corda.contract.generic.DummyContract
import net.corda.core.contracts.BelongsToContract
import net.corda.core.contracts.ContractState
import net.corda.core.identity.AbstractParty

@BelongsToContract(DummyContract::class)
data class DummyState(
    val value: Int
) : ContractState {
    override val participants: List<AbstractParty> = listOf()
}
