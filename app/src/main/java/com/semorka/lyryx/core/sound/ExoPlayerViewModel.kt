package com.semorka.lyryx.core.sound

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.Player
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlin.time.Duration.Companion.milliseconds

class ExoPlayerViewModel(val playerInstance: Player): ViewModel() {

    private val _isPlaying = MutableStateFlow(playerInstance.isPlaying)
    val isPlaying = _isPlaying.asStateFlow()
    private val _position = MutableStateFlow(0L)
    val position = _position.asStateFlow()

    val currentPosition: Long
        get() = playerInstance.currentPosition

    fun togglePlay() {
        if (playerInstance.isPlaying) playerInstance.pause() else playerInstance.play()
    }

    fun setMedia(uri: Uri){
        val currentUri = playerInstance.currentMediaItem?.localConfiguration?.uri
        if (currentUri == uri) return

        playerInstance.setMediaItem(
            MediaItem.fromUri(uri)
        )
        playerInstance.prepare()
    }

    fun updateCurrentMediaMetadata(
        coverUri: Uri?,
        title: String? = null,
        artist: String? = null
    ) {
        val currentMediaItem = playerInstance.currentMediaItem ?: return
        val currentIndex = playerInstance.currentMediaItemIndex

        val updatedMetadata = currentMediaItem.mediaMetadata
            .buildUpon()
            .apply {
                coverUri?.let { setArtworkUri(it) }
                title?.let { setTitle(it) }
                artist?.let { setArtist(it) }
            }
            .build()

        val updatedMediaItem = currentMediaItem
            .buildUpon()
            .setMediaMetadata(updatedMetadata)
            .build()

        playerInstance.replaceMediaItem(currentIndex, updatedMediaItem)
    }
    private val listener = object : Player.Listener {
        override fun onIsPlayingChanged(isPlaying: Boolean) {
            _isPlaying.value = isPlaying
        }
    }

    init {
        playerInstance.addListener(listener)
        viewModelScope.launch {
            while (true) {
                _position.value = playerInstance.currentPosition
                delay(500.milliseconds)
            }
        }
    }

    override fun onCleared() {
        playerInstance.removeListener(listener)
    }
}