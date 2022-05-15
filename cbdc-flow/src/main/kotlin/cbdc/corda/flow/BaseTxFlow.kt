package cbdc.corda.flow

import net.corda.core.contracts.TimeWindow
import net.corda.core.flows.FlowLogic
import net.corda.core.transactions.SignedTransaction
import net.corda.core.utilities.seconds

abstract class BaseTxFlow : BaseFlow<SignedTransaction>()

abstract class BaseFlow<T : Any> : FlowLogic<T>() {
    protected val notary
        get() = serviceHub.networkMapCache.notaryIdentities.single()
    protected val preferredTimeWindow
        get() = TimeWindow.withTolerance(serviceHub.clock.instant(), 30.seconds)
    protected val myParty
        get() = serviceHub.myInfo.legalIdentities.first()
    protected val dummySession
        get() = initiateFlow(notary)
}
