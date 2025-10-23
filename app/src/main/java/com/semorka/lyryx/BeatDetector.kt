package com.semorka.lyryx

import android.media.MediaPlayer
import android.media.audiofx.Visualizer
import android.util.Log

class BeatDetector {
    private var visualizer: Visualizer? = null
    private var lastBeatTime = 0L

    fun start(mediaPlayer: MediaPlayer, onBeatDetected: (Long) -> Unit) {
        try {
            visualizer = Visualizer(mediaPlayer.audioSessionId).apply {
                captureSize = Visualizer.getCaptureSizeRange()[0]
                setDataCaptureListener(
                    object : Visualizer.OnDataCaptureListener {
                        override fun onWaveFormDataCapture(
                            visualizer: Visualizer?,
                            waveform: ByteArray?,
                            samplingRate: Int
                        ) {
                            if (waveform != null && waveform.size >= 2) {
                                val left = waveform[0].toInt() and 0xFF
                                val right = waveform[1].toInt() and 0xFF
                                val amplitude = kotlin.math.max(left, right)

                                val currentPlayerTime = mediaPlayer.currentPosition.toLong()

                                if (amplitude > 130 &&
                                    currentPlayerTime - lastBeatTime > 400L) {

                                    lastBeatTime = currentPlayerTime
                                    onBeatDetected(currentPlayerTime)
                                }
                            }
                        }

                        override fun onFftDataCapture(
                            visualizer: Visualizer?,
                            fft: ByteArray?,
                            samplingRate: Int
                        ) {}
                    },
                    Visualizer.getMaxCaptureRate() / 3,
                    true,
                    false
                )
                enabled = true
            }
        } catch (e: Exception) {
            Log.e("BeatDetector", "Visualizer error: ${e.message}")
        }
    }

    fun stop() {
        visualizer?.release()
        visualizer = null
    }
}