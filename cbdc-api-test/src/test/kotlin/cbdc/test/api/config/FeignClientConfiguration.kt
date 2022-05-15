package cbdc.test.api.config

import feign.FeignException
import feign.RequestInterceptor
import feign.RetryableException
import feign.Retryer
import feign.codec.ErrorDecoder
import feign.okhttp.OkHttpClient
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.scheduling.TaskScheduler

@Configuration
class FeignClientConfiguration @Autowired constructor(
    private val clientProperties: FeignClientProperties,
    private val taskScheduler: TaskScheduler
) {
    private val log = LoggerFactory.getLogger(FeignClientConfiguration::class.java)

    // use feign-okhttp to enable HTTP/2.0
    @Bean
    fun client() = OkHttpClient()

    @Bean
    fun reqInterceptor() = RequestInterceptor {}

    @Bean
    fun errorDecoder() = ErrorDecoder { s, res ->
        val ex = ErrorDecoder.Default().decode(s, res)

        return@ErrorDecoder when {
            ex is RetryableException -> ex
            res.status() == 403 -> {
                val req = res.request()
                val message = "Authentication failed. Access token might have been invalidated due to refresh."
                RetryableException(403, message, req.httpMethod(), null, req)
            }
            else -> ex
        }
    }

    @Bean
    internal fun retryer() = FeignRetryer()
}

internal class FeignRetryer : Retryer.Default(10000, 5000, 1) {
    override fun continueOrPropagate(e: RetryableException?) {
        when (e) {
            is FeignException.NotFound -> {
                // throwing exception will stop retrying
                throw e
            }
            else -> {
                // doing nothing will continue
            }
        }
    }
}
