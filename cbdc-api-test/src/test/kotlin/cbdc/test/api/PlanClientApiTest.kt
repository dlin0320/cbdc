package cbdc.test.api

import gov.cbc.cbdc.utilities.domain.entity.plan.Plan
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.junit.jupiter.SpringExtension

@ExtendWith(SpringExtension::class)
@SpringBootTest(
    classes = [ApiTestClient::class],
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT
)
class PlanClientApiTest : BaseApiTest() {

    @BeforeEach
    fun init() {
        initCB()
    }

    @Test
    fun `add and get`() {
        val plan = Plan(
            "operation",
            listOf<String>(),
            listOf<String>(),
            0,
            true,
            true,
            "remark"
        )
        planClient.add(plan)
        Thread.sleep(3000)
        planClient.get("operation").apply {
            assertEquals(this, plan)
        }
    }
}
