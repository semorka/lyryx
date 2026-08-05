package com.semorka.lyryx.navigation

import androidx.annotation.OptIn
import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.media3.common.util.UnstableApi
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.semorka.lyryx.core.sound.ExoPlayerViewModel
import com.semorka.lyryx.core.music.MusicViewModel
import com.semorka.lyryx.net.lrclib.LRCLibViewModel
import com.semorka.lyryx.net.word_lyrics.WordLyricsViewModel
import com.semorka.lyryx.core.ui.features.loadtrack.presentation.LoadTrackScreen
import com.semorka.lyryx.core.ui.features.lyricsSearch.presentation.LyricsSearchScreen
import com.semorka.lyryx.core.ui.features.search.presentation.SearchScreen
import com.semorka.lyryx.core.ui.features.lyrics.presentation.LyricsScreen

@OptIn(UnstableApi::class)
@Composable
fun AppNavigation(
    musicVm: MusicViewModel,
    lyricsVm: LRCLibViewModel,
    playerVm: ExoPlayerViewModel?,
    wordsVm: WordLyricsViewModel
) {
    val navViewModel: NavigationViewModel = viewModel()
    val backStack = navViewModel.backStack
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