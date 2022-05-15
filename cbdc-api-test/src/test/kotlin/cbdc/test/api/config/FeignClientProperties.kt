package cbdc.test.api.config

import okhttp3.HttpUrl
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.context.annotation.Configuration

@Configuration
@ConfigurationProperties(prefix = "cbdc.test.feign")
data class FeignClientProperties(
    var url: String
) {
    constructor() : this("")

    private val host
        get() = HttpUrl.parse(url) ?: throw IllegalStateException()

    fun url(path: String) = host.resolve(path) ?: throw IllegalStateException()
}
