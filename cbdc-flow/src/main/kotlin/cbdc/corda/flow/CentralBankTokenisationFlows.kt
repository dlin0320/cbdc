package cbdc.corda.flow

import cbdc.corda.contract.Instruction
import cbdc.corda.contract.Token
import cbdc.corda.flow.generic.TokenSelectionStrategy
import cbdc.corda.state.CENTRAL_BANK_VAULT_ID
import cbdc.corda.state.InstructionState
import cbdc.corda.state.MintAndBurnProps
import cbdc.corda.state.TokenState
import cbdc.corda.state.VaultType
import cbdc.corda.state.generic.TokenAmount
import co.paralleluniverse.fibers.Suspendable
import net.corda.core.flows.StartableByRPC
import net.corda.core.transactions.SignedTransaction
import net.corda.core.transactions.TransactionBuilder
import net.corda.core.utilities.OpaqueBytes
import net.corda.core.utilities.toNonEmptySet

object CentralBankTokenisationFlows {
    @StartableByRPC
    class Mint(
        private val txnId: String,
        private val amount: TokenAmount,
        private val props: MintAndBurnProps? = null
    ) : BaseTxFlow() {
        @Suspendable
        override fun call(): SignedTransaction {
            val timeWindow = preferredTimeWindow
            val tx = TransactionBuilder(notary)
                .setTimeWindow(timeWindow)
                .addCommand(Instruction.Commands.Mint, myParty.owningKey)
                .addOutputState(
                    InstructionState(
                        event = Instruction.Events.MINT,
                        txnId = txnId,
                        timestamp = timeWindow.midpoint!!,
                        initiatorId = CENTRAL_BANK_VAULT_ID,
                        initiatorType = VaultType.CENTRAL_BANK,
                        counterPartyId = null,
                        amount = amount,
                        signature = OpaqueBytes.of(0), // todo: store signature
                        props = props
                    )
                )
                .addCommand(Token.Commands.Mint, myParty.owningKey)
                .addOutputState(
                    TokenState(
                        owner = CENTRAL_BANK_VAULT_ID,
                        amount = amount
                    )
                )

            val ptx = serviceHub.signInitialTransaction(tx)
            return subFlow(BroadcastFinalityFlow.Initiator(ptx))
        }
    }

    @StartableByRPC
    class Burn(
        private val txnId: String,
        private val amount: TokenAmount,
        private val props: MintAndBurnProps? = null
    ) : BaseTxFlow() {
        @Suspendable
        override fun call(): SignedTransaction {
            val (coins, residue) = TokenSelectionStrategy.gatherCoins(serviceHub, CENTRAL_BANK_VAULT_ID, amount)

            serviceHub.vaultService.softLockReserve(runId.uuid, coins.map { it.ref }.toNonEmptySet())
            val timeWindow = preferredTimeWindow
            val tx = TransactionBuilder(notary)
                .setTimeWindow(timeWindow)
                .addCommand(Instruction.Commands.Burn, myParty.owningKey)
                .addOutputState(
                    InstructionState(
                        event = Instruction.Events.BURN,
                        txnId = txnId,
                        timestamp = timeWindow.midpoint!!,
                        initiatorId = CENTRAL_BANK_VAULT_ID,
                        initiatorType = VaultType.CENTRAL_BANK,
                        counterPartyId = null,
                        amount = amount,
                        signature = OpaqueBytes.of(0), // todo: store signature
                        props = props
                    )
                )

            tx.addCommand(Token.Commands.Burn, myParty.owningKey)
            for (c in coins)
                tx.addInputState(c)
            if (residue.quantity > 0) {
                tx.addOutputState(
                    TokenState(
                        owner = CENTRAL_BANK_VAULT_ID,
                        amount = residue
                    )
                )
            }

            val ptx = serviceHub.signInitialTransaction(tx)
            return subFlow(BroadcastFinalityFlow.Initiator(ptx))
        }
    }
}
