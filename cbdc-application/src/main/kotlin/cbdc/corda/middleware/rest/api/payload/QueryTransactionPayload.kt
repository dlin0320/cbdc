package cbdc.corda.middleware.rest.api.payload

import java.time.Instant

data class QueryTransactionPayload(
    val txId: String?,
    val from: Instant?,
    val to: Instant?
)
