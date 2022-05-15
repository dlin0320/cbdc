package cbdc.corda.middleware.rest.api.unsupported

import gov.cbc.cbdc.utilities.client.KycClient
import gov.cbc.cbdc.utilities.domain.entity.kyc.KYCRecord
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping(KycClient.path)
class KycClientCordaImpl : KycClient {
    override fun add(walletID: String?, kyc: MutableMap<String, Any>?) {
        throw UnsupportedOperationException()
    }

    override fun get(walletID: String?): KYCRecord? {
        throw UnsupportedOperationException()
    }

    override fun replace(walletID: String?, kyc: MutableMap<String, Any>?) {
        throw UnsupportedOperationException()
    }
}
