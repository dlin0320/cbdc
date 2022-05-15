package cbdc.corda.middleware.rest.api.unsupported

import gov.cbc.cbdc.utilities.client.SCBDCClient
import gov.cbc.cbdc.utilities.domain.entity.scbdc.Contract
import gov.cbc.cbdc.utilities.domain.entity.scbdc.NewContract
import gov.cbc.cbdc.utilities.domain.entity.scbdc.NewTime
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping(SCBDCClient.path)
class SCBDCClientCordaImpl : SCBDCClient {
    override fun add(cvc: String?, newContract: NewContract?) {
        throw UnsupportedOperationException()
    }

    override fun addIssuableID(cvc: String?, issuableIDs: MutableList<String>?) {
        throw UnsupportedOperationException()
    }

    override fun get(cvc: String?): Contract? {
        throw UnsupportedOperationException()
    }

    override fun removeIssuableID(cvc: String?, issuableIDs: MutableList<String>?) {
        throw UnsupportedOperationException()
    }

    override fun updateTime(cvc: String?, newTime: NewTime?) {
        throw UnsupportedOperationException()
    }
}
