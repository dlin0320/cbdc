package cbdc.corda.middleware.rest.api.unsupported

import gov.cbc.cbdc.utilities.client.STransactionClient
import gov.cbc.cbdc.utilities.domain.entity.TransferAttribute
import gov.cbc.cbdc.utilities.domain.entity.TransferTransaction
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping(STransactionClient.path)
class STransactionClientCordaImpl : STransactionClient {
    override fun create(sVaultID: String?, txnID: String?, transferAttribute: TransferAttribute?): TransferTransaction {
        throw UnsupportedOperationException()
    }

    override fun redeem(txnID: String?, transferAttribute: TransferAttribute?): TransferTransaction {
        throw UnsupportedOperationException()
    }

    override fun transfer(txnID: String?, transferAttribute: TransferAttribute?): TransferTransaction {
        throw UnsupportedOperationException()
    }
}
