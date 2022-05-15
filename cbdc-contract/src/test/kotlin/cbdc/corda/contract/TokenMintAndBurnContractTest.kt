package cbdc.corda.contract

import cbdc.corda.contract.generic.BaseContractTest
import cbdc.corda.state.TokenState
import cbdc.corda.state.generic.Amount
import net.corda.testing.node.ledger
import org.junit.jupiter.api.Test

class TokenMintAndBurnContractTest : BaseContractTest() {
    private val walletIdA = "Wallet-A"
    private val walletIdB = "Wallet-B"

    @Test
    fun `mint - tokens must have 1 input and 0 outputs`() {
        ledgerServices.ledger {
            transaction {
                val tx = unverifiedTransaction {
                    attachment(Token.name)
                    output(Token.name, TokenState(walletIdA, 1.cbdc))
                }
                val inTokens = tx.outRefsOfType(TokenState::class.java)

                attachment(Token.name)
                command(partyA.publicKey, Token.Commands.Mint)

                tweak {
                    inputs(inTokens, 0)
                    `fails with`("no tokens are spent")
                }

                tweak {
                    output(Token.name, TokenState(walletIdA, 1.cbdc))
                    output(Token.name, TokenState(walletIdA, 1.cbdc))
                    `fails with`("one token is issued")
                }

                tweak {
                    output(Token.name, TokenState(walletIdA, 0.cbdc))
                    `fails with`("token has positive amount")
                }

                output(Token.name, TokenState(walletIdB, 0.01.cbdc))
                verifies()
            }
        }
    }

    @Test
    fun `burn - tokens should be of the same owner and type`() {
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
                command(partyA.publicKey, Token.Commands.Burn)

                tweak {
                    inputs(inTokens, 0, 1, 3)
                    `fails with`("tokens are of the same wallet")
                }

                tweak {
                    inputs(inTokens, 0, 1, 4)
                    `fails with`("tokens are of the same type")
                }

                tweak {
                    inputs(inTokens, 0, 1)
                    output(Token.name, TokenState(walletIdB, 0.5.cbdc))
                    `fails with`("change token must be of same owner")
                }

                tweak {
                    inputs(inTokens, 0, 1)
                    output(Token.name, TokenState(walletIdA, Amount.fromLong(100, "0902")))
                    `fails with`("Token mismatch")
                }

                inputs(inTokens, 0, 1, 2)
                verifies()
            }
        }
    }

    @Test
    fun `burn - tokens must have some inputs and 0 or 1 output`() {
        ledgerServices.ledger {
            transaction {
                val tx = unverifiedTransaction {
                    attachment(Token.name)
                    output(Token.name, TokenState(walletIdA, 1.cbdc))
                    output(Token.name, TokenState(walletIdA, 1.cbdc))
                }
                val inTokens = tx.outRefsOfType(TokenState::class.java)

                attachment(Token.name)
                command(partyA.publicKey, Token.Commands.Burn)

                tweak {
                    output(Token.name, TokenState(walletIdA, 1.cbdc))
                    `fails with`("some tokens are spent")
                }

                tweak {
                    inputs(inTokens, 0)
                    output(Token.name, TokenState(walletIdA, 0.5.cbdc))
                    output(Token.name, TokenState(walletIdB, 0.5.cbdc))
                    `fails with`("tokens are of the same wallet")
                }

                tweak {
                    inputs(inTokens, 0)
                    verifies()
                }

                tweak {
                    inputs(inTokens, 0)
                    output(Token.name, TokenState(walletIdA, 0.3.cbdc))
                    verifies()
                }

                inputs(inTokens, 0, 1)
                verifies()
            }
        }
    }
}
