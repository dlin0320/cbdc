package cbdc.corda.middleware.utils

class Exceptions

class UnsupportedOperationException : RuntimeException()

class WalletAlreadyExistsException(id: String) : RuntimeException("Wallet with id: $id already exists.")
