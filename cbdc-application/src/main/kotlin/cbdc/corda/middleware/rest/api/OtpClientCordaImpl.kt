package cbdc.corda.middleware.rest.api

import cbdc.corda.flow.UpdateOtpDataFlow
import cbdc.corda.flow.vault.CommonQueries.queryWallet
import cbdc.corda.middleware.core.service.CordaService
import cbdc.corda.middleware.rest.api.convert.toJava
import cbdc.corda.middleware.rest.api.convert.toKotlin
import cbdc.corda.middleware.utils.curry
import cbdc.corda.state.OtpDataK
import gov.cbc.cbdc.utilities.client.OtpClient
import gov.cbc.cbdc.utilities.domain.entity.OtpData
import org.springframework.web.bind.annotation.RestController

@RestController
class OtpClientCordaImpl(
    private val cordaService: CordaService
) : OtpClient {
    override fun getAndDelete(walletID: String, otp: String): OtpData? {
        val otpDataK = cordaService.rpcOps.queryWallet(walletID)?.otpData
        if (otpDataK != null) {
            val newOtp = OtpDataK(null, null)
            cordaService.startFlow(::UpdateOtpDataFlow.curry(walletID, newOtp))
        }

        return otpDataK?.toJava()
    }

    override fun upsert(walletID: String, otpData: OtpData) {
        cordaService.startFlow(::UpdateOtpDataFlow.curry(walletID, otpData.toKotlin()))
    }
}
