package cbdc.corda.middleware.rest.api.unsupported

import gov.cbc.cbdc.utilities.client.SWalletClient
import gov.cbc.cbdc.utilities.domain.entity.account.NewSWallet
import gov.cbc.cbdc.utilities.domain.entity.account.SWallet
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping(SWalletClient.path)
class SWalletClientCordaImpl : SWalletClient {
    override fun enable(sWalletID: String?, newSWallet: NewSWallet?) {
        throw UnsupportedOperationException()
    }

    override fun get(sWalletID: String?): SWallet {
        throw UnsupportedOperationException()
    }

    override fun update(sWalletID: String?, newSWallet: NewSWallet?) {
        throw UnsupportedOperationException()
    }
}
