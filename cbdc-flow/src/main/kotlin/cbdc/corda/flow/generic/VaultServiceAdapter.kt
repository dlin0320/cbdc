package cbdc.corda.flow.generic

import net.corda.core.contracts.ContractState
import net.corda.core.identity.Party
import net.corda.core.messaging.CordaRPCOps
import net.corda.core.node.ServiceHub
import net.corda.core.node.services.Vault
import net.corda.core.node.services.vault.PageSpecification
import net.corda.core.node.services.vault.QueryCriteria
import net.corda.core.node.services.vault.Sort

interface VaultServiceAdapter<T : ContractState> {
    fun myParty(): Party

    fun query(contractState: Class<T>): Vault.Page<T>

    fun queryWithCriteria(contractState: Class<T>, criteria: QueryCriteria): Vault.Page<T>

    fun queryWithCriteriaAndPaging(contractState: Class<T>, criteria: QueryCriteria, paging: PageSpecification, sorting: Sort): Vault.Page<T>
}

class ServiceHubService<T : ContractState>(private val serviceHub: ServiceHub) : VaultServiceAdapter<T> {
    companion object {
        fun <T : ContractState> ServiceHub.toVaultService(): ServiceHubService<T> = ServiceHubService(this)
    }

    override fun query(contractState: Class<T>): Vault.Page<T> {
        return serviceHub.vaultService.queryBy(contractState)
    }

    override fun queryWithCriteria(contractState: Class<T>, criteria: QueryCriteria): Vault.Page<T> {
        return serviceHub.vaultService.queryBy(contractState, criteria)
    }

    override fun queryWithCriteriaAndPaging(contractState: Class<T>, criteria: QueryCriteria, paging: PageSpecification, sorting: Sort): Vault.Page<T> {
        return serviceHub.vaultService.queryBy(contractState, criteria, paging, sorting)
    }

    override fun myParty() = serviceHub.myInfo.legalIdentities[0]
}

class CordaRpcService<T : ContractState>(private val rpcOps: CordaRPCOps) : VaultServiceAdapter<T> {
    companion object {
        fun <T : ContractState> CordaRPCOps.toVaultService(): CordaRpcService<T> = CordaRpcService(this)
    }

    override fun query(contractState: Class<T>): Vault.Page<T> {
        return rpcOps.vaultQuery(contractState)
    }

    override fun queryWithCriteria(contractState: Class<T>, criteria: QueryCriteria): Vault.Page<T> {
        return rpcOps.vaultQueryByCriteria(criteria, contractState)
    }

    override fun queryWithCriteriaAndPaging(contractState: Class<T>, criteria: QueryCriteria, paging: PageSpecification, sorting: Sort): Vault.Page<T> {
        return rpcOps.vaultQueryBy(criteria, paging, sorting, contractState)
    }

    override fun myParty() = rpcOps.nodeInfo().legalIdentities[0]
}
