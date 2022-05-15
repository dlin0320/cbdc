package cbdc.corda.state

import cbdc.corda.contract.Token
import cbdc.corda.schema.CBDCSchemaV1
import cbdc.corda.state.generic.AddressId
import cbdc.corda.state.generic.Amount
import cbdc.corda.state.generic.BroadcastedState
import cbdc.corda.state.generic.TokenAmount
import net.corda.core.contracts.BelongsToContract
import net.corda.core.internal.sumByLong
import net.corda.core.schemas.MappedSchema
import net.corda.core.schemas.PersistentState
import net.corda.core.schemas.QueryableState

@BelongsToContract(Token::class)
data class TokenState(
    val owner: AddressId,
    val amount: Amount<String>
) : BroadcastedState, QueryableState {
    override fun generateMappedObject(schema: MappedSchema): PersistentState {
        return when (schema) {
            CBDCSchemaV1 -> CBDCSchemaV1.PersistentToken(
                owner = owner,
                quantity = amount.quantity,
                token = amount.token
            )
            else -> throw IllegalArgumentException("Unsupported schema")
        }
    }

    override fun supportedSchemas() = listOf(CBDCSchemaV1)
}

fun Collection<TokenState>.sumOrZero(
    token: String,
    isDifferentWalletAllowed: Boolean = false
): TokenAmount {
    val oneToken = firstOrNull() ?: return Amount.zero(token)
    if (!isDifferentWalletAllowed && !all { it.owner == oneToken.owner })
        throw IllegalArgumentException("tokens are of the same wallet: ${oneToken.owner}")
    return map { it.amount }.sumOrZero(token)
}

fun Collection<TokenAmount>.sumOrZero(
    token: String
): TokenAmount {
    val oneToken = firstOrNull() ?: return Amount.zero(token)
    if (!all { it.token == oneToken.token })
        throw IllegalArgumentException("amounts are of the same type: ${oneToken.token}")
    val sumQuantity = sumByLong { it.quantity }
    return first().copy(quantity = sumQuantity)
}
