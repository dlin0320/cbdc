package cbdc.corda.middleware.rest.api

import cbdc.corda.flow.DisableVaultFlow
import cbdc.corda.flow.FreezeVaultFlow
import cbdc.corda.flow.InitialiseBankVaultFlow
import cbdc.corda.flow.UnfreezeVaultFlow
import cbdc.corda.flow.UpdateAgencyCertFlow
import cbdc.corda.flow.UpdateVaultCertFlow
import cbdc.corda.flow.vault.CommonQueries.queryBalance
import cbdc.corda.flow.vault.CommonQueries.queryVault
import cbdc.corda.middleware.core.service.CordaService
import cbdc.corda.middleware.rest.api.convert.toJava
import cbdc.corda.middleware.rest.api.convert.toKotlin
import cbdc.corda.middleware.utils.curry
import gov.cbc.cbdc.utilities.client.VaultClient
import gov.cbc.cbdc.utilities.domain.entity.CertInfo
import gov.cbc.cbdc.utilities.domain.entity.account.NewVault
import gov.cbc.cbdc.utilities.domain.entity.account.Vault
import gov.cbc.cbdc.utilities.domain.entity.account.VaultSetting
import gov.cbc.cbdc.utilities.domain.entity.account.VaultStatus
import org.springframework.web.bind.annotation.RestController

@RestController
class VaultClientCordaImpl(
    private val cordaService: CordaService
) : VaultClient {
    override fun add(userId: String, newVault: NewVault) {
        cordaService.startFlow(::InitialiseBankVaultFlow.curry(userId, newVault.toKotlin())).returnValue.get()
    }

    override fun getTotal(vaultID: String): Vault? {
        val status = getStatus(vaultID) ?: return null
        val setting = getSetting(vaultID) ?: return null
        return Vault(status, setting)
    }

    override fun getStatus(vaultID: String): VaultStatus? {
        val vault = cordaService.rpcOps.queryVault(vaultID) ?: return null
        val balance = cordaService.rpcOps.queryBalance(vaultID)

        return vault.copy(balance = balance.quantity).status.toJava()
    }

    override fun getSetting(vaultID: String): VaultSetting? {
        val vault = cordaService.rpcOps.queryVault(vaultID) ?: return null
        return vault.setting.toJava()
    }

    override fun disable(vaultID: String) {
        cordaService.startFlow(::DisableVaultFlow.curry(vaultID))
            .returnValue.get()
    }

    override fun freeze(vaultID: String) {
        cordaService.startFlow(::FreezeVaultFlow.curry(vaultID))
            .returnValue.get()
    }

    override fun unfreeze(vaultID: String) {
        cordaService.startFlow(::UnfreezeVaultFlow.curry(vaultID))
            .returnValue.get()
    }

    override fun updateAgencyCert(vaultID: String, newAgencyCert: CertInfo) {
        cordaService.startFlow(::UpdateAgencyCertFlow.curry(vaultID, newAgencyCert.toKotlin()))
            .returnValue.get()
    }

    override fun updateVaultCert(vaultID: String, newVaultCert: CertInfo) {
        cordaService.startFlow(::UpdateVaultCertFlow.curry(vaultID, newVaultCert.toKotlin()))
            .returnValue.get()
    }
}
