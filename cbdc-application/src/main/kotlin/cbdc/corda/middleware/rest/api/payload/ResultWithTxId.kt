package cbdc.corda.middleware.rest.api.payload

data class ResultWithTxId(
    val result: Any,
    val txId: String
)
