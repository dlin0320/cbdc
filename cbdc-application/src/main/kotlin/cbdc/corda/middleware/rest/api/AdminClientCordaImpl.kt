package cbdc.corda.middleware.rest.api

import cbdc.corda.flow.CentralBankTokenisationFlows.Burn
import cbdc.corda.flow.CentralBankTokenisationFlows.Mint
import cbdc.corda.middleware.core.service.CordaService
import cbdc.corda.middleware.rest.api.convert.toMintAndBurnProps
import cbdc.corda.middleware.utils.curry
import cbdc.corda.state.generic.Amount
import gov.cbc.cbdc.utilities.client.AdminClient
import gov.cbc.cbdc.utilities.domain.entity.TransferAttribute
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping(AdminClient.path)
class AdminClientCordaImpl(
    private val cordaService: CordaService
) : AdminClient {
    override fun mint(txnID: String, attr: TransferAttribute) {
        cordaService.startFlow(
            ::Mint.curry(
                txnID,
                Amount.fromLong(attr.amount),
                attr.toMintAndBurnProps()
            )
        ).returnValue.get()
    }

    override fun burn(txnID: String, attr: TransferAttribute) {
        cordaService.startFlow(
            ::Burn.curry(
                txnID,
                Amount.fromLong(attr.amount),
                attr.toMintAndBurnProps()
            )
        ).returnValue.get()
    }
}
