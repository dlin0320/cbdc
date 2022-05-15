package cbdc.corda.middleware.rest.api.payload

import cbdc.corda.state.generic.Amount
import net.corda.core.serialization.CordaSerializable
import java.math.BigDecimal

@CordaSerializable
data class Quantity(
    val quantity: BigDecimal
) {
    fun toAmount() = Amount.fromDecimal(quantity)
}
