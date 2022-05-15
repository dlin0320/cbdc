package cbdc.corda.middleware.rest.api

import cbdc.corda.contract.Instruction
import cbdc.corda.flow.TransferFlow
import cbdc.corda.middleware.core.service.CordaService
import cbdc.corda.middleware.rest.api.convert.toTransferProps
import cbdc.corda.middleware.utils.curry
import cbdc.corda.state.generic.Amount
import gov.cbc.cbdc.utilities.client.TransactionClient
import gov.cbc.cbdc.utilities.domain.entity.TransferAttribute
import gov.cbc.cbdc.utilities.domain.entity.TransferTransaction
import net.corda.core.utilities.loggerFor
import org.springframework.web.bind.annotation.RestController

@RestController
class TransactionClientCordaImpl(
    private val cordaService: CordaService
) : TransactionClient {
    private val logger = loggerFor<TransactionClientCordaImpl>()

    override fun transfer(
        txnID: String,
        attr: TransferAttribute
    ) = transfer(Instruction.Events.TRANSFER, txnID, attr)

    override fun issue(
        txnID: String,
        attr: TransferAttribute
    ) = transfer(Instruction.Events.ISSUE, txnID, attr)

    override fun redeem(
        txnID: String,
        attr: TransferAttribute
    ) = transfer(Instruction.Events.REDEEM, txnID, attr)

    override fun deduct(txnID: String?, transferAttribute: TransferAttribute?): TransferTransaction {
        TODO("Not yet implemented")
    }

    private fun transfer(
        event: Instruction.Events,
        txnId: String,
        attr: TransferAttribute
    ): TransferTransaction {
        return try {
            val ins = cordaService.startFlow(
                ::TransferFlow.curry(
                    event,
                    txnId,
                    attr.senderID,
                    attr.recipientID,
                    Amount.fromLong(attr.amount),
                    attr.toTransferProps()
                )
            ).returnValue.get()

            TransferTransaction(
                txnId,
                "S",
                ins.timestamp
            )
        } catch (ex: Exception) {
            logger.error("event: $event, id: $txnId, ex: $ex")
            TransferTransaction(
                txnId,
                "F",
                null
            )
        }
    }
}
