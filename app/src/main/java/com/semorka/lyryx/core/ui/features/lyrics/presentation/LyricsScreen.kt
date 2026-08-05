package com.semorka.lyryx.core.ui.features.lyrics.presentation

import android.app.Activity
import android.net.Uri
import android.view.WindowManager
import androidx.annotation.OptIn
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.ui.platform.LocalView
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.media3.common.util.UnstableApi
import com.semorka.lyryx.core.sound.ExoPlayerViewModel
import com.semorka.lyryx.core.music.MusicViewModel
import com.semorka.lyryx.core.ui.components.AlbumCoverBackground
import com.semorka.lyryx.net.word_lyrics.LyricSegment
import com.semorka.lyryx.net.word_lyrics.WordLyricsViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import java.util.Locale
import kotlin.time.Duration.Companion.milliseconds

@OptIn(UnstableApi::class)
@Composable
fun LyricsScreen(
    audioUri: Uri?,
    playerVm: ExoPlayerViewModel?,
    musicVm: MusicViewModel,
    wordsVm: WordLyricsViewModel
) {
    val segments by wordsVm.lyricsSegments.collectAsStateWithLifecycle()

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

    LyricsScreenContent(
        isPlaying = isPlaying,
        formattedTime = String.format(Locale.ROOT, "%02d:%02d", smoothTime / 60_000, (smoothTime % 60_000) / 1_000),
        onPlayPause = {
            playerVm.togglePlay()
        },
        trackName = musicVm.track!!.trackName,
        artistName = musicVm.track!!.artistName,
        currentSegment = currentSegment,
        currentTime = smoothTime,
        cover = musicVm.track?.cover
    )
}