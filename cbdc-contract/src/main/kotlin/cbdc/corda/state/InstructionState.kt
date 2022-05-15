package cbdc.corda.state

import cbdc.corda.contract.Instruction
import cbdc.corda.schema.CBDCSchemaV1
import cbdc.corda.state.generic.BroadcastedState
import cbdc.corda.state.generic.TokenAmount
import com.fasterxml.jackson.annotation.JsonSubTypes
import com.fasterxml.jackson.annotation.JsonTypeInfo
import net.corda.core.contracts.BelongsToContract
import net.corda.core.schemas.MappedSchema
import net.corda.core.schemas.PersistentState
import net.corda.core.schemas.QueryableState
import net.corda.core.serialization.CordaSerializable
import net.corda.core.utilities.OpaqueBytes
import java.time.Instant

@BelongsToContract(Instruction::class)
data class InstructionState(
    val event: Instruction.Events,
    val txnId: String,
    val timestamp: Instant,
    val initiatorId: String,
    val initiatorType: VaultType,
    val counterPartyId: String?,
    val amount: TokenAmount,
    val signature: OpaqueBytes,
    val props: InstructionProps? = null
) : BroadcastedState, QueryableState {
    override fun supportedSchemas() = listOf(CBDCSchemaV1)

    override fun generateMappedObject(schema: MappedSchema): PersistentState {
        return when (schema) {
            is CBDCSchemaV1 -> CBDCSchemaV1.PersistentInstruction(
                txnId = txnId,
                senderId = initiatorId,
                // sender/receiver are CB/null when minting
                receiverId = counterPartyId,
                timestamp = timestamp
            )
            else -> throw IllegalArgumentException("Unsupported schema")
        }
    }
}

enum class InstructionPropType {
    MINT_AND_BURN,
    TRANSFER
}

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "docType", visible = true)
@JsonSubTypes(
    JsonSubTypes.Type(value = MintAndBurnProps::class, name = "MINT_AND_BURN"),
    JsonSubTypes.Type(value = TransferProps::class, name = "TRANSFER")
)
@CordaSerializable
sealed class InstructionProps {
    abstract val type: InstructionPropType
}

@CordaSerializable
data class TransferProps(
    val authorizedAgencyID: String?,
    val cvc: String?,
    val won: String?,
    val remark: String?,
    val paymentMethod: String?,
    val insID: String?
) : InstructionProps() {
    companion object {
        fun emptyProps() = TransferProps(null, null, null, null, null, null)
    }

    override val type = InstructionPropType.TRANSFER
}

@CordaSerializable
data class MintAndBurnProps(
    val remark: String?
) : InstructionProps() {
    override val type = InstructionPropType.MINT_AND_BURN
}
