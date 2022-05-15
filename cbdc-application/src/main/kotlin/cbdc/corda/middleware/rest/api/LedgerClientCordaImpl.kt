package cbdc.corda.middleware.rest.api

import cbdc.corda.flow.vault.CommonQueries.queryBalance
import cbdc.corda.middleware.core.service.CordaService
import cbdc.corda.schema.CBDCSchemaV1
import cbdc.corda.state.InstructionState
import cbdc.corda.state.MintAndBurnProps
import cbdc.corda.state.TransferProps
import gov.cbc.cbdc.utilities.client.LedgerClient
import gov.cbc.cbdc.utilities.domain.entity.LedgerState
import net.corda.core.node.services.vault.QueryCriteria
import net.corda.core.node.services.vault.builder
import org.springframework.web.bind.annotation.RestController
import java.time.Instant

@RestController
class LedgerClientCordaImpl(
    private val cordaService: CordaService
) : LedgerClient {
    override fun get(txnID: String): LedgerState? {
        val instruction = cordaService.rpcOps.vaultQueryByCriteria(
            QueryCriteria.VaultCustomQueryCriteria(
                expression = builder {
                    CBDCSchemaV1.PersistentInstruction::txnId.equal(txnID)
                }
            ),
            InstructionState::class.java
        ).states.singleOrNull()?.state?.data ?: return null

        return instructionToLedgerState(instruction)
    }

    override fun get(accountID: String?, sn: Long?): LedgerState? {
        throw UnsupportedOperationException()
    }

    override fun getList(accountID: String?, startSN: Long?, endSN: Long?): MutableList<LedgerState> {
        throw UnsupportedOperationException()
    }

    override fun getList(accountID: String, startTime: String, endTime: String): List<LedgerState> {
        val instructions = cordaService.rpcOps.vaultQueryByCriteria(
            QueryCriteria.VaultCustomQueryCriteria(
                expression = builder {
                    CBDCSchemaV1.PersistentInstruction::timestamp.between(Instant.parse(startTime), Instant.parse(endTime))
                }
            ) and (
                QueryCriteria.VaultCustomQueryCriteria(
                    expression = builder {
                        CBDCSchemaV1.PersistentInstruction::senderId.equal(accountID)
                    }
                ) or QueryCriteria.VaultCustomQueryCriteria(
                    expression = builder {
                        CBDCSchemaV1.PersistentInstruction::receiverId.equal(accountID)
                    }
                )
                ),
            InstructionState::class.java
        ).states.map { it.state.data }

        return instructions.map(::instructionToLedgerState)
    }

    private fun instructionToLedgerState(instruction: InstructionState): LedgerState {
        return with(instruction) {
            val baseBuilder = LedgerState.builder()
                .id(txnId)
                .txnTime(timestamp)
                .amount(amount.quantity)
                .senderID(initiatorId)
                .senderBalance(cordaService.rpcOps.queryBalance(initiatorId).quantity)
                .recipientID(counterPartyId)
                .recipientBalance(counterPartyId?.let { cordaService.rpcOps.queryBalance(it).quantity })
//                .recipientDN()
                // hardcoded serial numbers
                .senderSN(0)
                .recipientSN(0)
                // hardcoded retry count
                .retry(0)
                // successful transaction
                .result("S")
            when (event) {
                cbdc.corda.contract.Instruction.Events.ISSUE,
                cbdc.corda.contract.Instruction.Events.REDEEM,
                cbdc.corda.contract.Instruction.Events.TRANSFER -> {
                    val props = props as TransferProps
                    baseBuilder
                        .authorizedAgencyID(props.authorizedAgencyID)
                        .cvc(props.cvc)
                        .won(props.won)
                        .remark(props.remark)
                        .paymentMethod(props.paymentMethod)
                        .insID(props.insID)
                        .build()
                }
                cbdc.corda.contract.Instruction.Events.MINT,
                cbdc.corda.contract.Instruction.Events.BURN -> {
                    val props = props as MintAndBurnProps
                    baseBuilder
                        .remark(props.remark)
                        .build()
                }
            }
        }
    }
}
