package com.sd.demo.eventflow

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.sd.demo.eventflow.ui.theme.AppTheme
import com.sd.lib.eventflow.FEventFlow
import com.sd.lib.eventflow.fEventFlow
import com.sd.lib.eventflow.fEventPost
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    private val _scope = MainScope()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            AppTheme {
                Content()
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        _scope.cancel()
    }

    init {
        _scope.launch {
            fEventFlow<TestEvent>().collect {
                logMsg { "onEvent activity $it" }
            }
        }
    }

    companion object {
        init {
            FEventFlow.isDebug = true
        }
    }
}

@Composable
private fun Content() {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(5.dp)
    ) {
        Button(
            onClick = {
                fEventPost(TestEvent())
            }
        ) {
            Text(text = "button")
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun DefaultPreview() {
    AppTheme {
        Content()
    }
}

inline fun logMsg(block: () -> String) {
    Log.i("FEventFlow-demo", block())
}