package com.semorka.lyryx

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.semorka.lyryx.core.ui.theme.LyryxTheme
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            LyryxTheme {
                Scaffold { paddingValues ->
                    Surface(Modifier.padding(paddingValues), color = MaterialTheme.colorScheme.background) {
                        LyryxApp()
                    }
                }
            }
        }
    }
}