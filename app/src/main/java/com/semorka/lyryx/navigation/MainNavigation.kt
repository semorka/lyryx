package com.semorka.lyryx.navigation

import androidx.annotation.OptIn
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.media3.common.util.UnstableApi
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.semorka.lyryx.sound.v2.ExoPlayerViewModel
import com.semorka.lyryx.music.MusicViewModel
import com.semorka.lyryx.net.lrclib.LRCLibViewModel
import com.semorka.lyryx.net.word_lyrics.WordLyricsViewModel
import com.semorka.lyryx.screens.LibraryScreen
import com.semorka.lyryx.screens.LoadTrackScreen
import com.semorka.lyryx.screens.LyricsSearchScreen
import com.semorka.lyryx.screens.SearchScreen
import com.semorka.lyryx.screens.lyrics.LyricsScreen

@OptIn(UnstableApi::class)
@Composable
fun MainNavigation(
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
            LoadTrackScreen(navController, musicVm)
        }

        composable<Destination.Search> {
            SearchScreen(navController, musicVm, lyricsVm, wordsVm)
        }

        composable<Destination.Lyrics> {
            LyricsScreen(navController, musicVm.currentAudioUri, playerVm, musicVm = musicVm, wordsVm = wordsVm)
        }

        composable<Destination.Library> {
            val lyricsList by lyricsVm.lyricsState.collectAsState()

            LibraryScreen(navController, lyricsList = lyricsList)
        }

        composable<Destination.LyricsSearch> {
            LyricsSearchScreen(navController, lyricsVm, musicVm, wordsVm)
        }
    }
}