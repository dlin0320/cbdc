package cbdc.corda.flow.generic

import cbdc.corda.flow.util.BalanceInsufficientException
import cbdc.corda.schema.CBDCSchemaV1
import cbdc.corda.state.TokenState
import cbdc.corda.state.generic.AddressId
import cbdc.corda.state.generic.Amount
import co.paralleluniverse.fibers.Suspendable
import net.corda.core.contracts.StateAndRef
import net.corda.core.contracts.requireThat
import net.corda.core.node.ServiceHub
import net.corda.core.node.services.vault.DEFAULT_PAGE_NUM
import net.corda.core.node.services.vault.DEFAULT_PAGE_SIZE
import net.corda.core.node.services.vault.PageSpecification
import net.corda.core.node.services.vault.QueryCriteria
import net.corda.core.node.services.vault.Sort
import net.corda.core.node.services.vault.SortAttribute
import net.corda.core.node.services.vault.builder

class TokenSelectionStrategy {
    data class Result(
        val tokens: List<StateAndRef<TokenState>>,
        val residue: Amount<String>
    )

    companion object {
        /**
         * Gather coins that are just enough for the amount specified.
         *
         * Note: it selects as few coins as possible, i.e., coins with larger values are more likely selected.
         */
        @Suspendable
        fun gatherCoins(
            serviceHub: ServiceHub,
            fromAddress: AddressId,
            amount: Amount<String>
        ): Result {
            requireThat {
                "Quantity must not be zero." using (amount.quantity > 0)
            }
            val ownerCriteria = builder { CBDCSchemaV1.PersistentToken::owner.equal(fromAddress) }.toCriteria()
            val tokenCriteria = builder { CBDCSchemaV1.PersistentToken::token.equal(amount.token) }.toCriteria()
            val unlockedCriteria = QueryCriteria.VaultQueryCriteria(
                softLockingCondition = QueryCriteria.SoftLockingCondition(QueryCriteria.SoftLockingType.UNLOCKED_ONLY)
            )
            // todo: avoid in-memory selection
            val tokens = serviceHub.vaultService.queryBy(
                TokenState::class.java,
                unlockedCriteria.and(ownerCriteria).and(tokenCriteria),
                paging = PageSpecification(DEFAULT_PAGE_NUM, DEFAULT_PAGE_SIZE),
                sorting = Sort(
                    columns = listOf(
                        Sort.SortColumn(
                            sortAttribute = SortAttribute.Custom(CBDCSchemaV1.PersistentToken::class.java, CBDCSchemaV1.PersistentToken::quantity.name),
                            direction = Sort.Direction.DESC
                        )
                    )
                )
            )

            val tokenItr = tokens.statesMetadata.zip(tokens.states).iterator()
            val snrs = mutableListOf<StateAndRef<TokenState>>()
            var sum = Amount.zero(amount.token)
            while (sum < amount && tokenItr.hasNext()) {
                val (_, s) = tokenItr.next()
                snrs.add(s)
                sum += s.state.data.amount
            }

            if (sum < amount) {
                throw BalanceInsufficientException(fromAddress)
            }
            return Result(
                tokens = snrs,
                residue = sum - amount
            )
        }
    }
}
