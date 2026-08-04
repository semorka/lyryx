package com.semorka.lyryx.sound

import android.media.audiofx.Visualizer
import android.util.Log
import kotlin.math.sqrt

private const val TAG = "LYRYX"

class BeatDetector {
    private var visualizer: Visualizer? = null
    private var isRunning = false

    fun start(audioSessionId: Int, onBeatDetected: () -> Unit) {

        if (audioSessionId == 0) {
            Log.e(TAG, "BeatDetector: Error - Audio session ID is 0.")
            return
        }

        stop()
        isRunning = true

        try {
            visualizer = Visualizer(audioSessionId).apply {
                captureSize = Visualizer.getCaptureSizeRange()[1].coerceAtMost(1024)
                val captureRate = Visualizer.getMaxCaptureRate()

                setDataCaptureListener(
                    object : Visualizer.OnDataCaptureListener {
                        private val energyHistory = FloatArray(15)
                        private var historyIndex = 0
                        private var isHistoryFull = false

                        override fun onWaveFormDataCapture(v: Visualizer, w: ByteArray, s: Int) {}

                        override fun onFftDataCapture(
                            visualizer: Visualizer,
                            fft: ByteArray,
                            samplingRate: Int
                        ) {
                            if (!isRunning) return

                            val currentBassEnergy = calculateBassEnergy(fft, samplingRate)
                            val averageEnergy = calculateAverageEnergy()


                            val sensitivityMultiplier = 1.08f
                            val energyThresholdOffset = 25.0f

                            val dynamicThreshold = (averageEnergy * sensitivityMultiplier) + energyThresholdOffset

                            if (currentBassEnergy > dynamicThreshold) {
                                onBeatDetected()
                            }

                            updateHistory(currentBassEnergy)
                        }

                        private fun calculateBassEnergy(fft: ByteArray, samplingRate: Int): Float {
                            val frequencyResolution = samplingRate.toFloat() / captureSize
                            val minBassFreq = 60
                            val maxBassFreq = 250

                            val minBinIndex = (minBassFreq / frequencyResolution).toInt()
                            val maxBinIndex = (maxBassFreq / frequencyResolution).toInt()

                            var totalEnergy = 0f
                            for (i in minBinIndex..maxBinIndex) {
                                val re = fft[i * 2].toInt()
                                val im = fft[i * 2 + 1].toInt()
                                totalEnergy += sqrt((re * re + im * im).toFloat())
                            }
                            return totalEnergy
                        }

                        private fun calculateAverageEnergy(): Float {
                            val count = if (isHistoryFull) energyHistory.size else historyIndex
                            if (count == 0) return 0f
                            return energyHistory.take(count).average().toFloat()
                        }

                        private fun updateHistory(newValue: Float) {
                            energyHistory[historyIndex] = newValue
                            historyIndex++
                            if (historyIndex >= energyHistory.size) {
                                historyIndex = 0
                                isHistoryFull = true
                            }
                        }
                    },
                    captureRate,
                    false,
                    true
                )
                enabled = true
            }
            Log.i(TAG, "BeatDetector: Visualizer started successfully in FFT mode.")
        } catch (e: Exception) {
            Log.e(TAG, "BeatDetector: Error initializing FFT Visualizer - ${e.message}", e)
            stop()
        }
    }

    fun stop() {
        if (visualizer != null) {
            isRunning = false
            visualizer?.release()
            visualizer = null
        }
    }
}
