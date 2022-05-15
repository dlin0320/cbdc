package cbdc.corda.middleware.rest.service

import cbdc.corda.middleware.core.service.AbstractSimpleCordaService
import com.google.common.cache.CacheBuilder
import com.google.common.cache.CacheLoader
import net.corda.core.contracts.ContractState
import net.corda.core.contracts.StateAndRef
import net.corda.core.contracts.StateRef
import net.corda.core.messaging.vaultQueryBy
import net.corda.core.node.services.Vault
import net.corda.core.node.services.vault.QueryCriteria
import net.corda.core.transactions.SignedTransaction
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import javax.annotation.PostConstruct

class SimpleCordaServiceImpl : AbstractSimpleCordaService()

@Service("cordaService")
class CachingCordaService : AbstractSimpleCordaService() {
    val logger: Logger = LoggerFactory.getLogger(CachingCordaService::class.java)

    companion object {
        // todo: caching disabled
        private const val maxCacheFailedAllowed = 0

        inline fun <reified T : ContractState> SignedTransaction.getSingle() =
            this.tx.outputStates.filterIsInstance<T>().single()

        inline fun <reified T : ContractState> SignedTransaction.getMultiple(): List<T> =
            this.tx.outputStates.filterIsInstance<T>()
    }

    private var numCacheFailed = 0

    private fun cacheFailed() = synchronized(this) {
        numCacheFailed += 1
    }

    private val stateCache = CacheBuilder.newBuilder()
        .maximumSize(500)
        .build(object : CacheLoader<StateRef, StateAndRef<*>>() {
            override fun load(key: StateRef): StateAndRef<*> {
                return loadAll(listOf(key)).values.single()
            }

            override fun loadAll(keys: Iterable<StateRef>): Map<StateRef, StateAndRef<*>> {
                val refs = keys.toList()
                val page = rpcOps.vaultQueryBy<ContractState>(
                    QueryCriteria.VaultQueryCriteria(stateRefs = refs, status = Vault.StateStatus.ALL)
                )

                val result = page.states.associateBy { it.ref }

                for (ref in refs) {
                    if (result[ref] == null) {
                        throw IllegalStateException("ref=$ref")
                    }
                }

                return result
            }
        })

    @PostConstruct
    fun populateCache() {
        newStateUpdates.subscribe {
            stateCache.put(it.ref, it)
        }
        log.debug("State caching is banned forever until server restarts")
    }
}
