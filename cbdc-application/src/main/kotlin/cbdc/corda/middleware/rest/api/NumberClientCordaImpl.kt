package cbdc.corda.middleware.rest.api

import cbdc.corda.flow.AddNumberFlow
import cbdc.corda.flow.DeleteNumberFlow
import cbdc.corda.flow.vault.CommonQueries.queryNumber
import cbdc.corda.middleware.core.service.CordaService
import cbdc.corda.middleware.rest.api.convert.toJava
import cbdc.corda.middleware.rest.api.convert.toKotlin
import cbdc.corda.middleware.utils.curry
import gov.cbc.cbdc.utilities.client.NumberClient
import gov.cbc.cbdc.utilities.domain.entity.NewNumberData
import gov.cbc.cbdc.utilities.domain.entity.NumberData
import org.springframework.web.bind.annotation.RestController
import java.util.UUID

@RestController
class NumberClientCordaImpl(
    private val cordaService: CordaService
) : NumberClient {
    override fun getAndDelete(payableNumber: String): NumberData? {
        val number = cordaService.rpcOps.queryNumber(payableNumber)
        if (number != null) {
            cordaService.startFlow(::DeleteNumberFlow.curry(payableNumber))
        }

        return number?.toJava()
    }

    override fun add(newNumberData: NewNumberData): String {
        val id = UUID.randomUUID().toString()
        cordaService.startFlow(::AddNumberFlow.curry(id, newNumberData.toKotlin()))

        return id
    }
}
