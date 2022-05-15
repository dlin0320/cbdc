package cbdc.corda.state

import cbdc.corda.contract.Number
import cbdc.corda.schema.CBDCSchemaV1
import net.corda.core.contracts.BelongsToContract
import net.corda.core.identity.AbstractParty
import net.corda.core.schemas.MappedSchema
import net.corda.core.schemas.PersistentState
import net.corda.core.schemas.QueryableState
import net.corda.core.serialization.CordaSerializable
import java.time.Instant

@BelongsToContract(Number::class)
data class NumberState(
    val id: String,
    val walletId: String,
    val cvc: String,
    val amount: Long,
    val enabled: Boolean,
    val createTime: Instant
) : QueryableState {

    override val participants: List<AbstractParty>
        get() = listOf()

    override fun generateMappedObject(schema: MappedSchema): PersistentState {
        return when (schema) {
            CBDCSchemaV1 -> CBDCSchemaV1.PersistentNumber(id, walletId, cvc, amount, enabled, createTime)
            else -> throw IllegalArgumentException("Unsupported schema")
        }
    }

    override fun supportedSchemas() = listOf(CBDCSchemaV1)
}

@CordaSerializable
data class NewNumberDataK(
    val walletId: String?,
    val cvc: String?,
    val amount: Long?
)
