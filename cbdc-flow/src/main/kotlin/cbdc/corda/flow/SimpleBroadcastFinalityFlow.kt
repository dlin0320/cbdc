package cbdc.corda.flow

import cbdc.corda.flow.util.evalTime
import cbdc.corda.flow.util.measureTime
import co.paralleluniverse.fibers.Suspendable
import net.corda.core.flows.FlowLogic
import net.corda.core.flows.FlowSession
import net.corda.core.flows.InitiatedBy
import net.corda.core.flows.InitiatingFlow
import net.corda.core.node.StatesToRecord
import net.corda.core.transactions.SignedTransaction
import net.corda.core.utilities.unwrap

object SimpleBroadcastFinalityFlow {

    @InitiatingFlow
    class Initiator(
        private val stx: SignedTransaction
    ) : FlowLogic<SignedTransaction>() {
        @Suspendable
        override fun call(): SignedTransaction {
            val sb = StringBuilder().append("(${ourIdentity.name}) SimpleBroadcastFinalityFlow.Initiator- ")
            // Notarise only
            val notarised = measureTime {
                subFlow(MyFinalityFlow.Notarise(stx))
            }.evalTime {
                sb.append("notarise: $it")
            }

            measureTime {
                initiateFlow(ourIdentity).send(notarised)
            }.evalTime { sb.append(", broadcast-initiation: $it") }

            logger.info(sb.toString())
            return notarised
        }
    }

    @InitiatingFlow
    @InitiatedBy(Initiator::class)
    class Broadcaster(
        private val otherPartySession: FlowSession
    ) : FlowLogic<Unit>() {
        @Suspendable
        override fun call() {
            val sb = StringBuilder().append("(${ourIdentity.name}) SimpleBroadcastFinalityFlow.Broadcaster- ")
            // Get all parties' sessions
            val peers = serviceHub.networkMapCache.allNodes.map { it.legalIdentities.first() } -
                serviceHub.networkMapCache.notaryIdentities -
                serviceHub.myInfo.legalIdentities.first()
            val peerSessions = peers.map(::initiateFlow)

            // Broadcast and record the transaction in all parties' vaults
            val stx = otherPartySession.receive<SignedTransaction>().unwrap { it }
            measureTime {
                sendAll(stx, peerSessions.toSet())
            }.evalTime {
                sb.append("broadcaster: $it")
            }

            logger.info(sb.toString())
        }
    }

    @InitiatedBy(Broadcaster::class)
    class Audience(
        private val otherPartySession: FlowSession
    ) : FlowLogic<Unit>() {
        @Suspendable
        override fun call() {
            val sb = StringBuilder().append("(${ourIdentity.name}) SimpleBroadcastFinalityFlow.Audience- ")
            val stx = measureTime {
                otherPartySession.receive<SignedTransaction>().unwrap { it }
            }.evalTime {
                sb.append("audience-receive: $it")
            }
            measureTime {
                serviceHub.recordTransactions(StatesToRecord.ALL_VISIBLE, listOf(stx))
            }.evalTime {
                sb.append("audience-record: $it")
            }
            logger.info(sb.toString())
        }
    }
}
