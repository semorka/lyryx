package com.semorka.lyryx

import android.content.Context
import android.media.MediaPlayer
import android.net.Uri
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

open class PlayerViewModel: ViewModel() {
    var mediaPlayer: MediaPlayer? by mutableStateOf(null)

    open var currentTime by mutableStateOf(0L)

    open var isPlaying by mutableStateOf(false)

    var currentAudioUri by mutableStateOf<Uri?>(null)

    private var timeUpdateJob: Job? = null

    open fun initializePlayer(context: Context, audioUri: Uri, onPrepared: (() -> Unit)? = null) {
        if (currentAudioUri == audioUri && mediaPlayer != null) return

        cleanup()
        currentAudioUri = audioUri

        try {
            MediaPlayer().apply {
                setOnPreparedListener {
                    mediaPlayer = this
                    this@PlayerViewModel.isPlaying = false
                    onPrepared?.invoke()
                }
                setOnCompletionListener {
                    this@PlayerViewModel.isPlaying = false
                    stopTimeUpdates()
                }
                setDataSource(context, audioUri)
                prepareAsync()
            }
        } catch (e: Exception) {
            Log.e("PlayerViewModel", "Error initializing player", e)
        }
    }

    open fun playPause() {
        mediaPlayer?.let { player ->
            if (isPlaying) {
                player.pause()
                isPlaying = false
                stopTimeUpdates()
            } else {
                player.start()
                isPlaying = true
                startTimeUpdates()
            }
        }
    }

    open fun seekTo(position: Long) {
        mediaPlayer?.seekTo(position.toInt())
        currentTime = position
    }

    private fun startTimeUpdates() {
        stopTimeUpdates()
        timeUpdateJob = viewModelScope.launch {
            while (isActive) {
                mediaPlayer?.let { player ->
                    if (isPlaying) {
                        currentTime = player.currentPosition.toLong()
                    }
                }
                delay(50)
            }
        }
    }

    private fun stopTimeUpdates() {
        timeUpdateJob?.cancel()
        timeUpdateJob = null
    }

    private fun cleanup() {
        stopTimeUpdates()
        mediaPlayer?.release()
        mediaPlayer = null
        currentAudioUri = null
        isPlaying = false
        currentTime = 0L
    }

    override fun onCleared() {
        cleanup()
        super.onCleared()
    }
}