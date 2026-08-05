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
import com.semorka.lyryx.core.sound.PlayerViewModel
import com.semorka.lyryx.core.sound.PlaybackService

@OptIn(UnstableApi::class)
@Composable
fun LyryxApp(){
    AppNavigation()
}