package com.semorka.lyryx.core.ui.features.lyrics.presentation

import android.app.Activity
import android.net.Uri
import android.view.WindowManager
import androidx.annotation.OptIn
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.media3.common.util.UnstableApi
import com.semorka.lyryx.core.sound.ExoPlayerViewModel
import com.semorka.lyryx.core.music.MusicViewModel
import com.semorka.lyryx.net.word_lyrics.LyricSegment
import com.semorka.lyryx.net.word_lyrics.WordSegment
import com.semorka.lyryx.core.ui.components.AlbumCoverBackground
import com.semorka.lyryx.core.ui.components.CenteredLyricWordView
import com.semorka.lyryx.core.ui.theme.LyryxTheme
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import java.util.Locale
import kotlin.time.Duration.Companion.milliseconds

@OptIn(UnstableApi::class)
@Composable
fun LyricsScreenContent(
    audioUri: Uri?,
    playerVm: ExoPlayerViewModel?,
    musicVm: MusicViewModel,
    segments: List<LyricSegment>
) {
    val view = LocalView.current

    DisposableEffect(Unit) {
        val window = (view.context as? Activity)?.window
        window?.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        onDispose {
            window?.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        }
    }

    if (playerVm == null) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        return
    }

    LaunchedEffect(audioUri) {
        if (audioUri != null) {
            playerVm.setMedia(audioUri)
        }
    }

    val isPlaying by playerVm.isPlaying.collectAsState()

    var smoothTime by remember { mutableLongStateOf(playerVm.currentPosition) }

    LaunchedEffect(isPlaying) {
        while (isActive) {
            smoothTime = playerVm.currentPosition
            if (isPlaying) {
                delay(16.milliseconds)
            } else {
                delay(100.milliseconds)
            }
        }
    }

    val currentSegment: LyricSegment? by remember(smoothTime, segments) {
        derivedStateOf {
            segments
                .takeWhile { it.lineTimeMillis <= smoothTime }
                .lastOrNull()
        }
    }
    Box(modifier = Modifier.fillMaxSize()) {
        AlbumCoverBackground(musicVm.track!!.cover)

        LyricsScreenUI(
            isPlaying = isPlaying,
            formattedTime = String.format(Locale.ROOT, "%02d:%02d", smoothTime / 60_000, (smoothTime % 60_000) / 1_000),
            onPlayPause = {
                playerVm.togglePlay()
            },
            musicName = musicVm.track!!.trackName,
            artistName = musicVm.track!!.artistName,
            currentSegment = currentSegment,
            currentTime = smoothTime,
        )
    }
}

@Composable
fun LyricsScreenUI(
    isPlaying: Boolean,
    formattedTime: String,
    onPlayPause: () -> Unit,
    musicName: String,
    artistName: String,
    currentSegment: LyricSegment?,
    currentTime: Long
) {

    Box(Modifier.fillMaxSize().padding(16.dp)) {
        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(Modifier.weight(3f)) {
                Text(musicName, fontSize = 20.sp, style = MaterialTheme.typography.labelMedium)
                Text(artistName, fontSize = 16.sp, style = MaterialTheme.typography.labelMedium, color = Color.Gray)
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
            ) {
                Box(contentAlignment = Alignment.Center){
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = 16.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            currentSegment?.let { segment ->
                                CenteredLyricWordView(
                                    segment = segment,
                                    currentTime = currentTime,
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                        }
                    }
                }
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

@Preview(showBackground = true, backgroundColor = 0xFF121212)
@Composable
private fun LyricsScreenPreview() {
    LyryxTheme {
        val sampleWords = listOf(
            WordSegment(0L, "Однажды"),
            WordSegment(400L, "вечером"),
            WordSegment(800L, "мы"),
            WordSegment(1200L, "найдём"),
            WordSegment(1600L, "прекрасный"),
            WordSegment(2200L, "пляж")
        )

        val sampleSegment = LyricSegment(
            lineTimeMillis = 0L,
            text = "Однажды вечером мы найдём прекрасный пляж",
            words = sampleWords
        )

        LyricsScreenUI(
            isPlaying = true,
            formattedTime = "00:23",
            onPlayPause = {},
            musicName = "Born to be",
            artistName = "Semorka",
            currentSegment = sampleSegment,
            currentTime = 1200L
        )
    }
}