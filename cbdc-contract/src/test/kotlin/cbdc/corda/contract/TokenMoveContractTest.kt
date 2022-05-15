package cbdc.corda.contract

import cbdc.corda.contract.generic.BaseContractTest
import cbdc.corda.state.TokenState
import cbdc.corda.state.generic.Amount
import net.corda.testing.node.ledger
import org.junit.jupiter.api.Test

class TokenMoveContractTest : BaseContractTest() {
    private val walletIdA = "Wallet-A"
    private val walletIdB = "Wallet-B"
    private val walletIdC = "Wallet-C"

    @Test
    fun `move - tokens must be input and output`() {
        ledgerServices.ledger {
            transaction {
                val tx = unverifiedTransaction {
                    attachment(Token.name)
                    output(Token.name, TokenState(walletIdA, 1.cbdc))
                    output(Token.name, TokenState(walletIdA, 1.cbdc))
                }
                val inTokens = tx.outRefsOfType(TokenState::class.java)

                attachment(Token.name)
                command(partyA.publicKey, Token.Commands.Move)

                tweak {
                    output(Token.name, TokenState(walletIdA, 0.cbdc))
                    `fails with`("some tokens are spent")
                }

                tweak {
                    inputs(inTokens, 0)
                    `fails with`("some tokens are issued")
                }

                inputs(inTokens, 0)
                output(Token.name, TokenState(walletIdB, 1.cbdc))
                verifies()
            }
        }
    }

    @Test
    fun `move - tokens should be of the same owner and type`() {
        ledgerServices.ledger {
            transaction {
                val tx = unverifiedTransaction {
                    attachment(Token.name)
                    output(Token.name, TokenState(walletIdA, 1.cbdc))
                    output(Token.name, TokenState(walletIdA, 1.cbdc))
                    output(Token.name, TokenState(walletIdA, 1.cbdc))
                    output(Token.name, TokenState(walletIdB, 1.cbdc))
                    output(Token.name, TokenState(walletIdA, Amount.fromLong(100, "0902")))
                }
                val inTokens = tx.outRefsOfType(TokenState::class.java)

                attachment(Token.name)
                command(partyA.publicKey, Token.Commands.Move)
                output(Token.name, TokenState(walletIdA, 0.5.cbdc))
                output(Token.name, TokenState(walletIdB, 2.5.cbdc))

                tweak {
                    inputs(inTokens, 0, 1, 3)
                    `fails with`("tokens are of the same wallet")
                }

                tweak {
                    inputs(inTokens, 0, 1, 4)
                    `fails with`("tokens are of the same type")
                }

                inputs(inTokens, 0, 1, 2)
                verifies()
            }
        }
    }

    @Test
    fun `move - output tokens should either be transaction amount or change`() {
        ledgerServices.ledger {
            transaction {
                val tx = unverifiedTransaction {
                    attachment(Token.name)
                    output(Token.name, TokenState(walletIdA, 1.cbdc))
                }
                val inTokens = tx.outRefsOfType(TokenState::class.java)

                attachment(Token.name)
                command(partyA.publicKey, Token.Commands.Move)
                inputs(inTokens, 0)

                tweak {
                    output(Token.name, TokenState(walletIdA, 0.5.cbdc))
                    output(Token.name, TokenState(walletIdA, 0.5.cbdc))
                    `fails with`("transacted token and change belongs to different owners") // transacting with myself
                }

                tweak {
                    output(Token.name, TokenState(walletIdB, 0.5.cbdc))
                    output(Token.name, TokenState(walletIdB, 0.5.cbdc))
                    `fails with`("transacted token and change belongs to different owners") // no change to myself
                }

                tweak {
                    output(Token.name, TokenState(walletIdB, 0.5.cbdc))
                    output(Token.name, TokenState(walletIdC, 0.5.cbdc))
                    `fails with`("one of the tokens must be a change")
                }

                tweak {
                    output(Token.name, TokenState(walletIdA, 1.cbdc))
                    output(Token.name, TokenState(walletIdB, 0.cbdc))
                    `fails with`("tokens have positive amounts")
                }

                tweak {
                    output(Token.name, TokenState(walletIdA, 1.cbdc))
                    `fails with`("transacted token must belong to a new owner")
                }

                tweak {
                    output(Token.name, TokenState(walletIdA, 0.5.cbdc))
                    output(Token.name, TokenState(walletIdB, 0.5.cbdc))
                    verifies()
                }

                output(Token.name, TokenState(walletIdB, 1.cbdc))
                verifies()
            }
        }
    }

    @Test
    fun `move - input and output tokens should have equal amounts`() {
        ledgerServices.ledger {
            transaction {
                val tx = unverifiedTransaction {
                    attachment(Token.name)
                    output(Token.name, TokenState(walletIdA, 1.cbdc))
                    output(Token.name, TokenState(walletIdA, 1.cbdc))
                    output(Token.name, TokenState(walletIdA, 1.cbdc))
                }
                val inTokens = tx.outRefsOfType(TokenState::class.java)

                attachment(Token.name)
                command(partyA.publicKey, Token.Commands.Move)
                inputs(inTokens, 0, 1, 2)
                output(Token.name, TokenState(walletIdA, 0.5.cbdc))

                tweak {
                    output(Token.name, TokenState(walletIdB, 2.49.cbdc))
                    `fails with`("equal amounts of tokens are moved")
                }

                tweak {
                    output(Token.name, TokenState(walletIdB, 2.51.cbdc))
                    `fails with`("equal amounts of tokens are moved")
                }

                output(Token.name, TokenState(walletIdB, 2.5.cbdc))
                verifies()
            }
        }
    }
}
