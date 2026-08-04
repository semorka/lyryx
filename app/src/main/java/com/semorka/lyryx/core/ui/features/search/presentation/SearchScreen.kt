package com.semorka.lyryx.core.ui.features.search.presentation

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.semorka.lyryx.core.music.MusicViewModel
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.semorka.lyryx.R
import com.semorka.lyryx.core.data.Track
import com.semorka.lyryx.net.deezer.DeezerViewModel
import com.semorka.lyryx.net.lrclib.LRCLibViewModel
import com.semorka.lyryx.net.word_lyrics.WordLyricsViewModel
import org.jaudiotagger.audio.AudioFileIO
import org.jaudiotagger.tag.FieldKey
import java.io.File

@Composable
fun SearchScreen(
    musicVm: MusicViewModel,
    lyricsVm: LRCLibViewModel,
    wordsVm: WordLyricsViewModel,
    onTrackSelected: () -> Unit
) {
    val context = LocalContext.current
    var songNameSearch by remember { mutableStateOf("") }
    var artistNameSearch by remember { mutableStateOf("") }

    val deezerVm: DeezerViewModel = viewModel()
    val tracks by deezerVm.trackState.collectAsStateWithLifecycle()

    val fileName =
        musicVm.currentAudioUri?.let { getFileName(LocalContext.current, it) } ?: "Unknown"

    LaunchedEffect(musicVm.currentAudioUri) {
        musicVm.currentAudioUri?.let { uri ->
            val file = getFileFromUri(context, uri)
            val info = getTrackInfo(file)
            songNameSearch = info.title
            artistNameSearch = info.artist
        }
    }

    Column(
        Modifier.padding(16.dp).fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            if (musicVm.currentAudioUri != null) {
                Text("${stringResource(R.string.file_name)}: $fileName")
                Spacer(Modifier.height(8.dp))
            }

            OutlinedTextField(
                value = songNameSearch,
                onValueChange = { songNameSearch = it },
                modifier = Modifier.fillMaxWidth(),
                label = { Text(stringResource(R.string.name)) }
            )
            OutlinedTextField(
                value = artistNameSearch,
                onValueChange = { artistNameSearch = it },
                modifier = Modifier.fillMaxWidth(),
                label = { Text(stringResource(R.string.artist)) }
            )

            Button(
                onClick = {
                    deezerVm.findTrack("$artistNameSearch $songNameSearch")
                },
                shape = RoundedCornerShape(25),
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(MaterialTheme.colorScheme.primary)
            ) {
                Text("Search", style = MaterialTheme.typography.labelMedium)
            }
        }

        Text(musicVm.searchText)

        LazyColumn {
            items(tracks) { track ->
                Row(modifier = Modifier.clickable(onClick = {
                    musicVm.track = Track(
                        artistName = track.artist.name,
                        trackName = track.title,
                        cover = track.album.cover_medium,
                        plainLyrics = ""
                    )
                    lyricsVm.findLyricsSmart(track.artist.name, track.title, track.duration)
                    wordsVm.fetchLyrics(
                        track = track.title,
                        artist = track.artist.name,
                        durationSec = track.duration.toLong(),
                        deezerId = track.id.toString()
                    )
                    onTrackSelected()
                })) {
                    AsyncImage(
                        model = track.album.cover_medium,
                        contentDescription = "Album cover",
                        modifier = Modifier.size(64.dp)
                    )
                    Text(track.title)
                }
            }
        }
    }
}

private fun getFileName(context: Context, uri: Uri): String {
    return try {
        context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
            cursor.moveToFirst()
            cursor.getString(cursor.getColumnIndexOrThrow(OpenableColumns.DISPLAY_NAME))
        } ?: "Unknown"
    } catch (e: Exception) {
        "Unknown: ${e.localizedMessage}"
    }
}

private fun getFileFromUri(context: Context, uri: Uri): File? {
    return try {
        when (uri.scheme) {
            "file" -> {
                File(uri.path!!)
            }
            "content" -> {
                val inputStream = context.contentResolver.openInputStream(uri)
                val fileName = getFileName(context, uri)
                val file = File(context.cacheDir, fileName)

                inputStream?.use { input ->
                    file.outputStream().use { output ->
                        input.copyTo(output)
                    }
                }
                file
            }
            else -> {
                null
            }
        }
    } catch (e: Exception) {
        null
    }
}

private fun getTrackInfo(file: File?): ParsedTrack {
    if (file == null) {
        return ParsedTrack("", "")
    }

    return try {
        val audioFile = AudioFileIO.read(file)
        val tag = audioFile.tag

        val artist = tag.getFirst(FieldKey.ARTIST).takeIf { it.isNotBlank() }
        val title = tag.getFirst(FieldKey.TITLE).takeIf { it.isNotBlank() }

        if (artist != null && title != null) {
            ParsedTrack(artist, title)
        } else {
            parseMusicFilename(file.name)
        }
    } catch (e: Exception) {
        parseMusicFilename(file.name)
    }
}

private fun parseMusicFilename(filename: String): ParsedTrack {
    val cleanName = filename.substringBeforeLast(".")
    val parts = cleanName.split(Regex("\\s*-\\s*|\\s+_\\s+"), limit = 2)

    return when (parts.size) {
        2 -> ParsedTrack(parts[0].trim(), parts[1].trim())
        else -> ParsedTrack("", cleanName)
    }
}

private data class ParsedTrack(val artist: String, val title: String)