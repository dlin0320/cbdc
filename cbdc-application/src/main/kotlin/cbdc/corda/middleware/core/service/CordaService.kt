package cbdc.corda.middleware.core.service

import cbdc.corda.middleware.utils.PendingCall
import net.corda.core.contracts.StateAndRef
import net.corda.core.messaging.CordaRPCOps
import net.corda.core.messaging.FlowHandle
import org.springframework.stereotype.Service
import rx.Observable

@Service
interface CordaService : DLTService {
    val newStateUpdates: Observable<StateAndRef<*>>

    val rpcOps: CordaRPCOps

    val onConnect: MutableList<(CordaRPCOps) -> Unit>

    fun <R> startFlow(call: PendingCall<R>): FlowHandle<R> {
        @Suppress("SpreadOperator")
        return rpcOps.startFlowDynamic(call.logicType, *call.args)
    }

    /**
     * might add back later
     */

//    fun getFileName(hash: SecureHash): String

//    fun uploadAttachment(uploadedInputStream: InputStream, fileName: String?): SecureHash

//    fun downloadAttachment(hash: SecureHash, dest: OutputStream)

//    fun <T : ContractState> getState(ref: StateRef, clazz: KClass<T>, all: Boolean = false): StateAndRef<T>

//    fun <T : ContractState> getAllHistoricalStatesByRef(refs: Iterable<StateRef>, clazz: KClass<T>): Map<StateRef, StateAndRef<T>>

//    fun <T : ContractState> getAllStates(clazz: KClass<T>, paging: PageSpecification): List<StateAndRef<T>>

//    fun <T : ContractState> getAllStatesByLinearId(linearId: UniqueIdentifier, clazz: KClass<T>, paging: PageSpecification): List<StateAndRef<T>>

//    fun <T : ContractState> getLatestStateByLinearId(linearId: UniqueIdentifier, clazz: KClass<T>, all: Boolean = false): StateAndRef<T>

//    fun <T : LinearState> getAllLatestStatesByLinearId(linearIds: Iterable<UniqueIdentifier>, clazz: KClass<T>): Map<UniqueIdentifier, StateAndRef<T>>

//    fun requireWellKnownParty(anonymous: AbstractParty) =
//        rpcOps.wellKnownPartyFromAnonymous(anonymous) ?: throw IllegalStateException(
//            anonymous.toString()
//        )
}
