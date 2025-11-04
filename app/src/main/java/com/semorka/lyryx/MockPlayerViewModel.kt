package com.semorka.lyryx

import android.content.Context
import android.net.Uri
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch

class MockPlayerViewModel : PlayerViewModel() {

    private val _currentTime = mutableStateOf(1500L)
    private val _isPlaying = mutableStateOf(false)

    override var currentTime: Long = 0
        get() = _currentTime.value

    override var isPlaying: Boolean = false
        get() = _isPlaying.value

    // Mock методы
    override fun initializePlayer(context: Context, audioUri: Uri, onPrepared: (() -> Unit)?) {
        if (onPrepared != null) {
            MainScope().launch {
                kotlinx.coroutines.delay(500)
                onPrepared.invoke()
            }
        }
    }

    override fun playPause() {
        _isPlaying.value = !_isPlaying.value
        if (_isPlaying.value) {
            startMockTimeUpdates()
        }
    }

    override fun seekTo(position: Long) {
        _currentTime.value = position
    }

    private fun startMockTimeUpdates() {
        MainScope().launch {
            while (_isPlaying.value) {
                kotlinx.coroutines.delay(100)
                _currentTime.value += 100
                if (_currentTime.value > 300000) _currentTime.value = 0
            }
        }
    }

    fun setMockTime(time: Long) {
        _currentTime.value = time
    }

    fun setMockPlaying(playing: Boolean) {
        _isPlaying.value = playing
    }
}