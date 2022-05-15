@file:Suppress("unused")

package cbdc.corda.middleware.utils

import net.corda.core.flows.FlowLogic
import kotlin.reflect.KClass
import kotlin.reflect.KFunction0
import kotlin.reflect.KFunction1
import kotlin.reflect.KFunction2
import kotlin.reflect.KFunction3
import kotlin.reflect.KFunction4
import kotlin.reflect.KFunction5
import kotlin.reflect.KFunction6

class PendingCall<R>(
    val logicType: Class<out FlowLogic<R>>,
    val args: Array<out Any?>,
    val returnType: KClass<*>? = null
) {
    companion object {
        inline fun <reified R> of(logicType: Class<out FlowLogic<R>>, vararg args: Any?): PendingCall<R> {
            return PendingCall<R>(logicType, args, R::class)
        }
    }
}

inline fun <reified T : FlowLogic<R>, reified R> KFunction0<T>.curry() =
    PendingCall.of<R>(T::class.java)
inline fun <reified T : FlowLogic<R>, reified R, A> KFunction1<A, T>.curry(arg0: A) =
    PendingCall.of<R>(T::class.java, arg0)
inline fun <reified T : FlowLogic<R>, reified R, A, B> KFunction2<A, B, T>.curry(arg0: A, arg1: B) =
    PendingCall.of<R>(T::class.java, arg0, arg1)
inline fun <reified T : FlowLogic<R>, reified R, A, B, C> KFunction3<A, B, C, T>.curry(arg0: A, arg1: B, arg2: C) =
    PendingCall.of<R>(T::class.java, arg0, arg1, arg2)
inline fun <reified T : FlowLogic<R>, reified R, A, B, C, D> KFunction4<A, B, C, D, T>.curry(arg0: A, arg1: B, arg2: C, arg3: D) =
    PendingCall.of<R>(T::class.java, arg0, arg1, arg2, arg3)
inline fun <reified T : FlowLogic<R>, reified R, A, B, C, D, E> KFunction5<A, B, C, D, E, T>.curry(arg0: A, arg1: B, arg2: C, arg3: D, arg4: E) =
    PendingCall.of<R>(T::class.java, arg0, arg1, arg2, arg3, arg4)
inline fun <reified T : FlowLogic<R>, reified R, A, B, C, D, E, F> KFunction6<A, B, C, D, E, F, T>.curry(arg0: A, arg1: B, arg2: C, arg3: D, arg4: E, arg5: F) =
    PendingCall.of<R>(T::class.java, arg0, arg1, arg2, arg3, arg4, arg5)
