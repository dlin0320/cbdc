package cbdc.corda.state

import cbdc.corda.contract.Authorization
import cbdc.corda.schema.CBDCSchemaV1
import cbdc.corda.state.generic.AddressId
import cbdc.corda.state.generic.BroadcastedState
import net.corda.core.contracts.BelongsToContract
import net.corda.core.schemas.MappedSchema
import net.corda.core.schemas.PersistentState
import net.corda.core.schemas.QueryableState
import net.corda.core.serialization.CordaSerializable
import java.lang.IllegalArgumentException
import java.time.Instant

typealias AuthId = String

@BelongsToContract(Authorization::class)
data class AuthorizationState(
    val authId: AuthId,
    val operation: String,
    val senderId: AddressId,
    val authorizedAgencyId: String,
    val recipientId: AddressId,
    val remark: String,
    val voidRemark: String,
    val status: Boolean,
    val createTime: Instant
) : BroadcastedState, QueryableState {
    companion object {
        fun fromProps(
            props: AuthorizationProps,
            voidRemark: String,
            createTime: Instant,
            status: Boolean
        ) = AuthorizationState(
            authId = props.authId,
            operation = props.operation ?: "",
            senderId = props.senderId ?: "",
            authorizedAgencyId = props.authorizedAgencyId ?: "",
            recipientId = props.recipientId ?: "",
            remark = props.remark ?: "",
            voidRemark = voidRemark,
            status = status,
            createTime = createTime
        )
    }

    override fun generateMappedObject(schema: MappedSchema): PersistentState {
        return when (schema) {
            CBDCSchemaV1 -> CBDCSchemaV1.PersistentAuthorization(
                authId = authId,
                operation = operation,
                senderId = senderId,
                authorizedAgencyId = authorizedAgencyId,
                recipientId = recipientId,
                remark = remark,
                voidRemark = voidRemark,
                status = status,
                createTime = createTime
            )
            else -> throw IllegalArgumentException("Unsupported schema")
        }
    }

    override fun supportedSchemas() = listOf(CBDCSchemaV1)
}

@CordaSerializable
data class AuthorizationProps(
    val authId: AuthId,
    val operation: String?,
    val senderId: AddressId?,
    val authorizedAgencyId: String?,
    val recipientId: AddressId?,
    val remark: String?,
    val voidRemark: String?
)
