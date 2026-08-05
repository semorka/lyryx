package com.semorka.lyryx.core.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.ui.NavDisplay
import com.semorka.lyryx.core.music.MusicViewModel
import com.semorka.lyryx.core.sound.PlayerViewModel
import com.semorka.lyryx.core.ui.features.loadtrack.presentation.LoadTrackScreen
import com.semorka.lyryx.core.ui.features.lyrics.presentation.LyricsScreen
import com.semorka.lyryx.core.ui.features.lyricsSearch.presentation.LyricsSearchScreen
import com.semorka.lyryx.core.ui.features.search.presentation.SearchScreen
import com.semorka.lyryx.core.net.lrclib.LRCLibViewModel
import com.semorka.lyryx.core.net.word_lyrics.WordLyricsViewModel
import androidx.hilt.navigation.compose.hiltViewModel

@Composable fun AppNavigation(
    navViewModel: NavigationViewModel = hiltViewModel(),
){
    val backStack by navViewModel.backStack.collectAsStateWithLifecycle()

    NavDisplay(
        backStack = backStack,
        onBack = { navViewModel.onBack() },
        entryProvider = { key ->
            when (key) {
                Destination.LoadTrack -> NavEntry(key) {
                    LoadTrackScreen { navViewModel.navigateTo(Destination.SearchFlow.TrackSearch)}
                }
                Destination.Lyrics-> NavEntry(key) {
                    val musicVm: MusicViewModel = hiltViewModel()

                    LyricsScreen(
                        audioUri = musicVm.currentAudioUri,
                        musicVm = musicVm
                    )
                }
                Destination.SearchFlow.TrackSearch -> NavEntry(key) {
                    val musicVm: MusicViewModel = hiltViewModel()
                    val lyricsVm: LRCLibViewModel = hiltViewModel()
                    val wordsVm: WordLyricsViewModel = hiltViewModel()

                    SearchScreen(
                        musicVm = musicVm,
                        lyricsVm = lyricsVm,
                        wordsVm = wordsVm,
                        onTrackSelected = { navViewModel.navigateTo(Destination.SearchFlow.LyricsSearch)}
                    )
                }
                Destination.SearchFlow.LyricsSearch -> NavEntry(key) {
                    val lyricsVm: LRCLibViewModel = hiltViewModel()
                    val musicVm: MusicViewModel = hiltViewModel()
                    val wordsVm: WordLyricsViewModel = hiltViewModel()

                    LyricsSearchScreen(
                        lyricsVm = lyricsVm,
                        musicVm = musicVm,
                        wordLyricsViewModel = wordsVm,
                        onLyricsSelected = { navViewModel.finishSearchAndOpenPlayer() }
                    )
                }
            }
        }
    )
}