package cbdc.corda.contract.generic

import net.corda.core.contracts.Contract
import net.corda.core.transactions.LedgerTransaction

class DummyContract : Contract {
    companion object {
        val name = DummyContract::class.qualifiedName!!
    }

    override fun verify(tx: LedgerTransaction) {
    }
}
