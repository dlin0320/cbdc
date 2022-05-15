package cbdc.test.api.config

import gov.cbc.cbdc.utilities.client.AdminClient
import gov.cbc.cbdc.utilities.client.CBClient
import gov.cbc.cbdc.utilities.client.ContractClient
import gov.cbc.cbdc.utilities.client.LedgerClient
import gov.cbc.cbdc.utilities.client.NumberClient
import gov.cbc.cbdc.utilities.client.OtpClient
import gov.cbc.cbdc.utilities.client.PlanClient
import gov.cbc.cbdc.utilities.client.TestClient
import gov.cbc.cbdc.utilities.client.TransactionClient
import gov.cbc.cbdc.utilities.client.VaultClient
import gov.cbc.cbdc.utilities.client.WalletClient
import org.springframework.cloud.openfeign.FeignClient

object FeignClientsCorda {
    @FeignClient(contextId = "test", name = "corda", url = "\${cbdc.test.feign.url}", configuration = [FeignClientConfiguration::class])
    interface FTestClient : TestClient

    @FeignClient(contextId = "vault", name = "corda", url = "\${cbdc.test.feign.url}", configuration = [FeignClientConfiguration::class])
    interface FVaultClient : VaultClient

    @FeignClient(contextId = "wallet", name = "corda", url = "\${cbdc.test.feign.url}", configuration = [FeignClientConfiguration::class])
    interface FWalletClient : WalletClient

    @FeignClient(contextId = "admin", name = "corda", url = "\${cbdc.test.feign.url}", configuration = [FeignClientConfiguration::class])
    interface FAdminClient : AdminClient

    @FeignClient(contextId = "transaction", name = "corda", url = "\${cbdc.test.feign.url}", configuration = [FeignClientConfiguration::class])
    interface FTransactionClient : TransactionClient

    @FeignClient(contextId = "contract", name = "corda", url = "\${cbdc.test.feign.url}", configuration = [FeignClientConfiguration::class])
    interface FContractClient : ContractClient

    @FeignClient(contextId = "ledger", name = "corda", url = "\${cbdc.test.feign.url}", configuration = [FeignClientConfiguration::class])
    interface FLedgerClient : LedgerClient

    @FeignClient(contextId = "cb", name = "corda", url = "\${cbdc.test.feign.url}", configuration = [FeignClientConfiguration::class])
    interface FCBClient : CBClient

    @FeignClient(contextId = "otp", name = "corda", url = "\${cbdc.test.feign.url}", configuration = [FeignClientConfiguration::class])
    interface FOtpClient : OtpClient

    @FeignClient(contextId = "number", name = "corda", url = "\${cbdc.test.feign.url}", configuration = [FeignClientConfiguration::class])
    interface FNumberClient : NumberClient

    @FeignClient(contextId = "plan", name = "corda", url = "\${cbdc.test.feign.url}", configuration = [FeignClientConfiguration::class])
    interface FPlanClient : PlanClient
}
