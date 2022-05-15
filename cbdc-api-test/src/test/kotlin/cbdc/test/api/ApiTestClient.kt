package cbdc.test.api

import org.springframework.boot.SpringBootConfiguration
import org.springframework.boot.autoconfigure.EnableAutoConfiguration
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.autoconfigure.jms.artemis.ArtemisAutoConfiguration
import org.springframework.cloud.openfeign.EnableFeignClients
import org.springframework.context.annotation.ComponentScan
import org.springframework.scheduling.annotation.EnableAsync
import org.springframework.scheduling.annotation.EnableScheduling

@ComponentScan(
    basePackages = [
        "cbdc.test.api",
        "cbdc.corda.middleware.rest.serialize" // todo: move to shared module
    ]
)
@SpringBootConfiguration
@EnableAsync
@EnableScheduling
@EnableFeignClients
@SpringBootApplication
@EnableAutoConfiguration(exclude = [ArtemisAutoConfiguration::class])
open class ApiTestClient
