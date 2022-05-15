package cbdc.corda.middleware.rest.api.payload

import cbdc.corda.state.generic.AddressId
import cbdc.corda.state.generic.Amount
import java.math.BigDecimal

data class WalletAndQuantity(
    val walletId: AddressId,
    val quantity: BigDecimal
) {
    fun toAmount() = Amount.fromDecimal(quantity)
}
