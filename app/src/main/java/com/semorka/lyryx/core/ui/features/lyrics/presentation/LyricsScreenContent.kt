package com.semorka.lyryx.core.ui.features.lyrics.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.semorka.lyryx.R
import com.semorka.lyryx.core.ui.components.AlbumCoverBackground
import com.semorka.lyryx.core.net.word_lyrics.LyricSegment
import com.semorka.lyryx.core.net.word_lyrics.WordSegment
import com.semorka.lyryx.core.ui.components.CenteredLyricWordView
import com.semorka.lyryx.core.ui.features.lyrics.PlayPauseButton
import com.semorka.lyryx.core.ui.theme.LyryxTheme

@Composable
fun LyricsScreenContent(
    isPlaying: Boolean,
    formattedTime: String,
    onPlayPause: () -> Unit,
    trackName: String,
    artistName: String,
    currentSegment: LyricSegment?,
    currentTime: Long,
    cover: String?
) {

    AlbumCoverBackground(cover)

    Box(Modifier.fillMaxSize().padding(16.dp)) {
        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(Modifier.weight(3f)) {
                Text(trackName, fontSize = 20.sp, style = MaterialTheme.typography.labelMedium)
                Text(artistName, fontSize = 16.sp, style = MaterialTheme.typography.labelMedium, color = Color.Gray)
            }
            Text(
                text = stringResource(R.string.app_name),
                textAlign = TextAlign.Center,
                modifier = Modifier.weight(3f),
                style = MaterialTheme.typography.titleLarge
            )
            Text(
                formattedTime,
                textAlign = TextAlign.End,
                style = MaterialTheme.typography.labelMedium,
                modifier = Modifier.weight(2f)
            )
        }

        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Box(
                modifier = Modifier
            ) {
                Box(contentAlignment = Alignment.Center){
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = 16.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            currentSegment?.let { segment ->
                                CenteredLyricWordView(
                                    segment = segment,
                                    currentTime = currentTime,
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                        }
                    }
                }
            }
        }

        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(32.dp)
        ) {
            PlayPauseButton(
                isPlaying = isPlaying,
                onPlayPause = onPlayPause,
                modifier = Modifier.size(60.dp)
            )
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF121212)
@Composable
private fun LyricsScreenPreview() {
    LyryxTheme {
        val sampleWords = listOf(
            WordSegment(0L, "Однажды"),
            WordSegment(400L, "вечером"),
            WordSegment(800L, "мы"),
            WordSegment(1200L, "найдём"),
            WordSegment(1600L, "прекрасный"),
            WordSegment(2200L, "пляж")
        )

        val sampleSegment = LyricSegment(
            lineTimeMillis = 0L,
            text = "Однажды вечером мы найдём прекрасный пляж",
            words = sampleWords
        )

        LyricsScreenContent(
            isPlaying = true,
            formattedTime = "00:23",
            onPlayPause = {},
            trackName = "Born to be",
            artistName = "Semorka",
            currentSegment = sampleSegment,
            currentTime = 1200L,
            null
        )
    }
}