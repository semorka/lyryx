package com.semorka.lyryx.core.ui.features.lyrics

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import com.semorka.lyryx.R

@Composable fun PlayPauseButton(
    isPlaying: Boolean,
    onPlayPause: () -> Unit,
    modifier: Modifier = Modifier
) {
    val icon = if (isPlaying) R.drawable.ic_pause else R.drawable.ic_play
    IconButton(
        onClick = onPlayPause,
        modifier = modifier
    ) {
        Icon(
            painterResource(icon),
            contentDescription = null,
            modifier = Modifier.fillMaxSize()
        )
    }
}