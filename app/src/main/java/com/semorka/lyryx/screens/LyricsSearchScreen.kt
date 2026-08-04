package com.semorka.lyryx.screens

import android.util.Log
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.semorka.lyryx.music.MusicViewModel
import com.semorka.lyryx.navigation.Destination
import com.semorka.lyryx.net.lrclib.LRCLibViewModel
import com.semorka.lyryx.net.lrclib.LyricSegment
import com.semorka.lyryx.net.word_lyrics.WordLyricsViewModel
import kotlinx.coroutines.delay

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