package com.sd.lib.eventflow

import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.Job
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import java.lang.reflect.ParameterizedType

abstract class FEventObserver<T> {
    private val _eventClass: Class<*>
    private val _scope = MainScope()

    private var _registerJob: Job? = null

    init {
        val clazz = findSubClass()
        val types = (clazz.genericSuperclass as ParameterizedType).actualTypeArguments
        _eventClass = if (types.isNotEmpty()) {
            types[0] as Class<*>
        } else {
            error("Generic type not found.")
        }
    }

    private fun findSubClass(): Class<*> {
        var clazz: Class<*> = javaClass
        while (true) {
            if (clazz.superclass == FEventObserver::class.java) break
            clazz = clazz.superclass
        }
        return clazz
    }

    @Suppress("UNCHECKED_CAST")
    @Synchronized
    fun register() {
        if (_registerJob != null) return
        _scope.launch(start = CoroutineStart.LAZY) {
            FEventFlow.flow(_eventClass).collect {
                onEvent(it as T)
            }
        }.let { job ->
            _registerJob = job
            job.start()
        }
    }

    @Synchronized
    fun unregister() {
        _registerJob?.cancel()
        _registerJob = null
    }

    abstract fun onEvent(event: T)
}