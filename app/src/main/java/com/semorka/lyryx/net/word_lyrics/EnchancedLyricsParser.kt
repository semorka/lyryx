package com.semorka.lyryx.net.word_lyrics

import java.util.regex.Pattern

object EnhancedLyricsParser {
    private val lineHeaderPattern = Pattern.compile("^\\[(\\d{2}):(\\d{2})\\.(\\d{2,3})](.*)$")
    private val wordTagPattern = Pattern.compile("<(\\d{2}):(\\d{2})\\.(\\d{2,3})>([^<]+)")

    fun parseEnhancedLrc(rawLrc: String): List<LyricSegment> {
        if (rawLrc.isBlank()) return emptyList()

        val result = mutableListOf<LyricSegment>()

        for (line in rawLrc.lines()) {
            val trimmed = line.trim()
            if (trimmed.isEmpty()) continue

            val lineMatcher = lineHeaderPattern.matcher(trimmed)
            if (lineMatcher.matches()) {
                val lineMs = parseTimeToMillis(
                    lineMatcher.group(1),
                    lineMatcher.group(2),
                    lineMatcher.group(3)
                )
                val body = lineMatcher.group(4)?.trim() ?: ""

                val wordMatcher = wordTagPattern.matcher(body)
                val wordSegments = mutableListOf<WordSegment>()
                val plainWordsText = StringBuilder()

                while (wordMatcher.find()) {
                    val wordMs = parseTimeToMillis(
                        wordMatcher.group(1),
                        wordMatcher.group(2),
                        wordMatcher.group(3)
                    )
                    val wordText = wordMatcher.group(4) ?: ""

                    wordSegments.add(WordSegment(timeMillis = wordMs, word = wordText.trim()))
                    plainWordsText.append(wordText)
                }

                val displayText = if (wordSegments.isNotEmpty()) {
                    plainWordsText.toString().trim()
                } else {
                    body
                }

                result.add(
                    LyricSegment(
                        lineTimeMillis = lineMs,
                        text = displayText,
                        words = wordSegments
                    )
                )
            }
        }
        return result
    }

    private fun parseTimeToMillis(min: String?, sec: String?, frac: String?): Long {
        val minutes = min?.toLong() ?: 0L
        val seconds = sec?.toLong() ?: 0L
        val fraction = frac ?: "00"
        val multiplier = if (fraction.length == 2) 10 else 1
        val milliseconds = fraction.toLong() * multiplier

        return (minutes * 60 + seconds) * 1000 + milliseconds
    }
}