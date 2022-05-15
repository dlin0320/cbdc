package cbdc.corda.middleware

import org.springframework.boot.SpringApplication
import org.springframework.boot.SpringBootConfiguration
import org.springframework.boot.autoconfigure.EnableAutoConfiguration
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.autoconfigure.domain.EntityScan
import org.springframework.boot.autoconfigure.jms.artemis.ArtemisAutoConfiguration
import org.springframework.context.annotation.ComponentScan
import org.springframework.scheduling.annotation.EnableAsync

@EntityScan(basePackages = ["cbdc.corda.middleware"])
@ComponentScan(basePackages = ["cbdc.corda.middleware"])
@SpringBootConfiguration
@EnableAsync
@SpringBootApplication
@EnableAutoConfiguration(exclude = [ArtemisAutoConfiguration::class])
open class CbdcApplication {
    companion object {
        @JvmStatic
        fun main(args: Array<String>) {
            SpringApplication.run(CbdcApplication::class.java, *args)
        }
    }
}
