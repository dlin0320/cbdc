package cbdc.corda.contract

import net.corda.core.contracts.Contract
import net.corda.core.contracts.TypeOnlyCommandData
import net.corda.core.transactions.LedgerTransaction

class VaultContract : Contract {
    sealed class Commands : TypeOnlyCommandData() {
        object Create : Commands()
        object Update : Commands()
        object Remove : Commands()
    }

    override fun verify(tx: LedgerTransaction) {
    }
}

class CentralBankVault : Contract {
    override fun verify(tx: LedgerTransaction) {
    }
}

class CommercialBankVault : Contract {
    override fun verify(tx: LedgerTransaction) {
    }
}
