package com.semorka.lyryx.music

import android.content.res.Resources
import android.net.Uri
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.semorka.lyryx.R
import com.semorka.lyryx.genius.models.GeniusSong
import com.semorka.lyryx.musicApi
import kotlinx.coroutines.launch
open class MusicViewModel : ViewModel() {
    var searchText by mutableStateOf("Найти музыку")
    var songs by mutableStateOf<List<GeniusSong>>(emptyList())
    var currentAudioUri by mutableStateOf<Uri?>(null)
}