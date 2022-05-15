package cbdc.corda.flow.generic

import net.corda.core.node.services.vault.CriteriaExpression
import net.corda.core.node.services.vault.QueryCriteria
import net.corda.core.schemas.StatePersistable

fun <L : StatePersistable> CriteriaExpression<L, Boolean>.toCriteria() = QueryCriteria.VaultCustomQueryCriteria(this)
