package cbdc.corda.flow

import cbdc.corda.contract.Number
import cbdc.corda.schema.CBDCSchemaV1
import cbdc.corda.state.NewNumberDataK
import cbdc.corda.state.NumberState
import co.paralleluniverse.fibers.Suspendable
import net.corda.core.flows.StartableByRPC
import net.corda.core.node.services.vault.QueryCriteria
import net.corda.core.node.services.vault.builder
import net.corda.core.transactions.SignedTransaction
import net.corda.core.transactions.TransactionBuilder
import java.time.Instant

@StartableByRPC
abstract class UpsertNumberFlow(
    private val id: String
) : BaseTxFlow() {

    abstract fun latestNumberState(number: NumberState?): NumberState

    @Suspendable
    override fun call(): SignedTransaction {
        val timeWindow = preferredTimeWindow

        val existingNumberState = serviceHub.vaultService.queryBy(
            contractStateType = NumberState::class.java,
            criteria = QueryCriteria.VaultCustomQueryCriteria(
                expression = builder { CBDCSchemaV1.PersistentNumber::id.equal(id) }
            )
        ).states.singleOrNull()

        val numberState = latestNumberState(existingNumberState?.state?.data)

        val tx = TransactionBuilder(notary)
            .setTimeWindow(timeWindow)
            .addOutputState(numberState)
        if (existingNumberState == null) {
            tx.addCommand(Number.Commands.Create, serviceHub.myInfo.legalIdentities.first().owningKey)
        } else {
            tx.addInputState(existingNumberState)
            tx.addCommand(Number.Commands.Delete, serviceHub.myInfo.legalIdentities.first().owningKey)
        }

        val ptx = serviceHub.signInitialTransaction(tx)

        return subFlow(BroadcastFinalityFlow.Initiator(ptx))
    }
}

@StartableByRPC
class DeleteNumberFlow(private val id: String) : UpsertNumberFlow(id) {
    override fun latestNumberState(number: NumberState?) = number!!.copy(enabled = false)
}

@StartableByRPC
class AddNumberFlow(private val id: String, private val newNumber: NewNumberDataK) : UpsertNumberFlow(id) {
    override fun latestNumberState(number: NumberState?) = NumberState(
        id = id,
        walletId = newNumber.walletId!!,
        cvc = newNumber.cvc!!,
        amount = newNumber.amount!!,
        enabled = true,
        createTime = Instant.now()
    )
}
