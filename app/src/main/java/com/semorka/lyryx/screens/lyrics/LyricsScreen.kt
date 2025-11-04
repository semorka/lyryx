package com.semorka.lyryx.screens.lyrics

import android.net.Uri
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.animateIntAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.semorka.lyryx.MockPlayerViewModel
import com.semorka.lyryx.PlayerViewModel
import com.semorka.lyryx.music.Music
import com.semorka.lyryx.ui.theme.LyryxTheme
import kotlinx.coroutines.delay
import kotlin.text.ifEmpty

@Composable
fun LyricsScreen(navController: NavController, music: Music, audioUri: Uri?, playerVm: PlayerViewModel, isPreview: Boolean = false) {
    if (isPreview) {
        LyricsScreenPreviewContent(music)
    } else {
        LyricsScreenRealContent(navController, music, audioUri, playerVm)
    }
}
@Composable
fun MovingTextCopy(originalText: String, isActive: Boolean) {
    var animate by remember { mutableStateOf(false) }

    val offset by animateDpAsState(
        targetValue = if (animate) 300f.dp else 0f.dp,
        animationSpec = tween(1500)
    )

    val alpha by animateFloatAsState(
        targetValue = if (animate) 0f else 0.5f,
        animationSpec = tween(1200)
    )

    LaunchedEffect(isActive) {
        if (isActive) {
            animate = true
            delay(1500)
            animate = false
        }
    }

    Text(
        text = originalText,
        fontSize = 20.sp,
        color = MaterialTheme.colorScheme.primary.copy(alpha = alpha),
        modifier = Modifier.offset(y = offset),
        textAlign = TextAlign.Center
    )
}

fun splitLyricIntoLines(lyrics: String, maxCharsPerLine: Int = 25): String {
    if (lyrics.length <= maxCharsPerLine) return lyrics

    val words = lyrics.split(" ")
    val lines = mutableListOf<String>()
    var currentLine = ""

    for (word in words) {
        if ("$currentLine $word".trim().length <= maxCharsPerLine) {
            currentLine = "$currentLine $word".trim()
        } else {
            if (currentLine.isNotEmpty()) {
                lines.add(currentLine)
            }
            currentLine = word
        }
    }

    if (currentLine.isNotEmpty()) {
        lines.add(currentLine)
    }

    return lines.joinToString("\n")
}