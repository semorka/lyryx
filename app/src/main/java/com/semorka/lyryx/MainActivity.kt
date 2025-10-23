package com.semorka.lyryx

import android.app.Application
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.LocalViewModelStoreOwner
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.semorka.lyryx.data.BaseLyricsViewModel
import com.semorka.lyryx.data.LyricsViewModel
import com.semorka.lyryx.music.Music
import com.semorka.lyryx.music.MusicViewModel
import com.semorka.lyryx.screens.LoadTrackScreen
import com.semorka.lyryx.screens.LyricsScreen
import com.semorka.lyryx.screens.SearchScreen

class LyricsViewModelFactory(val application: Application):
    ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return LyricsViewModel(application) as T
    } }

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val owner = LocalViewModelStoreOwner.current

            owner?.let{
                val lyricsVm: LyricsViewModel = viewModel(
                    it,
                    "LyricsViewModel",
                    LyricsViewModelFactory(LocalContext.current.applicationContext as Application)
                )

                Scaffold { paddingValues ->
                    Box(Modifier.padding(paddingValues)) {
                        Main(lyricsVm)
                    }
                }
            }
        }
    }
}

@Composable
fun Main(lyricsVm: BaseLyricsViewModel){
    val navcontroller = rememberNavController()
    val viewModel: MusicViewModel = viewModel()

    NavHost(navcontroller, startDestination = "LoadTrack") {
        composable("LoadTrack") {
            LoadTrackScreen(navcontroller, viewModel)
        }
        composable("Search") {
            SearchScreen(navcontroller, viewModel, lyricsVm)
        }
        composable("Lyrics/{artistName}/{songName}/{lyrics}") { backStackEntry ->
            val artistName = backStackEntry.arguments?.getString("artistName") ?: ""
            val songName = backStackEntry.arguments?.getString("songName") ?: ""
            val lyrics = backStackEntry.arguments?.getString("lyrics") ?: ""

            val music = Music(
                name = songName,
                artistName = artistName,
                syncedLyrics = lyrics
            )

            LaunchedEffect(Unit) {
                lyricsVm
                lyricsVm.songName = songName
                lyricsVm.syncedText = lyrics
                lyricsVm.addLyrics()
            }

            LyricsScreen(navcontroller, music, viewModel.currentAudioUri)
        }
    }
}