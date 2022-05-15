package cbdc.corda.contract.generic

import cbdc.corda.state.generic.Amount
import net.corda.core.contracts.Command
import net.corda.core.contracts.CommandData
import net.corda.core.contracts.Contract
import net.corda.core.contracts.ContractState
import net.corda.core.contracts.StateAndRef
import net.corda.core.transactions.LedgerTransaction
import kotlin.reflect.KClass

abstract class BaseContract<STATE : ContractState, CMD : CommandData>(
    private val stateKlazz: KClass<STATE>,
    private val cmdKlazz: KClass<CMD>
) : Contract {
    fun Collection<*>.isSingle() = size == 1
    fun Amount<*>.isPositive() = quantity > 0
    fun LedgerTransaction.verify(block: Verify.() -> Unit) {
        Verify(this).apply {
            if ((ins.isNotEmpty() || outs.isNotEmpty()) && command == null)
                throw IllegalArgumentException("Relevant command is required: ${stateKlazz.simpleName}")
            block()
        }
    }

    infix fun Boolean.implies(other: Boolean) = !this || other

    inner class Verify(tx: LedgerTransaction) {
        val ins = tx.inputSNRs()
        val outs = tx.outputSNRs()
        val command = tx.singleOrNoneCommand()

        fun LedgerTransaction.singleOrNoneCommand(): Command<CMD>? {
            val commands = commandsOfType(cmdKlazz.java)
            return when (commands.size) {
                0 -> null
                1 -> commands.first()
                else -> throw IllegalArgumentException("there must be only one command of type: ${cmdKlazz.simpleName}")
            }
        }

        fun LedgerTransaction.inputSNRs(): List<StateAndRef<STATE>> = inRefsOfType(stateKlazz.java)
        fun LedgerTransaction.outputSNRs(): List<StateAndRef<STATE>> = outRefsOfType(stateKlazz.java)
        fun <T : ContractState> Collection<StateAndRef<T>>.toStates() = map { it.state.data }
    }
}
