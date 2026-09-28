package com.openregulatory.eudamedsearch

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.openregulatory.eudamedsearch.nav.EudamedNavGraph
import com.openregulatory.eudamedsearch.ui.theme.EudamedSearchTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            EudamedSearchTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    EudamedNavGraph()
                }
            }
        }
    }
}
