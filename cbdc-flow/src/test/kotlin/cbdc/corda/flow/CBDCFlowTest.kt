package cbdc.corda.flow

import cbdc.corda.contract.Instruction
import cbdc.corda.contract.Token
import cbdc.corda.state.TokenState
import cbdc.corda.state.generic.AddressId
import cbdc.corda.state.generic.Amount
import cbdc.corda.state.generic.TokenAmount
import net.corda.core.transactions.TransactionBuilder
import net.corda.testing.node.StartedMockNode
import java.math.BigDecimal

abstract class CBDCFlowTest : FlowTest() {
    protected val Double.cbdc get() = Amount.fromDecimal(BigDecimal.valueOf(this))
    protected val Long.cbdc get() = Amount.fromDecimal(BigDecimal.valueOf(this))
    protected val Int.cbdc get() = this.toLong().cbdc

    protected fun StartedMockNode.issueTokensAt(addressId: AddressId, amount: TokenAmount) {
        TransactionBuilder(network.defaultNotaryIdentity)
            .addCommand(Instruction.Commands.Mint, this.services.myInfo.legalIdentities.first().owningKey)
            .addCommand(Token.Commands.Mint, this.services.myInfo.legalIdentities.first().owningKey)
            .addOutputState(
                TokenState(
                    owner = addressId,
                    amount = amount
                )
            )
            .signedAndRecordedBy(this)
    }
}
