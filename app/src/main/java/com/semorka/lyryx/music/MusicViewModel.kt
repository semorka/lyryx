package com.semorka.lyryx.music

import android.net.Uri
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel

class MusicViewModel : ViewModel() {
    var searchText by mutableStateOf("Найти музыку")
    var songs by mutableStateOf<List<Music>>(emptyList())
    var currentAudioUri by mutableStateOf<Uri?>(null)
}