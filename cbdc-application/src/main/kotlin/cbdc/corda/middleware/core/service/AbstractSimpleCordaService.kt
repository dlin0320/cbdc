package cbdc.corda.middleware.core.service

import net.corda.client.rpc.CordaRPCClient
import net.corda.client.rpc.CordaRPCClientConfiguration
import net.corda.client.rpc.CordaRPCConnection
import net.corda.client.rpc.GracefulReconnect
import net.corda.core.contracts.StateAndRef
import net.corda.core.identity.Party
import net.corda.core.messaging.ClientRpcSslOptions
import net.corda.core.messaging.CordaRPCOps
import net.corda.core.utilities.NetworkHostAndPort
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.core.env.Environment
import org.springframework.stereotype.Service
import rx.Observable
import rx.subjects.PublishSubject
import java.nio.file.Path
import java.nio.file.Paths

@Service
abstract class AbstractSimpleCordaService : CordaService {
    protected val log = LoggerFactory.getLogger(CordaService::class.java)!!

    @Autowired
    private lateinit var env: Environment

    private val rpcUrl: String
        get() = env.getProperty("corda.rpc.url", "localhost:10006")

    private val rpcUser: String
        get() = env.getProperty("corda.rpc.username", "user1")

    private val rpcPassword: String
        get() = env.getProperty("corda.rpc.password", "test")

    private val usetls = false

    private val certsPath: Path =
        Paths.get("rpccerts")

    private val certsPass = "DEFAULT"

    private var connection: CordaRPCConnection? = null

    private val newStateUpdatesSubject: PublishSubject<StateAndRef<*>> =
        PublishSubject.create()

    // This observable won't work if there are multiple Spring instances...
    override val newStateUpdates: Observable<StateAndRef<*>> = newStateUpdatesSubject.asObservable()

    // register your runners here
    override val onConnect = mutableListOf<(CordaRPCOps) -> Unit>()

    init {
        log.debug("[BaseCordaService] Registering Corda OnConnection Handler")
        onConnect {
            log.debug("[BaseCordaService] Corda connection available, initializing!")
        }
    }

    fun onConnect(handler: (CordaRPCOps) -> Unit) {
        synchronized(onConnect) {
            onConnect += handler
            if (connection != null) {
                log.debug("[onConnect] Already connected to Corda, immediately triggering handler.")
                handler(rpcOps)
            }
        }
    }

    override val ourIdentity: Party by lazy { rpcOps.nodeInfo().legalIdentities[0] }

    override val rpcOps: CordaRPCOps
        @Synchronized
        get() {
            if (connection == null) {
                // Verify the node URL and extract as a HostAndPort
                val nodeHP = NetworkHostAndPort.parse(rpcUrl)
                log.info("Attempting upstream RPC to node: '$nodeHP'")
                val client = CordaRPCClient(
                    hostAndPort = nodeHP,
                    configuration = CordaRPCClientConfiguration(
                        maxReconnectAttempts = -1
                    ),
                    sslConfiguration = if (usetls) ClientRpcSslOptions(
                        certsPath,
                        certsPass
                    ) else null
                )

                // Start the RPC client using the first of the RPC users
                connection = client.start(rpcUser, rpcPassword, gracefulReconnect)

                log.info("Successful connection. Creating proxy...")
            }

            return connection!!.proxy
        }

    fun disconnect() {
        if (connected) {
            log.info("closing previous connection...")
            connection = null
        }

        log.info("Connection disconnected, attempting to reconnect...")
    }

    fun reconnect() {
        log.info("Successfully reconnected. Creating proxy...")
    }

    val gracefulReconnect = GracefulReconnect(
        onDisconnect = { disconnect() },
        onReconnect = { reconnect() },
        maxAttempts = -1
    )

    @Synchronized
    fun openConnection() {
        // Connected!

        synchronized(onConnect) {
            onConnect.forEach {
                try {
                    it(rpcOps)
                } catch (ex: Exception) {
                    log.error("[onConnect] Error caught while executing handler", ex)
                }
            }
        }
        registered = true
    }

    var registered = false

    val connected
        get() = (connection != null)
}
