package com.semorka.lyryx.screens

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.OpenableColumns
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.semorka.lyryx.data.BaseLyricsViewModel
import com.semorka.lyryx.data.MockLyricsViewModel
import com.semorka.lyryx.music.MockMusicViewModel
import com.semorka.lyryx.music.MusicViewModel
import com.semorka.lyryx.ui.theme.LyryxTheme
import androidx.compose.ui.platform.LocalResources
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import coil.compose.AsyncImage
import com.semorka.lyryx.R
import com.semorka.lyryx.data.LyricsEntity
import com.semorka.lyryx.genius.GeniusRepository
import com.semorka.lyryx.genius.models.GeniusSong
import com.semorka.lyryx.musicApi
import kotlinx.coroutines.launch
import org.jaudiotagger.audio.AudioFileIO
import org.jaudiotagger.tag.FieldKey
import java.io.File

@Composable
fun SearchScreen(navController: NavController = rememberNavController(), musicVm: MusicViewModel, lyricVm: BaseLyricsViewModel) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val resources = LocalResources.current
    var songNameSearch by remember { mutableStateOf("") }
    var artistNameSearch by remember { mutableStateOf("") }

    val GeniusRep = GeniusRepository()

    val fileName = musicVm.currentAudioUri?.let { getFileName(LocalContext.current, it) } ?: "Unknown"

    var showPermissionDialog by remember { mutableStateOf(false) }
    var selectedSong by remember { mutableStateOf<GeniusSong?>(null) }

    LaunchedEffect(musicVm.currentAudioUri) {
        musicVm.currentAudioUri?.let { uri ->
            val file = getFileFromUri(context, uri)
            val info = getTrackInfo(file)
            songNameSearch = info.title
            artistNameSearch = info.artist
        }
    }

    Column(Modifier.padding(16.dp).fillMaxSize(), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)){
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
                    if("$songNameSearch$artistNameSearch".isNotEmpty()){
                        scope.launch {
                            musicVm.searchText = context.getString(R.string.song_searching).withEllipsis()
                            musicVm.songs = GeniusRep.searchSongs(query = "$songNameSearch $artistNameSearch")
                            if(musicVm.songs.isNotEmpty()) musicVm.searchText = resources.getQuantityString(R.plurals.songs_found, musicVm.songs.size)
                            else musicVm.searchText = context.getString(R.string.search_nothing)
                        }
                    }
                    else {
                        musicVm.searchText = resources.getStringArray(R.array.enter_song).random()
                    }
                },
                shape = RoundedCornerShape(25),
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(MaterialTheme.colorScheme.primary)
            ) {
                Text(stringResource(R.string.search), style = MaterialTheme.typography.labelMedium)
            }
        }

        Text(musicVm.searchText)

        LazyColumn(verticalArrangement = Arrangement.spacedBy(16.dp), modifier = Modifier.fillMaxWidth()) {
            items(musicVm.songs) { song ->
                Row(Modifier.fillMaxWidth().clickable(
                    enabled = true,
                    onClick = {
                        selectedSong = song
                        showPermissionDialog = true
                    }
                )){
                    PreviewAsyncImage(
                        model = song.song_art_image_url,
                        contentDescription = "",
                        modifier = Modifier.size(100.dp).clip(RoundedCornerShape(20))
                    )
                    Column {
                        Text(song.title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        Text(song.primary_artist.name, style = MaterialTheme.typography.titleSmall)
                    }
                }
            }
        }

        if (showPermissionDialog) {
            PermissionDialog( {
                showPermissionDialog = false
                selectedSong?.let { song ->
                    scope.launch {
                        lyricVm.searchLyrics(song.primary_artist.name, song.title)
                        if(lyricVm.searchResult.isNotEmpty()){
                            musicVm.searchText = context.getString(R.string.song_founded_library).withEllipsis()
                            val result = lyricVm.searchResult[0]
                            navController.navigate("Lyrics/${result.artistName}/${result.songName}/${result.syncedText}")
                        } else {
                            musicVm.searchText = context.getString(R.string.song_loading_text).withEllipsis()
                            val result = musicApi.searchMusic("$artistNameSearch $songNameSearch").first()
                            navController.navigate("Lyrics/${result.artistName}/${result.name}/${result.syncedLyrics}")
                        }
                    }
                }
            })
        }
    }
}

@Composable
private fun PermissionDialog(event: () -> Unit) {
    val context = LocalContext.current

    val hasPermission = remember {
        ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.RECORD_AUDIO
        ) == PackageManager.PERMISSION_GRANTED
    }

    var showDialog by remember { mutableStateOf(!hasPermission) }

    val launcher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        Log.d("LYRYX", if (isGranted) "PERMISSION GRANTED" else "PERMISSION DENIED")
        event()
    }

    LaunchedEffect(hasPermission) {
        if (hasPermission) {
            event()
        }
    }
    if (showDialog) {
        AlertDialog(
            onDismissRequest = {
                showDialog = false
                launcher.launch(Manifest.permission.RECORD_AUDIO)
            },
            title = { Text("Microphone Access Required") },
            text = {
                Text(text = "Allow microphone access in the next window to sync lyrics with music beats.", style = MaterialTheme.typography.bodyLarge)
            },
            confirmButton = {
                Button(
                    onClick = {
                        showDialog = false
                        launcher.launch(Manifest.permission.RECORD_AUDIO)
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("OK")
                }
            }
        )
    }
}

@Preview
@Composable
private fun PreviewPermDialog(){
    LyryxTheme {
        AlertDialog(
            onDismissRequest = {
            },
            title = { Text("Microphone Access Required") },
            text = {
                Text(text = "Allow microphone access in the next window to sync lyrics with music beats.", style = MaterialTheme.typography.bodyLarge)
            },
            confirmButton = {
                Button(
                    onClick = {
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("OK")
                }
            }
        )
    }
}

private fun getFileName(context: Context, uri: Uri): String {
    return try {
        context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
            cursor.moveToFirst()
            cursor.getString(cursor.getColumnIndexOrThrow(OpenableColumns.DISPLAY_NAME))
        } ?: "Unknown"
    } catch (e: Exception) {
        "Unknown"
    }
}

private fun getFileFromUri(context: Context, uri: Uri): File? {
    return try {
        if (uri.scheme == "file") {
            File(uri.path!!)
        }
        else if (uri.scheme == "content") {
            val inputStream = context.contentResolver.openInputStream(uri)
            val fileName = getFileName(context, uri)
            val file = File(context.cacheDir, fileName)

            inputStream?.use { input ->
                file.outputStream().use { output ->
                    input.copyTo(output)
                }
            }
            file
        } else {
            null
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

fun String.withEllipsis(): String = "$this..."

@Composable
fun PreviewAsyncImage(
    model: Any?,
    contentDescription: String?,
    modifier: Modifier = Modifier
) {
    if (LocalInspectionMode.current) {
        Box(modifier = modifier.background(MaterialTheme.colorScheme.primary))
    } else {
        AsyncImage(
            model = model,
            contentDescription = contentDescription,
            modifier = modifier
        )
    }
}


@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun PreviewSearchScreen(){
    val musicVm = MockMusicViewModel.createMusicViewModel()
    val lyricsVm = MockLyricsViewModel("", "", "")
    LyryxTheme{
        Surface(Modifier.systemBarsPadding().fillMaxSize()){
            SearchScreen(rememberNavController(), musicVm, lyricsVm)
        }
    }
}