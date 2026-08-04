package com.semorka.lyryx.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.skydoves.cloudy.cloudy

@Composable
fun AlbumCoverBackground(
    cover: String?
) {
    var isLoaded by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier.fillMaxSize().cloudy(50),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier.fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            ImageUrl(
                imageUrl = cover,
                modifier = Modifier
                    .fillMaxWidth()
                    .then(if (isLoaded) Modifier.cloudy(5) else Modifier),
                onSuccess = { isLoaded = true }
            )

            if (isLoaded) {
                Box(
                    modifier = Modifier
                        .matchParentSize()
                        .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.10f))
                )
            }
        }
    }
}