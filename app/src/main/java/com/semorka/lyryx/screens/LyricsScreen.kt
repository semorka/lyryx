package com.semorka.lyryx.screens

import android.media.MediaPlayer
import android.net.Uri
import android.util.Log
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.animateIntAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
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
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.semorka.lyryx.BeatDetector
import com.semorka.lyryx.music.Music
import com.semorka.lyryx.ui.theme.LyryxTheme
import kotlinx.coroutines.delay
import kotlin.text.ifEmpty

@Composable
fun LyricsScreen(navController: NavController, music: Music, audioUri: Uri?, isPreview: Boolean = false) {
    val context = LocalContext.current
    var mediaPlayer by remember { mutableStateOf<MediaPlayer?>(null) }
    var currentTime by remember { mutableStateOf(0L) }
    var isPlaying by remember { mutableStateOf(false) }

    val beatDetector = if (!isPreview) remember { BeatDetector() } else null

    val onsetTimestamps = remember { mutableStateListOf<Long>() }
    var lastOnsetTime by remember { mutableStateOf(0L) }

    val syncedLines = remember(music.syncedLyrics) {
        music.syncedLyrics?.let { text ->
            text.lines().mapNotNull { line ->
                val timeMatch = Regex("\\[(\\d+):(\\d+)\\.(\\d+)\\]").find(line)
                if (timeMatch != null) {
                    val (min, sec, centi) = timeMatch.destructured
                    val timeMs = min.toLong() * 60_000 + sec.toLong() * 1_000 + centi.toLong() * 10
                    val cleanText = line.replace(Regex("\\[\\d+:\\d+\\.\\d+\\]\\s*"), "").trim()
                    timeMs to cleanText
                    // TODO Сделать обработку ухода строки
//                    if (cleanText.isNotEmpty()) {
//                        timeMs to cleanText
//                    } else null
                } else null
            }.sortedBy { it.first }
        } ?: emptyList()
    }

    val currentLyric by remember(currentTime, syncedLines) {
        derivedStateOf {
            syncedLines
                .takeWhile { it.first <= currentTime }
                .lastOrNull()
                ?.second ?: ""
        }
    }

    var previousLyric by remember { mutableStateOf("") }
    var showMovingText by remember { mutableStateOf(false) }
    var movingText by remember { mutableStateOf("") }

    LaunchedEffect(currentLyric) {
        if (currentLyric.isNotEmpty() && currentLyric != previousLyric) {
            movingText = previousLyric
            if (previousLyric.isNotEmpty()) {
                showMovingText = true
                delay(600)
                showMovingText = false
            }
            previousLyric = currentLyric
        }
    }

    val isOnsetActive by remember(currentTime, lastOnsetTime) {
        derivedStateOf {
            currentTime - lastOnsetTime < 150
        }
    }

    var lineChanged by remember { mutableStateOf(false) }

    if (isPreview) {
        LaunchedEffect(Unit) {
            while (true) {
                delay(100)
                currentTime += 100

                if (currentTime % 800 == 0L) {
                    lastOnsetTime = currentTime
                    onsetTimestamps.add(currentTime)
                }
                if (currentTime % 1000 == 0L) {
                    lineChanged = true
                    delay(500)
                    lineChanged = false
                }

                if (currentTime >= 3000){
                    currentTime = 0
                }
            }
        }
    } else {
        LaunchedEffect(currentLyric) {
            if (currentLyric.isNotEmpty() && currentLyric != "♪") {
                lineChanged = true
                delay(500)
                lineChanged = false
            }
        }

        LaunchedEffect(isPlaying) {
            if (isPlaying) {
                while (isPlaying) {
                    delay(50)
                    mediaPlayer?.let { player ->
                        currentTime = player.currentPosition.toLong()
                    }
                }
            }
        }

        LaunchedEffect(audioUri) {
            if (audioUri != null && mediaPlayer == null) {
                try {
                    val player = MediaPlayer().apply {
                        setDataSource(context, audioUri)
                        prepare()
                        setOnPreparedListener {
                            start()
                            isPlaying = true

                            beatDetector?.start(this) { beatTime ->
                                // Просто обновляем lastOnsetTime - currentTime уже обновляется в другом месте
                                lastOnsetTime = beatTime
                                onsetTimestamps.add(beatTime)
                                Log.d("BEAT_UI", "Бит принят в UI: $beatTime")
                            }
                        }
                        setOnCompletionListener {
                            isPlaying = false
                        }
                    }
                    mediaPlayer = player
                } catch (e: Exception) {
                    Log.e("LYRYX", "Ошибка: ${e.message}")
                }
            }
        }

        DisposableEffect(Unit) {
            onDispose {
                isPlaying = false
                beatDetector?.stop()
                mediaPlayer?.release()
                mediaPlayer = null
            }
        }
    }

    val animatedTextSize by animateFloatAsState(
        targetValue = when {
            isOnsetActive -> if (currentLyric.length > 25) 23f else 29f
            lineChanged -> if (currentLyric.length > 25) 24f else 29f
            else -> if (currentLyric.length > 25) 20f else 28f
        },
        animationSpec = tween(100)
    )

    val animatedTextColor by animateColorAsState(
        targetValue = when {
            isOnsetActive -> MaterialTheme.colorScheme.primary.copy(0.8f)
            lineChanged -> MaterialTheme.colorScheme.primary.copy(0.5f)
            else -> MaterialTheme.colorScheme.primary
        },
        animationSpec = tween(150)
    )

    val animatedTextWeight by animateIntAsState(
        targetValue = when {
            isOnsetActive -> 800
            lineChanged -> 1000
            else -> 400
        },
        animationSpec = tween(200)
    )

    val animatedBoxColor by animateColorAsState(
        targetValue = if(lineChanged) Color.Black.copy(0.03f)
        else Color.Transparent,
        animationSpec = tween(400)
    )

    val formattedTime = remember(currentTime) {
        String.format("%02d:%02d", currentTime / 60_000, (currentTime % 60_000) / 1_000)
    }

    Box(Modifier.fillMaxSize().padding(16.dp)) {
        Row(Modifier.fillMaxWidth()) {
            Column(Modifier.weight(1f)) {
                Text(music.name, fontSize = 20.sp)
                Text(music.artistName, fontSize = 16.sp, color = Color.Gray)
            }
            Text(formattedTime)
        }

        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Box(
                modifier = Modifier
                    .background(animatedBoxColor)
            ) {
                Box(contentAlignment = Alignment.Center){
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = splitLyricIntoLines(currentLyric.ifEmpty { "♪" }),
                            fontSize = animatedTextSize.sp,
                            color = animatedTextColor,
                            textAlign = TextAlign.Center,
                            fontWeight = FontWeight(animatedTextWeight),
                            modifier = Modifier.fillMaxWidth()
                        )

                        if (isPreview) {
                            Spacer(Modifier.height(16.dp))
                            Text(
                                text = "Превью | Бит: $isOnsetActive | Смена: $lineChanged",
                                fontSize = 12.sp,
                                color = Color.Gray
                            )
                        }
                    }
                }
            }
            if (showMovingText && movingText.isNotEmpty()) {
                MovingTextCopy(movingText, true)
            }
        }
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

@Preview(showBackground = true)
@Composable
fun LyricsScreenPreview(){
    val previewMusic = Music(
        "ArtistName",
        "SongName",
        """
        [00:00.00] Первая строка текста
        [00:01.00] Вторая строка текста  
        [00:02.00] Третья строка текста
        """.trimIndent()
    )

    LyryxTheme(dynamicColor = false){
        LyricsScreen(
            rememberNavController(),
            previewMusic,
            null,
            isPreview = true
        )
    }

}