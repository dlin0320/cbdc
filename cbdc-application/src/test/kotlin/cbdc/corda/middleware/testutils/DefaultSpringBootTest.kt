package cbdc.corda.middleware.testutils

import cbdc.corda.middleware.CbdcApplication
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.Import

@SpringBootTest(
    classes = [CbdcApplication::class],
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT
)
@Import(DefaultSpringBootTestConfiguration::class)
annotation class DefaultSpringBootTest
