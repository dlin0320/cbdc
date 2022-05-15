package cbdc.corda.state.generic

import net.corda.core.contracts.LinearState

typealias AddressId = String

interface AddressState : LinearState {
    val id: AddressId
}
