package cbdc.corda.contract

import cbdc.corda.contract.generic.BaseContract
import cbdc.corda.state.InstructionState
import cbdc.corda.state.MintAndBurnProps
import cbdc.corda.state.TransferProps
import net.corda.core.contracts.TypeOnlyCommandData
import net.corda.core.contracts.requireThat
import net.corda.core.serialization.CordaSerializable
import net.corda.core.transactions.LedgerTransaction

class Instruction : BaseContract<InstructionState, Instruction.Commands>(InstructionState::class, Commands::class) {
    override fun verify(tx: LedgerTransaction) {
        tx.verify {
            // token commands are always single, verified in Token.kt
            val tokenCmd = tx.commandsOfType(Token.Commands::class.java).first()
            val out = outs.first().state.data
            requireThat {
                "input instruction is empty" using ins.isEmpty()
                "output instruction is single" using outs.isSingle()
                "time window is set" using (tx.timeWindow?.midpoint != null)
                "timestamp matches time window" using (out.timestamp == tx.timeWindow!!.midpoint)
            }
            when (command?.value) {
                Commands.Mint -> {
                    requireThat {
                        "token command is Mint" using (tokenCmd.value is Token.Commands.Mint)
                        "instruction props is MintAndBurnProps" using ((out.props != null) implies (out.props is MintAndBurnProps))
                        "event is TRANSFER" using (out.event == Events.MINT)
                    }
                }
                Commands.Burn -> {
                    requireThat {
                        "token command is Burn" using (tokenCmd.value is Token.Commands.Burn)
                        "instruction props is MintAndBurnProps" using ((out.props != null) implies (out.props is MintAndBurnProps))
                        "event is TRANSFER" using (out.event == Events.BURN)
                    }
                }
                Commands.Issue -> {
                    requireThat {
                        "token command is Move" using (tokenCmd.value is Token.Commands.Move)
                        "instruction props is TransferProps" using ((out.props != null) implies (out.props is TransferProps))
                        "event is ISSUE" using (out.event == Events.ISSUE)
                    }
                }
                Commands.Redeem -> {
                    requireThat {
                        "token command is Move" using (tokenCmd.value is Token.Commands.Move)
                        "instruction props is TransferProps" using ((out.props != null) implies (out.props is TransferProps))
                        "event is REDEEM" using (out.event == Events.REDEEM)
                    }
                }
                Commands.Transfer -> {
                    requireThat {
                        "token command is Move" using (tokenCmd.value is Token.Commands.Move)
                        "instruction props is TransferProps" using ((out.props != null) implies (out.props is TransferProps))
                        "event is TRANSFER" using (out.event == Events.TRANSFER)
                    }
                }
                else -> {}
            }
        }
    }

    sealed class Commands : TypeOnlyCommandData() {
        object Mint : Commands()
        object Burn : Commands()
        object Issue : Commands()
        object Redeem : Commands()
        object Transfer : Commands()
    }

    @CordaSerializable
    enum class Events {
        MINT,
        BURN,
        ISSUE,
        REDEEM,
        TRANSFER,
    }
}
