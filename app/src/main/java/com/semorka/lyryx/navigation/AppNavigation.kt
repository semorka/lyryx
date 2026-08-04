package com.semorka.lyryx.navigation

import androidx.annotation.OptIn
import androidx.compose.runtime.Composable
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
    navController: NavHostController,
    musicVm: MusicViewModel,
    lyricsVm: LRCLibViewModel,
    playerVm: ExoPlayerViewModel?,
    wordsVm: WordLyricsViewModel
) {
    NavHost(
        navController = navController,
        startDestination = Destination.LoadTrack
    ) {
        composable<Destination.LoadTrack> {
            LoadTrackScreen(
                viewModel = musicVm,
                onFileSelected = { navController.navigate(Destination.Search) }
            )
        }

        composable<Destination.Search> {
            SearchScreen(
                musicVm,
                lyricsVm,
                wordsVm
            ) {
                navController.navigate(
                    Destination.LyricsSearch
                )
            }
        }

        composable<Destination.LyricsSearch> {
            LyricsSearchScreen(navController, lyricsVm, musicVm, wordsVm)
        }

        composable<Destination.Lyrics> {
            LyricsScreen(musicVm.currentAudioUri, playerVm, musicVm = musicVm, wordsVm = wordsVm)
        }

    }
}