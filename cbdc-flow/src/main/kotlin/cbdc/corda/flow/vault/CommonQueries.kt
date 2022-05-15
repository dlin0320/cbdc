package cbdc.corda.flow.vault

import cbdc.corda.flow.generic.CordaRpcService
import cbdc.corda.flow.generic.ServiceHubService
import cbdc.corda.flow.generic.VaultServiceAdapter
import cbdc.corda.flow.generic.toCriteria
import cbdc.corda.schema.CBDCSchemaV1
import cbdc.corda.state.AuthId
import cbdc.corda.state.AuthorizationState
import cbdc.corda.state.DummyState
import cbdc.corda.state.NumberState
import cbdc.corda.state.PlanState
import cbdc.corda.state.TokenState
import cbdc.corda.state.VaultState
import cbdc.corda.state.WalletState
import cbdc.corda.state.generic.AddressId
import cbdc.corda.state.generic.Amount
import cbdc.corda.state.generic.TokenAmount
import net.corda.core.messaging.CordaRPCOps
import net.corda.core.node.ServiceHub
import net.corda.core.node.services.vault.builder
import org.slf4j.LoggerFactory
import java.sql.ResultSet
import java.sql.SQLException

object CommonQueries {
    private val logger = LoggerFactory.getLogger(CommonQueries::class.java)

    fun ServiceHub.queryBalance(addressId: AddressId, token: String = "0901") = ServiceHubService<TokenState>(this).queryBalance(addressId, token)

    fun CordaRPCOps.queryBalance(addressId: AddressId, token: String = "0901") = CordaRpcService<TokenState>(this).queryBalance(addressId, token)

    private fun VaultServiceAdapter<TokenState>.queryBalance(addressId: AddressId, token: String = "0901"): TokenAmount {
        val ownerCriteria = builder { CBDCSchemaV1.PersistentToken::owner.equal(addressId) }.toCriteria()
        val tokenCriteria = builder { CBDCSchemaV1.PersistentToken::token.equal(token) }.toCriteria()
        val sumCriteria = builder { CBDCSchemaV1.PersistentToken::quantity.sum() }.toCriteria()
        val result = queryWithCriteria(TokenState::class.java, ownerCriteria.and(tokenCriteria).and(sumCriteria))
        val sum = result.otherResults[0] as? Long ?: 0L // `null` means sum to 0
        return Amount.fromLong(sum)
    }

    fun ServiceHub.getAllWallet(): List<WalletState> = ServiceHubService<WalletState>(this).getAllWallet()

    fun CordaRPCOps.getAllWallet(): List<WalletState> = CordaRpcService<WalletState>(this).getAllWallet()

    private fun VaultServiceAdapter<WalletState>.getAllWallet(): List<WalletState> {
        return query(WalletState::class.java).states.map { it.state.data }
    }

    fun ServiceHub.queryWallet(addressId: AddressId) = ServiceHubService<WalletState>(this).queryWallet(addressId)

    fun CordaRPCOps.queryWallet(addressId: AddressId) = CordaRpcService<WalletState>(this).queryWallet(addressId)

    private fun VaultServiceAdapter<WalletState>.queryWallet(addressId: AddressId): WalletState? {
        val criteria =
            builder { CBDCSchemaV1.PersistentWallet::id.equal(addressId) }.toCriteria()

        return queryWithCriteria(WalletState::class.java, criteria).states.singleOrNull()?.state?.data
    }

    fun ServiceHub.doesWalletExist(addressId: AddressId) = ServiceHubService<WalletState>(this).doesWalletExist(addressId)
    fun CordaRPCOps.doesWalletExist(addressId: AddressId) = CordaRpcService<WalletState>(this).doesWalletExist(addressId)
    private fun VaultServiceAdapter<WalletState>.doesWalletExist(addressId: AddressId): Boolean {
        val criteria = builder { CBDCSchemaV1.PersistentWallet::id.equal(addressId) }.toCriteria()
        val existCriteria = builder { CBDCSchemaV1.PersistentWallet::id.count() }.toCriteria()
        val result = queryWithCriteria(WalletState::class.java, criteria.and(existCriteria))
        val count = result.otherResults[0] as? Long ?: 0L
        return count == 1L
    }

