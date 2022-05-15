package cbdc.corda.flow

import co.paralleluniverse.fibers.Suspendable
import net.corda.core.crypto.isFulfilledBy
import net.corda.core.flows.FlowLogic
import net.corda.core.flows.FlowSession
import net.corda.core.flows.NotaryException
import net.corda.core.flows.NotaryFlow
import net.corda.core.flows.SendTransactionFlow
import net.corda.core.flows.UnexpectedFlowEndException
import net.corda.core.node.StatesToRecord
import net.corda.core.transactions.LedgerTransaction
import net.corda.core.transactions.SignedTransaction
import net.corda.core.utilities.ProgressTracker

object MyFinalityFlow {
    class Notarise(
        private val transaction: SignedTransaction
    ) : FlowLogic<SignedTransaction>() {
        override val progressTracker = tracker()

        companion object {
            object NOTARISING : ProgressTracker.Step("Requesting signature by notary service") {
                override fun childProgressTracker() = NotaryFlow.Client.tracker()
            }

            @JvmStatic
            fun tracker() = ProgressTracker(NOTARISING)
        }

        @Suspendable
        @Throws(NotaryException::class)
        override fun call(): SignedTransaction {
            verifyTx()
            return notariseAndRecord()
        }

        @Suspendable
        private fun notariseAndRecord(): SignedTransaction {
            val notarised = if (needsNotarySignature(transaction)) {
                progressTracker.currentStep = NOTARISING
                val notarySignatures = subFlow(NotaryFlow.Client(transaction, skipVerification = true))
                transaction + notarySignatures
            } else {
                logger.info("No need to notarise this transaction.")
                transaction
            }
            logger.info("Recording transaction locally.")
            serviceHub.recordTransactions(StatesToRecord.ALL_VISIBLE, listOf(notarised))
            logger.info("Recorded transaction locally successfully.")
            return notarised
        }

        private fun needsNotarySignature(stx: SignedTransaction): Boolean {
            val wtx = stx.tx
            val needsNotarisation = wtx.inputs.isNotEmpty() || wtx.references.isNotEmpty() || wtx.timeWindow != null
            return needsNotarisation && hasNoNotarySignature(stx)
        }

        private fun hasNoNotarySignature(stx: SignedTransaction): Boolean {
            val notaryKey = stx.tx.notary?.owningKey
            val signers = stx.sigs.asSequence().map { it.by }.toSet()
            return notaryKey?.isFulfilledBy(signers) != true
        }

        private fun verifyTx(): LedgerTransaction {
            val notary = transaction.tx.notary
            // The notary signature(s) are allowed to be missing but no others.
            if (notary != null) transaction.verifySignaturesExcept(notary.owningKey) else transaction.verifyRequiredSignatures()
            // TODO= [CORDA-3267] Remove duplicate signature verification
            val ltx = transaction.toLedgerTransaction(serviceHub, false)
            ltx.verify()
            return ltx
        }
    }

    class Broadcast(
        private val transaction: SignedTransaction,
        private val sessions: Collection<FlowSession>
    ) : FlowLogic<Unit>() {
        override val progressTracker = tracker()

        companion object {
            object BROADCASTING : ProgressTracker.Step("Broadcasting transaction to participants")

            @JvmStatic
            fun tracker() = ProgressTracker(BROADCASTING)
        }

        @Suspendable
        @Throws(NotaryException::class)
        override fun call() {
            require(sessions.none { serviceHub.myInfo.isLegalIdentity(it.counterparty) }) {
                "Do not provide flow sessions for the local node. FinalityFlow will record the notarised transaction locally."
            }
            broadcast(transaction)
        }

        @Suspendable
        private fun broadcast(stx: SignedTransaction) {
            progressTracker.currentStep = BROADCASTING

            for (session in sessions) {
                try {
                    subFlow(SendTransactionFlow(session, stx))
                    logger.info("Party ${session.counterparty} received the transaction.")
                } catch (e: UnexpectedFlowEndException) {
                    throw UnexpectedFlowEndException(
                        "${session.counterparty} has finished prematurely and we're trying to send them the finalised transaction. " +
                            "Did they forget to call ReceiveFinalityFlow? (${e.message})",
                        e.cause,
                        e.originalErrorId
                    )
                }
            }

            logger.info("All parties received the transaction successfully.")
        }
    }
}
