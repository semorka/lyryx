package com.semorka.lyryx

import android.content.Context
import android.media.AudioFormat
import android.media.MediaPlayer
import android.media.audiofx.Visualizer
import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.animateIntAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.unit.times
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.semorka.lyryx.music.MockMusicViewModel
import com.semorka.lyryx.music.Music
import com.semorka.lyryx.music.MusicViewModel
import com.semorka.lyryx.screens.LyricsScreen
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            Scaffold { paddingValues ->
                Box(Modifier.padding(paddingValues)) {
                    Main()
                }
            }
        }
    }
}

@Composable
fun Main(){
    val navcontroller = rememberNavController()
    val viewModel: MusicViewModel = viewModel()

    NavHost(navcontroller, startDestination = "LoadTrack") {
        composable("LoadTrack") {
            LoadTrackScreen(navcontroller, viewModel)
        }
        composable("Search") {
            SearchScreen(navcontroller, viewModel)
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

            LyricsScreen(navcontroller, music, viewModel.currentAudioUri)
        }
    }
}

@Composable
fun LoadTrackScreen(
    navController: NavController,
    viewModel: MusicViewModel
){
    val audioPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent(),
        onResult = { uri ->
            uri?.let {
                viewModel.currentAudioUri = it
                navController.navigate("Search")
            }
        }
    )

    Column {
        Button(
            onClick = {
                audioPickerLauncher.launch("audio/*")
            }
        ) {
            Text("Выбрать аудиофайл")
        }
    }
}

@Composable
fun SearchScreen(navController: NavController = rememberNavController(), viewModel: MusicViewModel) {
    val scope = rememberCoroutineScope()
    var search by remember { mutableStateOf("") }

    Column(Modifier.padding(16.dp)) {
        if (viewModel.currentAudioUri != null) {
            Text("Аудиофайл выбран: ${viewModel.currentAudioUri!!.lastPathSegment ?: "файл"}")
            Spacer(Modifier.height(8.dp))
        }

        OutlinedTextField(search, onValueChange = { search = it })

        Button(
            onClick = {
                viewModel.searchText = "Ищем..."
                scope.launch {
                    try {
                        val result = musicApi.searchMusic(search)
                        if (result.isNotEmpty()) {
                            viewModel.songs = result
                            viewModel.searchText = "Найдено ${viewModel.songs.size} текстов"
                        }
                        else {
                            viewModel.searchText = "Ничего не найдено. Попробуйте указать точнее название и исполнителя"
                        }
                    } catch (e: Exception) {
                        viewModel.searchText = "Ошибка: ${e.message}"
                    }
                }
            }
        ) {
            Text("Найти")
        }

        Spacer(Modifier.height(16.dp))
        Text(viewModel.searchText)
        Spacer(Modifier.height(8.dp))
        LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            items(viewModel.songs) { song ->
                if (song.syncedLyrics != null && song.syncedLyrics.isNotBlank()) {
                    Row {
                        Column(
                            modifier = Modifier
                                .border(
                                    width = 2.dp,
                                    color = Color.Black,
                                    shape = RoundedCornerShape(10.dp)
                                )
                                .padding(6.dp)
                                .clickable(onClick = {
                                    navController.navigate("Lyrics/${song.artistName}/${song.name}/${song.syncedLyrics}")
                                })
                        )
                        {
                            Text(song.artistName, fontSize = 10.sp, fontStyle = FontStyle.Italic)
                            Text(song.name)
                        }
                        Text(
                            text = song.syncedLyrics.replace(Regex("\\[\\d+:\\d+\\.\\d+\\]"), "")
                                .trim().substringBefore("\n"),
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
        }
    }
}

@Preview
@Composable
fun SearchScreenPreview() {
    val viewModel = MockMusicViewModel.createMusicViewModel()
    SearchScreen(rememberNavController(), viewModel)
}