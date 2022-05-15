package cbdc.corda.flow

import cbdc.corda.contract.Authorization
import cbdc.corda.state.AuthorizationProps
import cbdc.corda.state.AuthorizationState
import co.paralleluniverse.fibers.Suspendable
import net.corda.core.flows.StartableByRPC
import net.corda.core.transactions.TransactionBuilder

@StartableByRPC
class AuthorizationFlow(
    private val cmd: Authorization.Commands,
    private val authorizationProps: AuthorizationProps
) : BaseFlow<AuthorizationState>() {
    @Suspendable
    override fun call(): AuthorizationState {
        val status = cmd == Authorization.Commands.Authorize

        val tx = TransactionBuilder(notary)
            .setTimeWindow(preferredTimeWindow)
            .addCommand(cmd, myParty.owningKey)
            .addOutputState(
                AuthorizationState.fromProps(authorizationProps, "", preferredTimeWindow.midpoint!!, status)
            )

        val ptx = serviceHub.signInitialTransaction(tx)
        return subFlow(BroadcastFinalityFlow.Initiator(ptx))
            .coreTransaction
            .outputsOfType(AuthorizationState::class.java)
            .single()
    }
}
