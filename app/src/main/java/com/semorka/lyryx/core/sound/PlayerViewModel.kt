package com.semorka.lyryx.core.sound

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.session.MediaController
import com.google.common.util.concurrent.ListenableFuture
import com.google.common.util.concurrent.MoreExecutors
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.time.Duration.Companion.milliseconds

@HiltViewModel
class PlayerViewModel @Inject constructor(
    private val controllerFuture: ListenableFuture<MediaController>
) : ViewModel() {

    private var playerInstance: Player? = null

    private val _isPlaying = MutableStateFlow(false)
    val isPlaying = _isPlaying.asStateFlow()

    private val _position = MutableStateFlow(0L)
    val position = _position.asStateFlow()

    val currentPosition: Long
        get() = playerInstance?.currentPosition ?: 0L

    private val listener = object : Player.Listener {
        override fun onIsPlayingChanged(isPlaying: Boolean) {
            _isPlaying.value = isPlaying
        }
    }

    init {
        controllerFuture.addListener({
            try {
                val player = controllerFuture.get()
                playerInstance = player
                _isPlaying.value = player.isPlaying
                player.addListener(listener)

                startPositionTracker()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }, MoreExecutors.directExecutor())
    }

    private fun startPositionTracker() {
        viewModelScope.launch {
            while (true) {
                playerInstance?.let { player ->
                    _position.value = player.currentPosition
                }
                delay(500.milliseconds)
            }
        }
    }

    fun togglePlay() {
        val player = playerInstance ?: return
        if (player.isPlaying) player.pause() else player.play()
    }

    fun setMedia(uri: Uri) {
        val player = playerInstance ?: return
        val currentUri = player.currentMediaItem?.localConfiguration?.uri
        if (currentUri == uri) return

        player.setMediaItem(
            MediaItem.fromUri(uri)
        )
        player.prepare()
    }

    fun updateCurrentMediaMetadata(
        coverUri: Uri?,
        title: String? = null,
        artist: String? = null
    ) {
        val player = playerInstance ?: return
        val currentMediaItem = player.currentMediaItem ?: return
        val currentIndex = player.currentMediaItemIndex

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

        player.replaceMediaItem(currentIndex, updatedMediaItem)
    }

    override fun onCleared() {
        playerInstance?.removeListener(listener)
    }
}