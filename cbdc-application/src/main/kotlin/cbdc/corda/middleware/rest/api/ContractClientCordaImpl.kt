package cbdc.corda.middleware.rest.api

import cbdc.corda.flow.AuthorizationFlow
import cbdc.corda.flow.util.StateNotFoundException
import cbdc.corda.flow.vault.CommonQueries.queryAuthorization
import cbdc.corda.middleware.core.service.CordaService
import cbdc.corda.middleware.rest.api.convert.toJava
import cbdc.corda.middleware.rest.api.convert.toKotlin
import cbdc.corda.middleware.utils.curry
import cbdc.corda.state.AuthorizationProps
import gov.cbc.cbdc.utilities.client.ContractClient
import gov.cbc.cbdc.utilities.domain.entity.authorization.Authorization
import org.springframework.web.bind.annotation.RestController

@RestController
class ContractClientCordaImpl(
    private val cordaService: CordaService
) : ContractClient {
    private fun getAuth(authID: String) =
        cordaService.rpcOps.queryAuthorization(authID) ?: throw StateNotFoundException(authID, "AuthorizationState")

    override fun add(authorization: Authorization) {
        cordaService.startFlow(
            ::AuthorizationFlow.curry(
                cbdc.corda.contract.Authorization.Commands.Authorize,
                authorization.toKotlin()
            )
        )
    }

    override fun queryByAuthorizedAgency(
        authorizedAgencyID: String,
        operation: String,
        authID: String
    ): MutableList<Authorization> {
        val auth = getAuth(authID)

        return mutableListOf(auth.toJava())
    }

    override fun queryBySender(senderID: String, operation: String, authID: String): MutableList<Authorization> {
        val auth = getAuth(authID)

        return mutableListOf(auth.toJava())
    }

    override fun get(operation: String, authID: String): Authorization {
        return getAuth(authID).toJava()
    }

    override fun query(
        senderID: String,
        authorizedAgencyID: String,
        recipientID: String,
        operation: String,
        authID: String
    ): MutableList<Authorization> {
        val auth = getAuth(authID)

        return mutableListOf(auth.toJava())
    }

    override fun cancel(operation: String, authID: String, voidRemark: String) {
        val props = AuthorizationProps(
            authID,
            operation,
            null,
            null,
            null,
            null,
            voidRemark
        )

        cordaService.startFlow(
            ::AuthorizationFlow.curry(
                cbdc.corda.contract.Authorization.Commands.Void,
                props
            )
        )
    }
}
