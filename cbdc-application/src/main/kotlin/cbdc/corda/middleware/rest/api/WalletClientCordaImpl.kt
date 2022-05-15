package cbdc.corda.middleware.rest.api

import cbdc.corda.flow.DisableWalletFlow
import cbdc.corda.flow.FreezeWalletFlow
import cbdc.corda.flow.InitialiseWalletFlow
import cbdc.corda.flow.UnfreezeWalletFlow
import cbdc.corda.flow.UpdateCertFlow
import cbdc.corda.flow.UpdateDisplayNameFlow
import cbdc.corda.flow.UpdateDivDataFlow
import cbdc.corda.flow.UpdateLimitFlow
import cbdc.corda.flow.UpdatePubKeyFlow
import cbdc.corda.flow.UpgradeWalletFlow
import cbdc.corda.flow.vault.CommonQueries.getAllWallet
import cbdc.corda.flow.vault.CommonQueries.queryBalance
import cbdc.corda.flow.vault.CommonQueries.queryWallet
import cbdc.corda.middleware.core.service.CordaService
import cbdc.corda.middleware.rest.api.convert.toJava
import cbdc.corda.middleware.rest.api.convert.toKotlin
import cbdc.corda.middleware.utils.WalletAlreadyExistsException
import cbdc.corda.middleware.utils.curry
import gov.cbc.cbdc.utilities.client.WalletClient
import gov.cbc.cbdc.utilities.domain.entity.Limit
import gov.cbc.cbdc.utilities.domain.entity.account.NewWallet
import gov.cbc.cbdc.utilities.domain.entity.account.Wallet
import gov.cbc.cbdc.utilities.domain.entity.account.WalletSetting
import gov.cbc.cbdc.utilities.domain.entity.account.WalletStatus
import gov.cbc.cbdc.utilities.domain.enums.WalletType
import org.springframework.web.bind.annotation.RestController

@RestController
class WalletClientCordaImpl(
    private val cordaService: CordaService
) : WalletClient {
    private fun walletExists(walletID: String) {
        if (cordaService.rpcOps.queryWallet(walletID) != null) throw WalletAlreadyExistsException(walletID)
    }

    override fun getStatus(walletID: String): WalletStatus? {
        val wallet = cordaService.rpcOps.queryWallet(walletID) ?: return null
        val balance = cordaService.rpcOps.queryBalance(walletID)

        return wallet.copy(balance = balance.quantity).status.toJava()
    }

    override fun updateDivData(walletID: String, divData: String) {
        cordaService.startFlow(::UpdateDivDataFlow.curry(walletID, divData))
    }

    override fun getTotal(walletID: String): Wallet? {
        val status = getStatus(walletID) ?: return null
        val setting = getSetting(walletID) ?: return null

        return Wallet(status, setting)
    }

    override fun updatePubKey(walletID: String, newWalletPubKey: ByteArray) {
        cordaService.startFlow(::UpdatePubKeyFlow.curry(walletID, newWalletPubKey))
    }

    override fun freeze(walletID: String) {
        cordaService.startFlow(::FreezeWalletFlow.curry(walletID))
    }

    override fun updateLimit(walletID: String, limit: Limit) {
        cordaService.startFlow(::UpdateLimitFlow.curry(walletID, limit.toKotlin()))
    }

    override fun extendCert(walletID: String, newWalletCert: ByteArray) {
        cordaService.startFlow(::UpdateCertFlow.curry(walletID, newWalletCert))
    }

    override fun checkExist(phoneNumber: String, walletType: WalletType): Boolean {
        val type = when (walletType) {
            WalletType.REGISTERED -> listOf(cbdc.corda.state.WalletTypeK.REGISTERED)
            WalletType.ANONYMOUS -> listOf(cbdc.corda.state.WalletTypeK.ANONYMOUS)
        }

        return cordaService.rpcOps.getAllWallet().any { type.contains(it.typeK) }
    }

    override fun add(walletID: String, newWallet: NewWallet) {
        walletExists(walletID)

        cordaService.startFlow(::InitialiseWalletFlow.curry(walletID, newWallet.toKotlin())).returnValue.get()
    }

    override fun updateCert(walletID: String, newWalletCert: ByteArray) {
        cordaService.startFlow(::UpdateCertFlow.curry(walletID, newWalletCert))
    }

    override fun extendPubKey(walletID: String, newWalletPubKey: ByteArray) {
        cordaService.startFlow(::UpdatePubKeyFlow.curry(walletID, newWalletPubKey))
    }

    override fun getSetting(walletID: String): WalletSetting? {
        val wallet = cordaService.rpcOps.queryWallet(walletID) ?: return null
        return wallet.setting.toJava()
    }

    override fun updateDisplayName(walletID: String, newDn: String) {
        cordaService.startFlow(::UpdateDisplayNameFlow.curry(walletID, newDn))
    }

    override fun unfreeze(walletID: String) {
        cordaService.startFlow(::UnfreezeWalletFlow.curry(walletID))
    }

    override fun upgrade(walletID: String) {
        cordaService.startFlow(::UpgradeWalletFlow.curry(walletID))
    }

    override fun disable(walletID: String) {
        cordaService.startFlow(::DisableWalletFlow.curry(walletID))
    }
}
