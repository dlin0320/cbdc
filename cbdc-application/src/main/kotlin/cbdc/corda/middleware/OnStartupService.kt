package cbdc.corda.middleware

import cbdc.corda.middleware.core.service.AbstractSimpleCordaService
import net.corda.core.utilities.loggerFor
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.web.context.WebServerInitializedEvent
import org.springframework.context.ApplicationListener
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class OnStartupService(
    @Autowired(required = false) private val basicCordaService: AbstractSimpleCordaService? = null
) : ApplicationListener<WebServerInitializedEvent> {

    private val log = loggerFor<OnStartupService>()

    // listening on WebServerInitializedEvent ensures we execute on web application context (which runs jetty server)
    @Transactional
    override fun onApplicationEvent(event: WebServerInitializedEvent?) {
        if (basicCordaService != null) {
            connectToCorda(basicCordaService)
        }
    }

    private fun connectToCorda(corda: AbstractSimpleCordaService) {
        log.info("Connecting to corda...")
        if (corda.registered) {
            log.info("Corda was already connected!")
        } else {
            corda.openConnection()
            log.info("Corda connected!")
        }
    }
}