    fun ServiceHub.queryWalletBalanceLimit(addressId: AddressId): Pair<Boolean, Long?> {
        val session = jdbcSession()
        val preparedStatementRead = session.prepareStatement("select balance_limit from wallet_state where id = ?")
        preparedStatementRead.setString(1, addressId)

        var rs: ResultSet? = null
        return try {
            preparedStatementRead.executeQuery()
            rs = preparedStatementRead.resultSet
            if (rs.next()) {
                val balanceLimit = rs.getObject(1) as? Long?
                true to balanceLimit
            } else false to null
        } catch (e: SQLException) {
            logger.error("Failed to execute query: ", e)
            false to null
        } finally {
            try {
                rs?.close()
                preparedStatementRead.close()
            } catch (e: SQLException) {
                logger.error("Failed to close: ", e)
            }
        }
    }

    fun ServiceHub.queryVault(addressId: AddressId) = ServiceHubService<VaultState>(this).queryVault(addressId)

    fun CordaRPCOps.queryVault(addressId: AddressId) = CordaRpcService<VaultState>(this).queryVault(addressId)

    private fun VaultServiceAdapter<VaultState>.queryVault(addressId: AddressId): VaultState? {
        val criteria =
            builder { CBDCSchemaV1.PersistentVault::id.equal(addressId) }.toCriteria()

        return queryWithCriteria(VaultState::class.java, criteria).states.singleOrNull()?.state?.data
    }

    fun ServiceHub.queryAuthorization(id: AuthId) = ServiceHubService<AuthorizationState>(this).queryAuthorization(id)

    fun CordaRPCOps.queryAuthorization(id: AuthId) = CordaRpcService<AuthorizationState>(this).queryAuthorization(id)

    private fun VaultServiceAdapter<AuthorizationState>.queryAuthorization(id: AuthId): AuthorizationState? {
        val criteria =
            builder { CBDCSchemaV1.PersistentAuthorization::authId.equal(id) }.toCriteria()

        return queryWithCriteria(AuthorizationState::class.java, criteria).states.singleOrNull()?.state?.data
    }

    fun ServiceHub.queryNumber(id: String) = ServiceHubService<NumberState>(this).queryNumber(id)

    fun CordaRPCOps.queryNumber(id: String) = CordaRpcService<NumberState>(this).queryNumber(id)

    private fun VaultServiceAdapter<NumberState>.queryNumber(addressId: AddressId): NumberState? {
        val criteria =
            builder {
                CBDCSchemaV1.PersistentWallet::id.equal(addressId)
                CBDCSchemaV1.PersistentNumber::enabled.equal(true)
            }.toCriteria()

        return queryWithCriteria(NumberState::class.java, criteria).states.singleOrNull()?.state?.data
    }

    fun ServiceHub.queryPlan(operation: String) = ServiceHubService<PlanState>(this).queryPlan(operation)

    fun CordaRPCOps.queryPlan(operation: String) = CordaRpcService<PlanState>(this).queryPlan(operation)

    private fun VaultServiceAdapter<PlanState>.queryPlan(operation: String): PlanState? {
        val criteria = builder {
            CBDCSchemaV1.PersistentPlan::operation.equal(operation)
        }.toCriteria()

        return queryWithCriteria(PlanState::class.java, criteria).states.singleOrNull()?.state?.data
    }

    fun CordaRPCOps.queryDummy(id: String) = CordaRpcService<DummyState>(this).queryDummy(id)

    private fun VaultServiceAdapter<DummyState>.queryDummy(id: String): DummyState? {
        val criteria = builder {
            CBDCSchemaV1.PersistentDummy::id.equal(id)
        }.toCriteria()

        return queryWithCriteria(DummyState::class.java, criteria).states.singleOrNull()?.state?.data
    }

    fun CordaRPCOps.allDummy() = CordaRpcService<DummyState>(this).allDummy()

    private fun VaultServiceAdapter<DummyState>.allDummy(): List<DummyState> {
        return query(DummyState::class.java).states.map { it.state.data }
    }
}
