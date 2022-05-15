package cbdc.corda.middleware.testutils

import cbdc.corda.middleware.core.service.AbstractSimpleCordaService
import cbdc.corda.middleware.core.service.CordaService
import cbdc.corda.middleware.utils.PendingCall
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.doAnswer
import com.nhaarman.mockitokotlin2.mock
import net.corda.core.concurrent.CordaFuture
import net.corda.core.flows.StateMachineRunId
import net.corda.core.messaging.FlowHandle
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Primary
import java.util.UUID
import java.util.concurrent.CompletableFuture
import java.util.concurrent.TimeUnit

@TestConfiguration
open class DefaultSpringBootTestConfiguration {
    @MockBean private lateinit var cordaSvc: AbstractSimpleCordaService
}

@TestConfiguration
open class MockCordaServiceConfiguration {
    companion object {
        private val logger: Logger = LoggerFactory.getLogger(MockCordaServiceConfiguration::class.java)
    }

    @Primary
    @Bean
    open fun mockCordaService(): CordaService {
        return mock {
            on { startFlow(any<PendingCall<*>>()) } doAnswer {
                val pendingCall = it.arguments[0]
                logger.debug("Mocking corda service startFlow($pendingCall)")
                DummyFlowHandle(pendingCall)
            }
        }
    }

    class DummyFlowHandle(
        private vararg val arguments: Any
    ) : FlowHandle<String> {
        override val id: StateMachineRunId
            get() = StateMachineRunId(UUID.randomUUID())

        override val returnValue: CordaFuture<String>
            get() = DummyCordaFuture("dummy flow handle with arguments ${arguments.toList()}}")

        override fun close() {
        }

        class DummyCordaFuture(
            private val dummyMessage: String
        ) : CordaFuture<String> {
            override fun cancel(p0: Boolean): Boolean {
                return true
            }

            override fun get(): String {
                return dummyMessage
            }

            override fun get(p0: Long, p1: TimeUnit): String {
                return dummyMessage
            }

            override fun isCancelled(): Boolean {
                return true
            }

            override fun isDone(): Boolean {
                return true
            }

            override fun <W> then(callback: (CordaFuture<String>) -> W) {
                TODO("Not yet implemented")
            }

            override fun toCompletableFuture(): CompletableFuture<String> {
                TODO("Not yet implemented")
            }
        }
    }
}
