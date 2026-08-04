package com.semorka.lyryx.screens

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.NavController
import com.semorka.lyryx.net.lrclib.LRCLibTrack

@Composable
fun LibraryScreen(navController: NavController, lyricsList: List<LRCLibTrack>){
//    Column(Modifier.fillMaxSize()){
//        LazyColumn(){
//            items(lyricsList) { song ->
//                Row(Modifier.fillMaxSize().clickable{
//                    navController.navigate(
//                        Destination.Lyrics.createRoute(song.artistName, song.songName, song.syncedText)
//                    )
//                }.padding(16.dp)){
//                    Column{
//                        Text(song.songName, style = MaterialTheme.typography.labelLarge)
//                        Text(song.artistName, style = MaterialTheme.typography.labelMedium)
//                    }
//                    Text(
//                        getCleanLyricsText(song.syncedText),
//                        modifier = Modifier
//                            .weight(1f),
//                        style = MaterialTheme.typography.bodySmall,
//                        maxLines = 3,
//                        overflow = TextOverflow.Ellipsis
//                    )
//                }
//            }
//        }
//    }
}