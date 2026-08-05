package com.semorka.lyryx

import android.content.ComponentName
import androidx.annotation.OptIn
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.media3.common.util.UnstableApi
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import com.google.common.util.concurrent.MoreExecutors
import com.semorka.lyryx.core.music.MusicViewModel
import com.semorka.lyryx.core.navigation.AppNavigation
import com.semorka.lyryx.core.net.lrclib.LRCLibViewModel
import com.semorka.lyryx.core.net.word_lyrics.WordLyricsViewModel
import com.semorka.lyryx.core.sound.ExoPlayerViewModel
import com.semorka.lyryx.core.sound.PlaybackService

@OptIn(UnstableApi::class)
@Composable
fun LyryxApp(){
    val context = LocalContext.current
    val musicVm: MusicViewModel = viewModel()
    val lyricsVm: LRCLibViewModel = viewModel()
    val wordsVm: WordLyricsViewModel = viewModel()
    var playerVm by remember { mutableStateOf<ExoPlayerViewModel?>(null) }

    val sessionToken =
        remember { SessionToken(context, ComponentName(context, PlaybackService::class.java)) }
    val controllerFuture = remember { MediaController.Builder(context, sessionToken).buildAsync() }

    DisposableEffect(controllerFuture) {
        onDispose {
            MediaController.releaseFuture(controllerFuture)
        }
    }

    LaunchedEffect(Unit) {
        controllerFuture.addListener({
            try {
                val player = controllerFuture.get()
                playerVm = ExoPlayerViewModel(player)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }, MoreExecutors.directExecutor())
    }

    AppNavigation(musicVm, lyricsVm, playerVm, wordsVm)
}