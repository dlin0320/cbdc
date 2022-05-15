package cbdc.corda.flow

import cbdc.corda.contract.Instruction
import cbdc.corda.contract.Token
import cbdc.corda.flow.generic.TokenSelectionStrategy
import cbdc.corda.flow.vault.CommonQueries.queryBalance
import cbdc.corda.flow.vault.CommonQueries.queryWalletBalanceLimit
import cbdc.corda.state.InstructionState
import cbdc.corda.state.TokenState
import cbdc.corda.state.TransferProps
import cbdc.corda.state.VaultType
import cbdc.corda.state.generic.AddressId
import cbdc.corda.state.generic.TokenAmount
import co.paralleluniverse.fibers.Suspendable
import net.corda.core.contracts.requireThat
import net.corda.core.flows.StartableByRPC
import net.corda.core.transactions.TransactionBuilder
import net.corda.core.utilities.OpaqueBytes
import net.corda.core.utilities.toNonEmptySet
import java.io.File
import java.time.Instant

@StartableByRPC
class TransferFlow(
    private val event: Instruction.Events,
    private val txnId: String,
    private val fromAddress: AddressId,
    private val toAddress: AddressId,
    private val amount: TokenAmount,
    private val props: TransferProps? = null
) : BaseFlow<InstructionState>() {
    private val now: Long
        get() = Instant.now().toEpochMilli()

    @Suspendable
    override fun call(): InstructionState {
        val file = File("./transfer_timestamps.txt")
        file.appendText("\nfrom: $fromAddress, to: $toAddress, ")
        val start = now
        requireThat {
            val sBalance = now
            val receiverBalance = serviceHub.queryBalance(toAddress)
            val eBalance = now
            val sLimit = now
            val (doesExist, balanceLimit) = serviceHub.queryWalletBalanceLimit(toAddress)
            val eLimit = now
            "receiver must exist" using doesExist
            "receiver balance limit must not exceed" using (balanceLimit == null || receiverBalance.quantity + amount.quantity <= balanceLimit)
            file.appendText("get balance: ${eBalance - sBalance}, get limit: ${eLimit - sLimit}")
        }
        val sCoins = now
        val (coins, residue) = TokenSelectionStrategy.gatherCoins(serviceHub, fromAddress, amount)
        val eCoins = now

        file.appendText(", get coins: ${eCoins - sCoins}, ")

        val command = when (event) {
            Instruction.Events.ISSUE -> Instruction.Commands.Issue
            Instruction.Events.REDEEM -> Instruction.Commands.Redeem
            Instruction.Events.TRANSFER -> Instruction.Commands.Transfer
            else -> throw IllegalArgumentException("Event type is not a transfer: $event.")
        }

        serviceHub.vaultService.softLockReserve(runId.uuid, coins.map { it.ref }.toNonEmptySet())
        val timeWindow = preferredTimeWindow
        val initiatorType = when {
            fromAddress.contains('B') -> VaultType.COMMERCIAL_BANK
            fromAddress.contains('F') -> VaultType.MERCHANT
            else -> VaultType.CENTRAL_BANK
        }
        val tx = TransactionBuilder(notary)
            .setTimeWindow(timeWindow)
            .addCommand(command, myParty.owningKey)
            .addOutputState(
                InstructionState(
                    event = event,
                    txnId = txnId,
                    timestamp = timeWindow.midpoint!!,
                    initiatorId = fromAddress,
                    counterPartyId = toAddress,
                    initiatorType = initiatorType,
                    amount = amount,
                    signature = OpaqueBytes.of(0), // todo: store signature
                    props = props
                )
            )

        tx.addCommand(Token.Commands.Move, myParty.owningKey)
        for (c in coins)
            tx.addInputState(c)
        tx.addOutputState(TokenState(owner = toAddress, amount = amount))
        if (residue.quantity > 0) {
            tx.addOutputState(TokenState(owner = fromAddress, amount = residue))
        }

        val ptx = serviceHub.signInitialTransaction(tx)

        val res = subFlow(SimpleBroadcastFinalityFlow.Initiator(ptx))
            .coreTransaction
            .outputsOfType(InstructionState::class.java)
            .single()

        val finish = now

        file.appendText(", total: ${finish - start}, ")

        return res
    }
}
