package com.semorka.lyryx

import android.app.Application
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
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
import com.semorka.lyryx.screens.lyrics.LyricsScreen
import com.semorka.lyryx.screens.SearchScreen
import com.semorka.lyryx.screens.lyrics.ExampleScreen
import com.semorka.lyryx.ui.theme.LyryxTheme
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.functions.Functions
import io.github.jan.supabase.functions.functions
import io.github.jan.supabase.postgrest.Postgrest
import io.ktor.client.statement.bodyAsText
import kotlinx.serialization.Serializable

val supabase = createSupabaseClient(
    supabaseUrl = "https://wicmfrsnwoeoomtapwog.supabase.co",
    supabaseKey = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6IndpY21mcnNud29lb29tdGFwd29nIiwicm9sZSI6ImFub24iLCJpYXQiOjE3NjIxMTc5MzIsImV4cCI6MjA3NzY5MzkzMn0.X-U0EytanHHJD-PoFnEIWL7S2dGe3OcYAzV2HTPqvEg"
) {
    install(Auth)
    install(Postgrest)
    install(Functions)
}

val playerVm = PlayerViewModel()

class LyricsViewModelFactory(
    private val application: Application
): ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(LyricsViewModel::class.java)) {
            return LyricsViewModel(application) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    } }

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            LyryxTheme {
                val owner = LocalViewModelStoreOwner.current

                owner?.let{
                    val lyricsVm: LyricsViewModel = viewModel(
                        it,
                        "LyricsViewModel",
                        LyricsViewModelFactory(LocalContext.current.applicationContext as Application)
                    )

                    Scaffold { paddingValues ->
                        Surface(Modifier.padding(paddingValues), color = MaterialTheme.colorScheme.background) {
                            Main(lyricsVm, playerVm)
                        }
                    }
                } ?: run {
                    throw IllegalArgumentException("Owner is null")
                }
            }
        }
    }
}

@Composable
fun Main(lyricsVm: BaseLyricsViewModel, playerVm: PlayerViewModel){
    val navcontroller = rememberNavController()
    val musicVm: MusicViewModel = viewModel()

    LaunchedEffect(Unit) {
        val response = supabase.functions.invoke("genius-proxy").bodyAsText()
        Log.d("SUPABASE", "Genius key: $response")
    }

    NavHost(navcontroller, startDestination = "LoadTrack") {
        composable("LoadTrack") {
            LoadTrackScreen(navcontroller, musicVm)
        }
        composable("Search") {
            SearchScreen(navcontroller, musicVm, lyricsVm)
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

            LaunchedEffect(artistName, songName, lyrics) {
                if (artistName.isNotEmpty() && songName.isNotEmpty()) {
                    lyricsVm.songName = songName
                    lyricsVm.artistName = artistName
                    lyricsVm.syncedText = lyrics
                    lyricsVm.addLyrics()
                }
            }

            LyricsScreen(navcontroller, music, musicVm.currentAudioUri, playerVm)
        }
    }
}