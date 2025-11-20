package com.semorka.lyryx.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.semorka.lyryx.data.LyricsEntity
import com.semorka.lyryx.ui.theme.LyryxTheme

@Composable
fun LibraryScreen(navController: NavController, lyricsList: List<LyricsEntity>){
    Column(Modifier.fillMaxSize()){
        LazyColumn(){
            items(lyricsList) { song ->
                Row(Modifier.fillMaxSize().clickable{
                    navController.navigate("Lyrics/${song.artistName}/${song.songName}/${song.syncedText}")
                }.padding(16.dp)){
                    Column{
                        Text(song.songName, style = MaterialTheme.typography.labelLarge)
                        Text(song.artistName, style = MaterialTheme.typography.labelMedium)
                    }
                    Text(
                        getCleanLyricsText(song.syncedText),
                        modifier = Modifier
                            .weight(1f),
                        style = MaterialTheme.typography.bodySmall,
                        maxLines = 3,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}

@Preview
@Composable
private fun LibraryScreenPreview(){
    val lyricsList = listOf(LyricsEntity(artistName = "Puzzle", songName = "Loose Cannon", syncedText = "[00:00:00] Ponyal\n[00:00:01] Aga"))
    LyryxTheme {
        LibraryScreen(rememberNavController(), lyricsList)
    }
}

fun getCleanLyricsText(lyrics: String): String {
    return lyrics.lines()
        .map { it.substringAfter("]").trim() }
        .filter { it.isNotBlank() }
        .joinToString("\n")
}