package cbdc.corda.middleware.rest.api

import cbdc.corda.flow.vault.CommonQueries.queryBalance
import cbdc.corda.flow.vault.CommonQueries.queryVault
import cbdc.corda.middleware.core.service.CordaService
import gov.cbc.cbdc.utilities.client.CBClient
import gov.cbc.cbdc.utilities.domain.entity.account.CBVault
import org.springframework.web.bind.annotation.RestController

@RestController
class CBClientCordaImpl(
    private val cordaService: CordaService
) : CBClient {
    override fun get(cbVaultID: String): CBVault? {
        val cbVaultState = cordaService.rpcOps.queryVault(cbVaultID) ?: return null
        val balance = cordaService.rpcOps.queryBalance(cbVaultID)

        return CBVault(
            cbVaultState.id,
            balance.quantity,
            cbVaultState.totalCount,
            cbVaultState.createTime,
            cbVaultState.settingLastModified
        )
    }
}
