package com.semorka.lyryx.core.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.tooling.preview.Preview
import coil.compose.AsyncImage
import com.semorka.lyryx.R

@Composable
fun ImageUrl(
    imageUrl: String?,
    modifier: Modifier = Modifier,
    onSuccess: () -> Unit = {}
) {
    AsyncImage(
        model = imageUrl,
        contentDescription = null,
        modifier = modifier,
        onSuccess = { onSuccess() },
        contentScale = ContentScale.FillWidth
    )
}

@Preview(showBackground = true)
@Composable
fun ImageUrlPreview(modifier: Modifier = Modifier) {
    AsyncImage(
        model = R.drawable.cover,
        contentDescription = null,
        modifier = modifier,
        contentScale = ContentScale.FillWidth
    )
}