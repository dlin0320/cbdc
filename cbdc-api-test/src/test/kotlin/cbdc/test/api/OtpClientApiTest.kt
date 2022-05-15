package cbdc.test.api

import gov.cbc.cbdc.utilities.domain.entity.OtpData
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.junit.jupiter.SpringExtension
import java.time.Instant
import java.util.UUID

@ExtendWith(SpringExtension::class)
@SpringBootTest(
    classes = [ApiTestClient::class],
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT
)
class OtpClientApiTest : BaseApiTest() {

    private val otp: String
        get() = UUID.randomUUID().toString()

    private val now: Instant
        get() = Instant.now()

    @BeforeEach
    fun init() {
        initCB()
        createWallets()
    }

    @Test
    fun `add otp and delete`() {
        val o1 = otp
        val t1 = now
        otpClient.upsert(wallet1ID, OtpData(o1, t1))
        otpClient.getAndDelete(wallet1ID, o1).apply {
            assertEquals(o1, this.otp)
            assertEquals(t1, this.createTime)
        }
        otpClient.getAndDelete(wallet1ID, o1).apply {
            assertEquals(null, this)
        }
    }
}
