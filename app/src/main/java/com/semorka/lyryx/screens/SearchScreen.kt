package com.semorka.lyryx.screens

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.semorka.lyryx.data.BaseLyricsViewModel
import com.semorka.lyryx.data.LyricsEntity
import com.semorka.lyryx.data.MockLyricsViewModel
import com.semorka.lyryx.music.MockMusicViewModel
import com.semorka.lyryx.music.MusicViewModel
import com.semorka.lyryx.musicApi
import com.semorka.lyryx.ui.theme.LyryxTheme
import kotlinx.coroutines.launch

@Composable
fun SearchScreen(navController: NavController = rememberNavController(), musicVm: MusicViewModel, lyricVm: BaseLyricsViewModel) {
    val scope = rememberCoroutineScope()
    var songNameSearch by remember { mutableStateOf("") }
    var artistNameSearch by remember { mutableStateOf("") }
    
    val fileName = musicVm.currentAudioUri?.let { getFileName(LocalContext.current, it) } ?: "Unknown"

    Column(Modifier.padding(16.dp).fillMaxSize(), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)){
            if (musicVm.currentAudioUri != null) {
                Text("Название файла: $fileName")
                Spacer(Modifier.height(8.dp))
            }

            OutlinedTextField(
                value = songNameSearch,
                onValueChange = { songNameSearch = it },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Название") }
            )
            OutlinedTextField(
                value = artistNameSearch,
                onValueChange = { artistNameSearch = it },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Исполнитель") }
            )

            Button(
                onClick = {
                    musicVm.searchText = "Ищем..."

                    scope.launch {
                        lyricVm.searchLyrics(artistNameSearch, songNameSearch)
                        if(lyricVm.searchResult != emptyList<LyricsEntity>()){
                            musicVm.searchText = "Найдено в библиотеке. Загружаем..."
                            val result = lyricVm.searchResult[0]
                            val artistName = result.artistName
                            val songName = result.songName
                            val syncedText = result.syncedText
                            navController.navigate("Lyrics/${artistName}/${songName}/${syncedText}")
                        }
                        else {
                            try {
                                val result = musicApi.searchMusic("$artistNameSearch $songNameSearch")
                                if (result.isNotEmpty()) {
                                    musicVm.songs = result
                                    musicVm.searchText = "Найдено ${musicVm.songs.size} текстов"
                                }
                                else {
                                    musicVm.searchText = "Ничего не найдено. Попробуйте указать точнее название и исполнителя"
                                }
                            } catch (e: Exception) {
                                musicVm.searchText = "Ошибка: ${e.message}"
                            }
                        }
                    }
                },
                shape = RoundedCornerShape(25),
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(MaterialTheme.colorScheme.primary)
            ) {
                Text("Найти", style = MaterialTheme.typography.labelMedium)
            }
        }

        Text(musicVm.searchText)

        LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            items(musicVm.songs) { song ->
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

fun getFileName(context: Context, uri: Uri): String {
    return try {
        context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
            cursor.moveToFirst()
            cursor.getString(cursor.getColumnIndexOrThrow(OpenableColumns.DISPLAY_NAME))
        } ?: "Unknown"
    } catch (e: Exception) {
        "Unknown"
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun PreviewSearchScreen(){
    val musicVm = MockMusicViewModel.createMusicViewModel()
    val lyricsVm = MockLyricsViewModel("", "", "")
    LyryxTheme{
        Surface(Modifier.systemBarsPadding().fillMaxSize()){
            SearchScreen(rememberNavController(), musicVm, lyricsVm)
        }
    }
}