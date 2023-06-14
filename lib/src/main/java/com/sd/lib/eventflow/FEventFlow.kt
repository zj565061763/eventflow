package com.sd.lib.eventflow

import android.util.Log
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.FlowCollector
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch

object FEventFlow {
    private val _scope = MainScope()
    private val _flowHolder: MutableMap<Class<*>, MutableSharedFlow<*>> = hashMapOf()

    var isDebug = false

    @JvmStatic
    fun post(event: Any) {
        synchronized(this@FEventFlow) {
            val clazz = event.javaClass
            val flow = _flowHolder[clazz] ?: return

            flow as MutableSharedFlow<Any>
            logMsg { "post -----> $event (${flow.subscriptionCount.value})" }
            flow.tryEmit(event)
        }
    }

    fun <T> flow(clazz: Class<T>): Flow<T> {
        return flowOf(clazz).asSharedFlow()
    }

    @Suppress("UNCHECKED_CAST")
    private fun <T> flowOf(clazz: Class<T>): MutableSharedFlow<T> {
        return synchronized(this@FEventFlow) {
            val cache = _flowHolder[clazz]
            if (cache != null) return cache as MutableSharedFlow<T>

            MutableSharedFlow<T>(
                extraBufferCapacity = 1,
                onBufferOverflow = BufferOverflow.DROP_OLDEST,
            ).also {
                _flowHolder[clazz] = it
                logMsg { "+++++ ${clazz.name} eventTypeSize:${_flowHolder.size}" }
            }
        }.also { flow ->
            _scope.launch {
                delay(1000)
                flow.subscriptionCount.collect {
                    logMsg { "${clazz.name} subscriptionCount $it" }
                    if (it <= 0) {
                        synchronized(this@FEventFlow) {
                            _flowHolder.remove(clazz)
                            logMsg { "----- ${clazz.name} eventTypeSize:${_flowHolder.size}" }
                        }
                    }
                }
            }
        }
    }
}

fun fEventPost(event: Any) {
    FEventFlow.post(event)
}

suspend inline fun <reified T> fEventCollect(collector: FlowCollector<T>) {
    fEventFlow<T>().collect(collector)
}

inline fun <reified T> fEventFlow(): Flow<T> {
    return FEventFlow.flow(T::class.java)
}

private inline fun logMsg(block: () -> String) {
    if (FEventFlow.isDebug) {
        Log.i("FEventFlow", block())
    }
}