package com.sd.demo.eventflow

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.sd.demo.eventflow.databinding.ActivityMainBinding
import com.sd.lib.eventflow.FEventFlow
import com.sd.lib.eventflow.FEventObserver
import com.sd.lib.eventflow.fEventFlow
import com.sd.lib.eventflow.fEventPost
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {
    private val _binding by lazy { ActivityMainBinding.inflate(layoutInflater) }
    private val _scope = MainScope()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(_binding.root)
        _binding.btnPost.setOnClickListener {
            fEventPost(TestEvent())
        }

        _scope.launch {
            fEventFlow<TestEvent>().collect {
                logMsg { "onEvent flow $it" }
            }
        }

        _scope.launch {
            delay(5000)
            logMsg { "register observer" }
            _eventObserver.register()
        }
    }

    private val _eventObserver = object : FEventObserver<TestEvent>() {
        override fun onEvent(event: TestEvent) {
            logMsg { "onEvent observer $event" }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        _scope.cancel()
        _eventObserver.unregister()
    }

    companion object {
        init {
            FEventFlow.isDebug = true
        }
    }
}

inline fun logMsg(block: () -> String) {
    Log.i("eventflow-demo", block())
}