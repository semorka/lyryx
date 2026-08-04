package com.semorka.lyryx.music

import android.net.Uri
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.semorka.lyryx.data.Track

open class MusicViewModel : ViewModel() {
    var searchText by mutableStateOf("Найти музыку")
    var track by mutableStateOf<Track?>(null)
    var currentAudioUri by mutableStateOf<Uri?>(null)
}