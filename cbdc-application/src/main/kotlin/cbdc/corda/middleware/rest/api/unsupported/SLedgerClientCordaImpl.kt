package cbdc.corda.middleware.rest.api.unsupported

import gov.cbc.cbdc.utilities.client.SLedgerClient
import gov.cbc.cbdc.utilities.domain.entity.LedgerState
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping(SLedgerClient.path)
class SLedgerClientCordaImpl : SLedgerClient {
    override fun get(txnID: String?): LedgerState? {
        throw UnsupportedOperationException()
    }

    override fun get(sWalletID: String?, sn: Long?): LedgerState? {
        throw UnsupportedOperationException()
    }

    override fun getList(sWalletID: String?, startSN: Long?, endSN: Long?): MutableList<LedgerState> {
        throw UnsupportedOperationException()
    }

    override fun getList(sWalletID: String?, startTime: String?, endTime: String?): MutableList<LedgerState> {
        throw UnsupportedOperationException()
    }
}
