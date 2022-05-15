package cbdc.corda.state

import cbdc.corda.contract.Dummy
import cbdc.corda.schema.CBDCSchemaV1
import cbdc.corda.state.generic.BroadcastedState
import net.corda.core.contracts.BelongsToContract
import net.corda.core.schemas.MappedSchema
import net.corda.core.schemas.PersistentState
import net.corda.core.schemas.QueryableState

@BelongsToContract(Dummy::class)
data class DummyState(
    val id: String
) : BroadcastedState, QueryableState {
    override fun generateMappedObject(schema: MappedSchema): PersistentState {
        return when (schema) {
            is CBDCSchemaV1 -> CBDCSchemaV1.PersistentDummy(id)
            else -> throw IllegalArgumentException("Unsupported schema")
        }
    }

    override fun supportedSchemas(): Iterable<MappedSchema> = listOf(CBDCSchemaV1)
}
