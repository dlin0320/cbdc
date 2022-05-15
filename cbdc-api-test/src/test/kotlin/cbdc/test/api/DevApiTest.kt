package cbdc.test.api

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.junit.jupiter.SpringExtension

@ExtendWith(SpringExtension::class)
@SpringBootTest(
    classes = [ApiTestClient::class],
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT
)
class DevApiTest : BaseApiTest() {
    @Test
    fun init() {
        testClient.addCentralBank()
    }

    @Test
    fun `dummy with broadcast`() {
        val max = 100
        for (i in 1..max) {
            testClient.dummyWithBroadcast("$i", true)
        }
    }

    @Test
    fun `dummy without broadcast`() {
        val max = 100
        for (i in 1..max) {
            testClient.dummyWithoutBroadcast("$i", true)
        }
    }
}
