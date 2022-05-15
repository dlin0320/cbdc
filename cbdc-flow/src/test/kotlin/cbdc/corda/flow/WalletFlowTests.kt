package cbdc.corda.flow

import cbdc.corda.flow.vault.CommonQueries.doesWalletExist
import cbdc.corda.flow.vault.CommonQueries.queryVault
import cbdc.corda.flow.vault.CommonQueries.queryWalletBalanceLimit
import cbdc.corda.state.CertInfoK
import cbdc.corda.state.WalletTypeK
import gov.cbc.cbdc.utilities.domain.entity.CertInfo
import net.corda.core.contracts.ContractState
import net.corda.core.transactions.SignedTransaction
import net.corda.testing.core.TestIdentity
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertAll
import java.util.UUID
import kotlin.test.assertEquals

class WalletFlowTests : CBDCFlowTest() {
    private inline fun <reified T : ContractState> SignedTransaction.initWalletAssertions() {
        val output = this.coreTransaction.outputs.single()
        val results = allPeers.map { it.services.vaultService.queryBy(T::class.java).states.singleOrNull() }
        assertAll(
            { assertTrue(results.all { it != null }) },
            { assertTrue(results.all { it!!.state.data == output.data }) }
        )
    }

    private fun newCertInfo() = CertInfo(TestIdentity.fresh(UUID.randomUUID().toString()).identity.certificate).run { CertInfoK(certID, pubKey, notAfter) }

    @Test
    fun `create bank vault flow`() {
        nodeA.runFlow(InitialiseBankVaultFlow("IDX-24023", NewVaultK(newCertInfo(), newCertInfo())))

        nodeB.transactionEx {
            val result = services.queryVault("IDX-24023")
            assertTrue(result != null)
            assertFalse(result!!.setting.frozen!!)
            assertFalse(result.setting.disabled!!)
        }
    }

    @Test
    fun `freeze, unfreeze, disable vault flow`() {
        nodeA.runFlow(InitialiseBankVaultFlow("IDX-24023", NewVaultK(newCertInfo(), newCertInfo())))
        nodeC.transactionEx {
            val result = services.queryVault("IDX-24023")
            assertTrue(result != null)
            assertFalse(result!!.setting.frozen!!)
            assertFalse(result.setting.disabled!!)
        }
        nodeA.runFlow(FreezeVaultFlow("IDX-24023"))
        nodeC.transactionEx {
            val result = services.queryVault("IDX-24023")
            assertTrue(result != null)
            assertTrue(result!!.setting.frozen!!)
            assertFalse(result.setting.disabled!!)
        }
        nodeB.runFlow(UnfreezeVaultFlow("IDX-24023"))
        nodeC.transactionEx {
            val result = services.queryVault("IDX-24023")
            assertTrue(result != null)
            assertFalse(result!!.setting.frozen!!)
            assertFalse(result.setting.disabled!!)
        }
        nodeC.runFlow(DisableVaultFlow("IDX-24023"))
        nodeC.transactionEx {
            val result = services.queryVault("IDX-24023")
            assertTrue(result != null)
            assertFalse(result!!.setting.frozen!!)
            assertTrue(result.setting.disabled!!)
        }
    }

    @Test
    fun `update vault agency cert info`() {
        val newCert = newCertInfo()
        nodeA.runFlow(InitialiseBankVaultFlow("IDX-24023", NewVaultK(newCertInfo(), newCertInfo())))
        nodeB.runFlow(UpdateAgencyCertFlow("IDX-24023", newCert))
        nodeC.transactionEx {
            val result = services.queryVault("IDX-24023")
            assertTrue(result != null)
            assertEquals(newCert, result!!.setting.agencyCertInfo)
        }
    }

    @Test
    fun `update vault cert info`() {
        val newCert = newCertInfo()
        nodeA.runFlow(InitialiseBankVaultFlow("IDX-24023", NewVaultK(newCertInfo(), newCertInfo())))
        nodeB.runFlow(UpdateVaultCertFlow("IDX-24023", newCert))
        nodeC.transactionEx {
            val result = services.queryVault("IDX-24023")
            assertTrue(result != null)
            assertEquals(newCert, result!!.setting.vaultCertInfo)
        }
    }

    @Test
    fun `query helper - queryWalletBalanceLimit`() {
        nodeA.runFlow(
            InitialiseWalletFlow(
                "IDX-24023",
                NewWalletK(null, 10000, null, WalletTypeK.REGISTERED, null, null, null, CertInfoK("", byteArrayOf(), null), null)
            )
        )
        nodeA.runFlow(
            InitialiseWalletFlow(
                "IDX-24024",
                NewWalletK(null, null, null, WalletTypeK.REGISTERED, null, null, null, CertInfoK("", byteArrayOf(), null), null)
            )
        )
        nodeA.transactionEx {
            assertEquals(services.queryWalletBalanceLimit("IDX-24023"), true to 10000L)
//            fixme: `null` evaluates to Long.MAX_VALUE after inserting to db
//            assertEquals(services.queryWalletBalanceLimit("IDX-24024"), true to null)
            assertEquals(services.queryWalletBalanceLimit("IDX-24025"), false to null)
        }
    }

    @Test
    fun `query helper - doesWalletExist`() {
        nodeA.runFlow(
            InitialiseWalletFlow(
                "IDX-24023",
                NewWalletK(null, 10000, null, WalletTypeK.REGISTERED, null, null, null, CertInfoK("", byteArrayOf(), null), null)
            )
        )
        nodeA.transactionEx {
            assertTrue(services.doesWalletExist("IDX-24023"))
            assertFalse(services.doesWalletExist("DOES_NOT_EXIST"))
        }
    }
}
