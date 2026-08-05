package com.semorka.lyryx.core.navigation

import androidx.compose.runtime.Composable
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.ui.NavDisplay
import com.semorka.lyryx.core.music.MusicViewModel
import com.semorka.lyryx.core.sound.ExoPlayerViewModel
import com.semorka.lyryx.core.ui.features.loadtrack.presentation.LoadTrackScreen
import com.semorka.lyryx.core.ui.features.lyrics.presentation.LyricsScreen
import com.semorka.lyryx.core.ui.features.lyricsSearch.presentation.LyricsSearchScreen
import com.semorka.lyryx.core.ui.features.search.presentation.SearchScreen
import com.semorka.lyryx.core.net.lrclib.LRCLibViewModel
import com.semorka.lyryx.core.net.word_lyrics.WordLyricsViewModel

@Composable fun AppNavigationV2(
    backStack: List<Destination>,
    onBack: () -> Unit,
    navigationViewModel: NavigationViewModel,
    musicVm: MusicViewModel,
    lyricsVm: LRCLibViewModel,
    playerVm: ExoPlayerViewModel?,
    wordsVm: WordLyricsViewModel
){
    NavDisplay(
        backStack = backStack,
        onBack = onBack,
        entryProvider = { key ->
            when (key) {
                Destination.LoadTrack -> NavEntry(key) {
                    LoadTrackScreen(musicVm) { navigationViewModel.navigateTo(Destination.SearchFlow.TrackSearch)}
                }
                Destination.Lyrics-> NavEntry(key) {
                    LyricsScreen(
                        audioUri = musicVm.currentAudioUri,
                        playerVm = playerVm,
                        musicVm = musicVm,
                        wordsVm = wordsVm,
                    )
                }
                Destination.SearchFlow.TrackSearch -> NavEntry(key) {
                    SearchScreen(
                        musicVm = musicVm,
                        lyricsVm = lyricsVm,
                        wordsVm = wordsVm,
                        onTrackSelected = { navigationViewModel.navigateTo(Destination.SearchFlow.LyricsSearch)}
                    )
                }
                Destination.SearchFlow.LyricsSearch -> NavEntry(key) {
                    LyricsSearchScreen(
                        lyricsVm = lyricsVm,
                        musicVm = musicVm,
                        wordLyricsViewModel = wordsVm,
                        onLyricsSelected = { navigationViewModel.finishSearchAndOpenPlayer() }
                    )
                }
            }
        }
    )
}