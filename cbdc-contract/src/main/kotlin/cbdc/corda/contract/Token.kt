package cbdc.corda.contract

import cbdc.corda.contract.generic.BaseContract
import cbdc.corda.state.TokenState
import cbdc.corda.state.sumOrZero
import net.corda.core.contracts.TypeOnlyCommandData
import net.corda.core.contracts.requireThat
import net.corda.core.transactions.LedgerTransaction

class Token : BaseContract<TokenState, Token.Commands>(TokenState::class, Commands::class) {
    companion object {
        val name = Token::class.qualifiedName!!
    }

    sealed class Commands : TypeOnlyCommandData() {
        object Mint : Commands()
        object Burn : Commands()
        object Move : Commands()
    }

    override fun verify(tx: LedgerTransaction) {
        tx.verify {
            when (command?.value) {
                Commands.Mint -> {
                    requireThat {
                        "no tokens are spent" using ins.isEmpty()
                        "one token is issued" using outs.isSingle()
                        "token has positive amount" using outs.single().state.data.amount.isPositive()
                    }
                }
                Commands.Burn -> {
                    requireThat {
                        "some tokens are spent" using ins.isNotEmpty()
                        val oneToken = ins.first().state.data
                        val owner = oneToken.owner
                        val token = oneToken.amount.token
                        "tokens are of the same wallet" using ins.all { it.state.data.owner == owner }
                        "tokens are of the same type" using ins.all { it.state.data.amount.token == token }
                        "valid amount of tokens are burnt" using (ins.toStates().sumOrZero(token) > outs.toStates().sumOrZero(token))
                        when (outs.size) {
                            1 -> {
                                // with change
                                val change = outs.first().state.data
                                "change token must be of same owner" using (change.owner == owner)
                                "change token must be of same type" using (change.amount.token == token)
                            }
                            0 -> { /** no change issued */ }
                            else -> throw IllegalArgumentException("there must be one or no tokens generated as change")
                        }
                    }
                }
                Commands.Move -> {
                    requireThat {
                        "some tokens are spent" using ins.isNotEmpty()
                        "some tokens are issued" using outs.isNotEmpty()
                        val oneToken = ins.first().state.data
                        val owner = oneToken.owner
                        val token = oneToken.amount.token
                        "tokens are of the same wallet" using ins.all { it.state.data.owner == owner }
                        "tokens are of the same type" using ins.all { it.state.data.amount.token == token }
                        "tokens have positive amounts" using outs.all { it.state.data.amount.isPositive() }
                        "tokens issued are of the same type" using outs.all { it.state.data.amount.token == token }
                        "equal amounts of tokens are moved" using (ins.toStates().sumOrZero(token) == outs.toStates().sumOrZero(token, true))

                        when (outs.size) {
                            2 -> {
                                // with change
                                val (a, b) = outs
                                "transacted token and change belongs to different owners" using (a.state.data.owner != b.state.data.owner)
                                "one of the tokens must be a change" using (
                                    a.state.data.owner == owner || b.state.data.owner == owner
                                    )
                            }
                            1 -> {
                                "transacted token must belong to a new owner" using (outs.single().state.data.owner != owner)
                            }
                            else -> throw IllegalArgumentException("tokens issued must be transacted amount and optionally a change")
                        }
                    }
                }
                else -> {}
            }
        }
    }
}
