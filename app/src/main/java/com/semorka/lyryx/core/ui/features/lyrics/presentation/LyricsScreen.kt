package com.semorka.lyryx.core.ui.features.lyrics.presentation

import android.net.Uri
import androidx.annotation.OptIn
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.media3.common.util.UnstableApi
import com.semorka.lyryx.core.sound.ExoPlayerViewModel
import com.semorka.lyryx.core.music.MusicViewModel
import com.semorka.lyryx.net.word_lyrics.WordLyricsViewModel

@OptIn(UnstableApi::class)
@Composable
fun LyricsScreen(
    audioUri: Uri?,
    playerVm: ExoPlayerViewModel?,
    musicVm: MusicViewModel,
    wordsVm: WordLyricsViewModel
) {
    val segments by wordsVm.lyricsSegments.collectAsStateWithLifecycle()

    LyricsScreenContent(
        audioUri,
        playerVm,
        musicVm,
        segments = segments
    )
}