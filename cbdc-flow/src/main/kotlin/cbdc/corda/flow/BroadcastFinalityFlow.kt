package cbdc.corda.flow

import cbdc.corda.flow.util.evalTime
import cbdc.corda.flow.util.measureTime
import co.paralleluniverse.fibers.Suspendable
import net.corda.core.flows.FlowLogic
import net.corda.core.flows.FlowSession
import net.corda.core.flows.InitiatedBy
import net.corda.core.flows.InitiatingFlow
import net.corda.core.flows.ReceiveFinalityFlow
import net.corda.core.node.StatesToRecord
import net.corda.core.transactions.SignedTransaction
import net.corda.core.utilities.unwrap
import java.io.File

object BroadcastFinalityFlow {
    val file = File("./transfer_timestamps.txt")

    @InitiatingFlow
    class Initiator(
        private val stx: SignedTransaction
    ) : FlowLogic<SignedTransaction>() {
        @Suspendable
        override fun call(): SignedTransaction {
            val sb = StringBuilder().append("(${ourIdentity.name}) BroadcastFinalityFlow.Initiator- ")
            // Notarise only
            val notarised = measureTime {
                subFlow(MyFinalityFlow.Notarise(stx))
            }.evalTime {
                sb.append("notarise: $it")
                file.appendText("notarise: $it")
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
            val sb = StringBuilder().append("(${ourIdentity.name}) BroadcastFinalityFlow.Broadcaster- ")
            // Get all parties' sessions
            val peers = serviceHub.networkMapCache.allNodes.map { it.legalIdentities.first() } -
                serviceHub.networkMapCache.notaryIdentities -
                serviceHub.myInfo.legalIdentities.first()
            val peerSessions = peers.map(::initiateFlow)

            // Broadcast and record the transaction in all parties' vaults
            val stx = otherPartySession.receive<SignedTransaction>().unwrap { it }
            measureTime {
                subFlow(MyFinalityFlow.Broadcast(stx, peerSessions))
            }.evalTime {
                sb.append("broadcaster: $it")
                file.appendText("broadcaster: $it, ")
            }

            logger.info(sb.toString())
        }
    }

    @InitiatedBy(Broadcaster::class)
    class Audience(
        private val otherPartySession: FlowSession
    ) : FlowLogic<SignedTransaction>() {
        @Suspendable
        override fun call(): SignedTransaction {
            val sb = StringBuilder().append("(${ourIdentity.name}) BroadcastFinalityFlow.Audience- ")
            return measureTime {
                subFlow(
                    ReceiveFinalityFlow(
                        otherSideSession = otherPartySession,
                        expectedTxId = null,
                        statesToRecord = StatesToRecord.ALL_VISIBLE
                    )
                )
            }.evalTime {
                sb.append("audience: $it")
                logger.info(sb.toString())
            }
        }
    }
}
