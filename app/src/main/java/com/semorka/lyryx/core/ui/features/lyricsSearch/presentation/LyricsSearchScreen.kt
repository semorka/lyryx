package com.semorka.lyryx.core.ui.features.lyricsSearch.presentation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.semorka.lyryx.core.music.MusicViewModel
import com.semorka.lyryx.navigation.Destination
import com.semorka.lyryx.net.lrclib.LRCLibViewModel
import com.semorka.lyryx.net.word_lyrics.WordLyricsViewModel

@Composable
fun LyricsSearchScreen(navController: NavController, lyricsVm: LRCLibViewModel, musicVm: MusicViewModel, wordLyricsViewModel: WordLyricsViewModel){
    val tracks by lyricsVm.lyricsState.collectAsStateWithLifecycle()
    val isLoading by lyricsVm.isLoading.collectAsStateWithLifecycle()
    val isLoading2 by wordLyricsViewModel.isLoading.collectAsStateWithLifecycle()
    val segments by wordLyricsViewModel.lyricsSegments.collectAsStateWithLifecycle()

    val isWordSynced by wordLyricsViewModel.isWordSynced.collectAsStateWithLifecycle()

    if(isLoading2) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
    }
    else if(isWordSynced) {
        navController.navigate(Destination.Lyrics)

        // TODO
//        LazyColumn(Modifier.fillMaxSize(), verticalArrangement = Arrangement.spacedBy(8.dp)){
//            items(tracks.sortedByDescending { it.hasSynced }) { track ->
//                Card(Modifier.fillMaxWidth().heightIn(min = 80.dp, max = 200.dp)) {
//                    Row(Modifier.clickable(onClick = {
//                        if (track.isActuallySynced) {
//                            musicVm.track?.let { currentTrack ->
//                                musicVm.track = currentTrack.copy(syncedSegments = track.parsedSegments)
//                                navController.navigate(Destination.Lyrics)
//                            }
//                        }
//                    })){
//                        Column(Modifier.weight(0.3f)) {
//                            Text(track.artistName)
//                            Text(track.trackName)
//                            if (track.hasSynced) {
//                                Text("SYNCED LYRICS")
//                            }
//                        }
//                        if (track.isActuallySynced) {
//                            AutoScrollingText(
//                                segments = track.parsedSegments,
//                                modifier = Modifier.weight(0.6f).fillMaxWidth().height(120.dp),
//                            )
//                        }
//                    }
//                }
//            }
//        }
    } else {
        Column {
            Text("Word-by-Word Unavailable")
            Text("This song lacks word timing")
            Text("Plain mode is under development")
        }
    }

}

//@Composable
//fun AutoScrollingText(
//    segments: List<LyricSegment>,
//    modifier: Modifier = Modifier
//) {
//    val lazyListState = rememberLazyListState()
//    var currentSegmentIndex by remember { mutableIntStateOf(0) }
//
//    LaunchedEffect(segments) {
//        while (true) {
//            delay(500)
//
//            if (currentSegmentIndex < segments.size - 1) {
//                currentSegmentIndex++
//                lazyListState.animateScrollToItem(currentSegmentIndex)
//            } else {
//                currentSegmentIndex = 0
//                lazyListState.animateScrollToItem(0)
//            }
//        }
//    }
//
//    LazyColumn(
//        modifier = modifier,
//        state = lazyListState,
//        verticalArrangement = Arrangement.spacedBy(1.dp)
//    ) {
//        items(segments) { segment ->
//            Text(
//                text = segment.text,
//                modifier = Modifier.fillMaxWidth(),
//                fontSize = 14.sp
//            )
//        }
//    }
//}