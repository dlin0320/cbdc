package cbdc.corda.contract

import net.corda.core.contracts.Contract
import net.corda.core.contracts.TypeOnlyCommandData
import net.corda.core.transactions.LedgerTransaction

class WalletContract : Contract {
    sealed class Commands : TypeOnlyCommandData() {
        object Create : Commands()
        object Update : Commands()
        object Remove : Commands()
    }

    override fun verify(tx: LedgerTransaction) {
    }
}

// class NaturalPersonWithPubKeyWallet : WalletContract() {
//    override fun verify(tx: LedgerTransaction) {
//    }
// }
//
// class NaturalPersonWithCertWallet : WalletContract() {
//    override fun verify(tx: LedgerTransaction) {
//    }
// }
//
// class JuristicPersonWallet : WalletContract() {
//    override fun verify(tx: LedgerTransaction) {
//    }
// }
//
// class AnonymousWallet : WalletContract() {
//    override fun verify(tx: LedgerTransaction) {
//    }
// }
