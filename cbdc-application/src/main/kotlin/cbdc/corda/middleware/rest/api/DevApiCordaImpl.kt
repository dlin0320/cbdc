package cbdc.corda.middleware.rest.api

import cbdc.corda.flow.DummyFlow
import cbdc.corda.flow.DummyFlowWithSimpleBroadcast
import cbdc.corda.flow.InitialiseCBVaultFlow
import cbdc.corda.flow.NewVaultK
import cbdc.corda.flow.RemoveVaultFlow
import cbdc.corda.flow.RemoveWalletFlow
import cbdc.corda.flow.vault.CommonQueries.allDummy
import cbdc.corda.flow.vault.CommonQueries.queryDummy
import cbdc.corda.middleware.core.service.CordaService
import cbdc.corda.middleware.utils.curry
import gov.cbc.cbdc.utilities.client.TestClient
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
class DevApiCordaImpl @Autowired constructor(
    private val cordaService: CordaService
) : TestClient {
    override fun addCentralBank() {
        cordaService.startFlow(::InitialiseCBVaultFlow.curry(NewVaultK(null, null))).returnValue.get()
    }

    override fun initDatabase() { /* no-ops */ }

    override fun removeVault(vaultID: String) {
        cordaService.startFlow(::RemoveVaultFlow.curry(vaultID))
    }

    override fun removeWallet(walletID: String) {
        cordaService.startFlow(::RemoveWalletFlow.curry(walletID))
    }

    override fun setVaultBalance(vaultID: String?, balance: Long) {
        TODO("Not yet implemented")
    }

    override fun setWalletBalance(walletID: String?, balance: Long) {
        TODO("Not yet implemented")
    }

    @PostMapping("dummy/broadcast/{id}")
    fun dummyWithBroadcast(@PathVariable("id") id: String, @RequestBody notarise: Boolean) {
        cordaService.startFlow(::DummyFlow.curry(id, true, notarise))
    }

    @PostMapping("dummy/{id}")
    fun dummyWithoutBroadcast(
        @PathVariable("id") id: String,
        @RequestParam("notarise") notarise: Boolean?,
        @RequestParam("broadcast") broadcast: Boolean?,
        @RequestParam("simple") simple: Boolean?
    ) {
        val isSimple = simple ?: false
        val isBroadcast = broadcast ?: true
        val isNotarise = notarise ?: true
        if (isSimple) {
            if (!isBroadcast) {
                throw IllegalArgumentException("Simple = true, Broadcast = false is not allowed.")
            }
            cordaService.startFlow(::DummyFlowWithSimpleBroadcast.curry(id, isNotarise))
        } else {
            cordaService.startFlow(::DummyFlow.curry(id, isBroadcast, isNotarise))
        }
    }

    @GetMapping("dummy/{id}")
    fun getDummy(@PathVariable("id") id: String) {
        cordaService.rpcOps.queryDummy(id)
    }

    @GetMapping("dummy/all")
    fun allDummy() {
        cordaService.rpcOps.allDummy()
    }
}
