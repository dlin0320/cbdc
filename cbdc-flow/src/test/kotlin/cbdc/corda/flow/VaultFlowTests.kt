package cbdc.corda.flow

import cbdc.corda.flow.generic.toCriteria
import cbdc.corda.flow.vault.CommonQueries.queryBalance
import cbdc.corda.schema.CBDCSchemaV1
import cbdc.corda.state.CENTRAL_BANK_VAULT_ID
import cbdc.corda.state.TokenState
import cbdc.corda.state.VaultState
import net.corda.core.node.services.vault.builder
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertAll
import java.util.UUID
import kotlin.test.assertEquals

class VaultFlowTests : CBDCFlowTest() {
    private val txnId = "TXN-ID"

    @Test
    fun `all nodes see CB vault`() {
        val stx = nodeA.runFlow(InitialiseCBVaultFlow(NewVaultK(null, null)))
        val output = stx.coreTransaction.outputs.single()
        val criteria = builder { CBDCSchemaV1.PersistentVault::id.equal(CENTRAL_BANK_VAULT_ID) }.toCriteria()
        val results = allPeers.map { it.services.vaultService.queryBy(VaultState::class.java, criteria).states.singleOrNull() }
        assertAll(
            { Assertions.assertTrue(results.all { it != null }) },
            { Assertions.assertTrue(results.all { it!!.state.data == output.data }) }
        )
    }

    @Test
    fun `mint works`() {
        val stx = nodeA.runFlow(CentralBankTokenisationFlows.Mint(txnId, 300.cbdc))
        val output = stx.coreTransaction.outputsOfType(TokenState::class.java).single()
        val results = allPeers.map { it.services.vaultService.queryBy(TokenState::class.java).states.singleOrNull() }
        assertAll(
            { Assertions.assertTrue(results.all { it != null }) },
            { Assertions.assertTrue(results.all { it!!.state.data == output }) }
        )
    }

    @Test
    fun `burn works`() {
        nodeA.runFlow(CentralBankTokenisationFlows.Mint(txnId, 300.cbdc))
        nodeA.runFlow(CentralBankTokenisationFlows.Burn(txnId, 50.cbdc))
        nodeA.transactionEx {
            val balance = services.queryBalance(CENTRAL_BANK_VAULT_ID)
            assertEquals(250.cbdc, balance)
        }
    }

    @Test
    fun `burn works - multiple inputs`() {
        nodeA.runFlow(CentralBankTokenisationFlows.Mint(txnId, 20.cbdc))
        nodeA.runFlow(CentralBankTokenisationFlows.Mint(txnId, 20.cbdc))
        nodeA.runFlow(CentralBankTokenisationFlows.Mint(txnId, 20.cbdc))
        nodeA.runFlow(CentralBankTokenisationFlows.Burn(txnId, 50.cbdc))
        nodeA.transactionEx { assertEquals(10.cbdc, services.queryBalance(CENTRAL_BANK_VAULT_ID)) }
        nodeA.runFlow(CentralBankTokenisationFlows.Burn(txnId, 10.cbdc))
        nodeA.transactionEx { assertEquals(0.cbdc, services.queryBalance(CENTRAL_BANK_VAULT_ID)) }
    }

    @Test
    fun `query CB wallet - empty`() {
        nodeA.transactionEx {
            val balance = services.queryBalance(CENTRAL_BANK_VAULT_ID)
            assertEquals(0.cbdc, balance)
        }
    }

    @Test
    fun `query CB wallet - fractional`() {
        nodeA.issueTokensAt(CENTRAL_BANK_VAULT_ID, 0.3.cbdc)
        nodeA.issueTokensAt(CENTRAL_BANK_VAULT_ID, 0.5.cbdc)
        nodeA.issueTokensAt(UUID.randomUUID().toString(), 3.cbdc) // issue to another wallet
        nodeB.issueTokensAt(CENTRAL_BANK_VAULT_ID, 1.cbdc) // issue from another node
        allPeers.forEach {
            it.transactionEx {
                val balance = services.queryBalance(CENTRAL_BANK_VAULT_ID)
                assertEquals(1.8.cbdc, balance)
            }
        }
    }

    @Test
    fun `query CB wallet - integer`() {
        nodeA.issueTokensAt(CENTRAL_BANK_VAULT_ID, 1.cbdc)
        nodeA.issueTokensAt(CENTRAL_BANK_VAULT_ID, 2.cbdc)
        nodeA.issueTokensAt(UUID.randomUUID().toString(), 3.cbdc) // issue to another wallet
        nodeB.issueTokensAt(CENTRAL_BANK_VAULT_ID, 4.cbdc) // issue from another node
        allPeers.forEach {
            it.transactionEx {
                val balance = services.queryBalance(CENTRAL_BANK_VAULT_ID)
                assertEquals(7.cbdc, balance)
            }
        }
    }
}
