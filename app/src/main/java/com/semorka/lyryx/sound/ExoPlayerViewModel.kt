package com.semorka.lyryx.sound

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerNotificationManager
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

private const val TAG = "LYRYX"

@UnstableApi
class ExoPlayerViewModel : ViewModel() {
    private var notificationManager: PlayerNotificationManager? = null
    private var currentTrackTitle by mutableStateOf("Неизвестный трек")
    private var currentArtist by mutableStateOf("Неизвестный исполнитель")

    var currentTime by mutableLongStateOf(0L)
        private set
    var isPlaying by mutableStateOf(false)
        private set
    var lastBeatTime by mutableLongStateOf(0L)
        private set

    var player: ExoPlayer? = null
        private set

    private var beatDetector: BeatDetector? = null

    private var audioSessionId by mutableIntStateOf(0)

    private var timeUpdateJob: Job? = null
    private var lastAcceptedBeatTime = 0L

    fun initPlayer(context: Context, audioUri: Uri) {
        if (player != null) return

        createMediaNotificationChannel(context)

        beatDetector = BeatDetector()

        player = ExoPlayer.Builder(context).build().apply {
            setMediaItem(
                MediaItem.Builder()
                    .setMediaId("media-1")
                    .setUri(audioUri)
                    .setMediaMetadata(
                        MediaMetadata.Builder()
                            .setArtist("David Bow")
                            .setTitle("Heroes")
                            .build()
                    )
                    .build()
            )
            addListener(playerListener)
            prepare()
        }
        setupNotificationManager(context)
    }

    fun togglePlayPause() {
        if (player?.isPlaying == true) {
            player?.pause()
            notificationManager?.setPlayer(null)
            Log.d(TAG, "Notification: hidden (pause)")
        } else {
            if (player?.playbackState == Player.STATE_ENDED) {
                player?.seekTo(0)
            }
            player?.play()

            notificationManager?.let { nm ->
                nm.setPlayer(player)
                Log.d(TAG, "Notification: shown (play), player connected")
            } ?: run {
                Log.e(TAG, "Notification manager is null!")
            }
        }
    }

    private val playerListener = object : Player.Listener {
        override fun onIsPlayingChanged(isPlaying: Boolean) {
            this@ExoPlayerViewModel.isPlaying = isPlaying
            if (isPlaying) {
                startTimeUpdates()
                startBeatDetector()
            } else {
                stopTimeUpdates()
                stopBeatDetector()
            }
        }

        override fun onAudioSessionIdChanged(sessionId: Int) {
            audioSessionId = sessionId
            if (isPlaying) {
                startBeatDetector()
            }
        }
    }

    private fun startBeatDetector() {
        if (audioSessionId == 0) {
            Log.w(TAG, "ExoPlayerViewModel: audioSession is 0")
            return
        }

        beatDetector?.start(audioSessionId) {
            onBeatDetected()
        }
    }

    private fun stopBeatDetector() {
        beatDetector?.stop()
    }

    private fun onBeatDetected() {
        val currentPosition = player?.currentPosition ?: 0L
        if (currentPosition - lastAcceptedBeatTime > 150) {
            lastAcceptedBeatTime = currentPosition
            this.lastBeatTime = currentPosition
        } else {
            Log.v(TAG, "ExoPlayerViewModel: Bit ignored")
        }
    }

    private fun startTimeUpdates() {
        timeUpdateJob?.cancel()
        timeUpdateJob = viewModelScope.launch {
            while (true) {
                currentTime = player?.currentPosition ?: 0L
                delay(50)
            }
        }
    }

    private fun stopTimeUpdates() {
        timeUpdateJob?.cancel()
    }

    fun updateTrackInfo(title: String, artist: String) {
        currentTrackTitle = title
        currentArtist = artist
        notificationManager?.invalidate()
    }

    override fun onCleared() {
        stopBeatDetector()
        stopTimeUpdates()
        player?.removeListener(playerListener)
        player?.release()

        notificationManager?.setPlayer(null)
        notificationManager = null

        super.onCleared()
    }

    @Suppress("DEPRECATION")
    private fun createMediaNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "Media Playback"
            val descriptionText = "Controls for music playback"
            val importance = NotificationManager.IMPORTANCE_LOW

            val channel = NotificationChannel(
                "media_channel",
                name,
                importance
            ).apply {
                description = descriptionText
                setShowBadge(false)
                lockscreenVisibility = Notification.VISIBILITY_PUBLIC
                enableLights(false)
                enableVibration(false)
            }

            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
            Log.d(TAG, "Media notification channel created: media_channel")
        } else {
            Log.d(TAG, "Notification channel not needed (API < 26)")
        }
    }

    private fun setupNotificationManager(context: Context) {
        notificationManager = PlayerNotificationManager.Builder(
            context,
            1001,
            "media_channel"
        )
            .setMediaDescriptionAdapter(object : PlayerNotificationManager.MediaDescriptionAdapter {
                override fun getCurrentContentTitle(player: Player): CharSequence {
                    val metadata = player.currentMediaItem?.mediaMetadata
                    return metadata?.title ?: "Неизвестный трек"
                }

                override fun getCurrentContentText(player: Player): CharSequence? {
                    val metadata = player.currentMediaItem?.mediaMetadata
                    return metadata?.artist ?: "Неизвестный исполнитель"
                }

                override fun getCurrentLargeIcon(
                    player: Player,
                    callback: PlayerNotificationManager.BitmapCallback
                ): Bitmap? = null

                override fun createCurrentContentIntent(player: Player): PendingIntent? = null
            })
            .build()
        Log.d(TAG, "Notification manager created with media_channel")
    }

}
