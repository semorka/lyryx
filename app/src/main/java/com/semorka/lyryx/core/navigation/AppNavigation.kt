package com.semorka.lyryx.core.navigation

import androidx.annotation.OptIn
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.media3.common.util.UnstableApi
import com.semorka.lyryx.core.sound.ExoPlayerViewModel
import com.semorka.lyryx.core.music.MusicViewModel
import com.semorka.lyryx.core.net.lrclib.LRCLibViewModel
import com.semorka.lyryx.core.net.word_lyrics.WordLyricsViewModel

@OptIn(UnstableApi::class)
@Composable
fun AppNavigation(
    musicVm: MusicViewModel,
    lyricsVm: LRCLibViewModel,
    playerVm: ExoPlayerViewModel?,
    wordsVm: WordLyricsViewModel
) {
    val navViewModel: NavigationViewModel = viewModel()
    val backStack by navViewModel.backStack.collectAsStateWithLifecycle()
    AppNavigationV2(
        navigationViewModel = navViewModel,
        backStack = backStack,
        onBack = { navViewModel.onBack() },
        musicVm = musicVm,
        lyricsVm = lyricsVm,
        playerVm = playerVm,
        wordsVm = wordsVm
    )
}