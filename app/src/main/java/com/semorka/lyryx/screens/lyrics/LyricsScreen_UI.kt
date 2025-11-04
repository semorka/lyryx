package com.semorka.lyryx.screens.lyrics

import android.Manifest
import android.content.pm.PackageManager
import android.net.Uri
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.animateColorAsState
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
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
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
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberPermissionState
import com.semorka.lyryx.BeatDetector
import com.semorka.lyryx.MockPlayerViewModel
import com.semorka.lyryx.PlayerViewModel
import com.semorka.lyryx.music.Music
import com.semorka.lyryx.ui.theme.LyryxTheme
import kotlinx.coroutines.delay

@Composable
fun LyricsScreenRealContent(
    navController: NavController,
    music: Music,
    audioUri: Uri?,
    playerVm: PlayerViewModel
) {
    val context = LocalContext.current

    val currentTime = playerVm.currentTime
    val isPlaying = playerVm.isPlaying

    val beatDetector = remember { BeatDetector() }
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

    LaunchedEffect(currentLyric) {
        if (currentLyric.isNotEmpty() && currentLyric != "♪") {
            lineChanged = true
            delay(500)
            lineChanged = false
        }
    }

    LaunchedEffect(audioUri) {
        if (audioUri != null) {
            playerVm.initializePlayer(context, audioUri) {
                beatDetector.start(playerVm.mediaPlayer!!) { beatTime ->
                    lastOnsetTime = beatTime
                    onsetTimestamps.add(beatTime)
                }
            }
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            beatDetector.stop()
        }
    }

    LyricsScreenUI(
        music = music,
        currentLyric = currentLyric,
        isPlaying = isPlaying,
        formattedTime = String.format("%02d:%02d", currentTime / 60_000, (currentTime % 60_000) / 1_000),
        isOnsetActive = isOnsetActive,
        lineChanged = lineChanged,
        showMovingText = showMovingText,
        movingText = movingText,
        onPlayPause = { playerVm.playPause() }
    )
}

@Composable
fun LyricsScreenUI(
    music: Music,
    currentLyric: String,
    isPlaying: Boolean,
    formattedTime: String,
    isOnsetActive: Boolean,
    lineChanged: Boolean,
    showMovingText: Boolean,
    movingText: String,
    onPlayPause: () -> Unit,
    showPreviewInfo: Boolean = false
) {
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
        targetValue = if(lineChanged) Color.Black.copy(0.03f) else Color.Transparent,
        animationSpec = tween(400)
    )
    Box(Modifier.fillMaxSize().padding(16.dp)) {
        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(Modifier.weight(3f)) {
                Text(music.name, fontSize = 20.sp, style = MaterialTheme.typography.labelMedium)
                Text(music.artistName, fontSize = 16.sp, style = MaterialTheme.typography.labelMedium, color = Color.Gray)
            }
            Text(
                text = "Lyryx",
                textAlign = TextAlign.Center,
                modifier = Modifier.weight(3f),
                style = MaterialTheme.typography.titleLarge
            )
            Text(
                formattedTime,
                textAlign = TextAlign.End,
                style = MaterialTheme.typography.labelMedium,
                modifier = Modifier.weight(2f)
            )
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

                        if (showPreviewInfo) {
                            Spacer(Modifier.height(16.dp))
                            Text(
                                text = "Preivew mode",
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

        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(32.dp)
        ) {
            Text(
                text = if (isPlaying) "⏸" else "▶",
                modifier = Modifier
                    .clickable { onPlayPause() },
                fontSize = 36.sp
            )
        }
    }
}

@Composable
fun ExampleScreen() {
    val launcher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            // Permission Accepted: Do something
            Log.d("LYRYX","PERMISSION GRANTED")
        } else {
            Log.d("LYRYX","PERMISSION DENIED")
        }
    }
    val context = LocalContext.current
    Button(
        onClick = {
            // Check permission
            when (PackageManager.PERMISSION_GRANTED) {
                ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.RECORD_AUDIO
                ) -> {
                    // Some works that require permission
                    Log.d("ExampleScreen","Code requires permission")
                }
                else -> {
                    launcher.launch(Manifest.permission.RECORD_AUDIO)
                }
            }
        }
    ) {
        Text(text = "Check and Request Permission")
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun LyricsScreenUI_Preview() {
    val previewMusic = Music(
        "Snow Strippers",
        "Under Your Spell",
        "[00:00.00] Sample lyrics for preview"
    )

    LyryxTheme {
        LyricsScreenUI(
            music = previewMusic,
            currentLyric = "This is a sample lyric line that shows how text looks",
            isPlaying = true,
            formattedTime = "02:30",
            isOnsetActive = true,
            lineChanged = false,
            showMovingText = false,
            movingText = "",
            onPlayPause = { },
            showPreviewInfo = false
        )
    }
}