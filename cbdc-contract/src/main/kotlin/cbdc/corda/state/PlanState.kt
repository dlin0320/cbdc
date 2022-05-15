package cbdc.corda.state

import cbdc.corda.contract.Plan
import cbdc.corda.schema.CBDCSchemaV1
import cbdc.corda.state.generic.BroadcastedState
import net.corda.core.contracts.BelongsToContract
import net.corda.core.schemas.MappedSchema
import net.corda.core.schemas.PersistentState
import net.corda.core.schemas.QueryableState
import java.lang.IllegalArgumentException

@BelongsToContract(Plan::class)
data class PlanState(
    val operation: String,
    val amountLimit: Long,
    val certAllow: Boolean,
    val keyAllow: Boolean,
    val remark: String
) : BroadcastedState, QueryableState {
    override fun generateMappedObject(schema: MappedSchema): PersistentState {
        return when (schema) {
            CBDCSchemaV1 -> CBDCSchemaV1.PersistentPlan(
                operation = operation,
                amountLimit = amountLimit,
                certAllow = certAllow,
                keyAllow = keyAllow,
                remark = remark
            )
            else -> throw IllegalArgumentException("Unsupported schema")
        }
    }

    override fun supportedSchemas() = listOf(CBDCSchemaV1)
}
