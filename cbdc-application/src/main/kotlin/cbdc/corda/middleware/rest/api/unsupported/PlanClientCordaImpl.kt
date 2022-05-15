package cbdc.corda.middleware.rest.api.unsupported

import cbdc.corda.flow.CreatePlanFlow
import cbdc.corda.flow.vault.CommonQueries.queryPlan
import cbdc.corda.middleware.core.service.CordaService
import cbdc.corda.middleware.rest.api.convert.toJava
import cbdc.corda.middleware.rest.api.convert.toKotlin
import cbdc.corda.middleware.utils.curry
import gov.cbc.cbdc.utilities.client.PlanClient
import gov.cbc.cbdc.utilities.domain.entity.plan.NewAgency
import gov.cbc.cbdc.utilities.domain.entity.plan.NewConfig
import gov.cbc.cbdc.utilities.domain.entity.plan.Plan
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping(PlanClient.path)
class PlanClientCordaImpl(
    private val cordaService: CordaService
) : PlanClient {
    override fun add(plan: Plan) {
        cordaService.startFlow(::CreatePlanFlow.curry(plan.toKotlin()))
    }

    override fun addAuthorizedAgencyID(operation: String?, newAgency: NewAgency?) {
        throw UnsupportedOperationException()
    }

    override fun addDeductTriggerIDs(operation: String?, deductTriggerIDs: MutableList<String>?) {
        throw UnsupportedOperationException()
    }

    override fun disable(operation: String?) {
        throw UnsupportedOperationException()
    }

    override fun get(operation: String): Plan? {
        return cordaService.rpcOps.queryPlan(operation)?.toJava()
    }

    override fun removeAuthorizedAgencyID(operation: String?, newAgency: NewAgency?) {
        throw UnsupportedOperationException()
    }

    override fun removeDeductTriggerIDs(operation: String?, deductTriggerIDs: MutableList<String>?) {
        throw UnsupportedOperationException()
    }

    override fun updateAmountLimit(operation: String?, newAmountLimit: Long?) {
        throw UnsupportedOperationException()
    }

    override fun updateConfig(operation: String?, newConfig: NewConfig?) {
        throw UnsupportedOperationException()
    }
}
