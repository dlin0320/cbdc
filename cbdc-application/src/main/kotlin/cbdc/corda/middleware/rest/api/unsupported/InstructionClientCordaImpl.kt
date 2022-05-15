package cbdc.corda.middleware.rest.api.unsupported

import gov.cbc.cbdc.utilities.client.InstructionClient
import gov.cbc.cbdc.utilities.domain.entity.InsProcResult
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping(InstructionClient.path)
class InstructionClientCordaImpl : InstructionClient {
    override fun add(insID: String?, request: MutableMap<String, Any>?) {
        throw UnsupportedOperationException()
    }

    override fun get(insID: String?): InsProcResult? {
        throw UnsupportedOperationException()
    }

    override fun update(insID: String?, result: String?) {
        throw UnsupportedOperationException()
    }
}
