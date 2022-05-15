package cbdc.corda.flow.util

import cbdc.corda.state.generic.AddressId
import net.corda.core.serialization.CordaSerializable

@CordaSerializable
abstract class LedgerException(msg: String?) : RuntimeException(msg ?: "")

class BalanceInsufficientException(addressId: AddressId) : LedgerException("Wallet has insufficient balance: $addressId.")

class TransactionNotFoundException(txId: String) : LedgerException("No transaction found with id: $txId")

class StateNotFoundException(id: String, stateName: String) : LedgerException("No $stateName found with id: $id")
