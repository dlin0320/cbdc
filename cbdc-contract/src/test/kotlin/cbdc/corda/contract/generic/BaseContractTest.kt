package cbdc.corda.contract.generic

import cbdc.corda.state.generic.Amount
import net.corda.core.contracts.StateAndRef
import net.corda.testing.common.internal.testNetworkParameters
import net.corda.testing.core.ALICE_NAME
import net.corda.testing.core.BOB_NAME
import net.corda.testing.core.CHARLIE_NAME
import net.corda.testing.core.TestIdentity
import net.corda.testing.dsl.TransactionDSL
import net.corda.testing.node.MockServices
import java.math.BigDecimal
import java.time.Instant

abstract class BaseContractTest {
    val partyA = TestIdentity(ALICE_NAME)
    val partyB = TestIdentity(BOB_NAME)
    val partyC = TestIdentity(CHARLIE_NAME)
    val ledgerServices = MockServices(
        cordappPackages = listOf("cbdc.corda"),
        firstIdentity = partyA,
        networkParameters = testNetworkParameters(
            minimumPlatformVersion = 4
        )
    )
    val now = Instant.now()
    val Double.cbdc get() = Amount.fromDecimal(BigDecimal.valueOf(this))
    val Long.cbdc get() = Amount.fromDecimal(BigDecimal.valueOf(this))
    val Int.cbdc get() = this.toLong().cbdc

    fun TransactionDSL<*>.inputs(snrs: List<StateAndRef<*>>, vararg indices: Int) {
        for (i in indices) {
            input(snrs[i].ref)
        }
    }
}
