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

//    fun searchSongs(songName: String, artistName: String, resources: Resources) {
//        viewModelScope.launch {
//            searchText = "Ищем тексты..."
//
//            try {
//                val result = musicApi.searchMusic("$artistName $songName")
//                if (result.isNotEmpty()) {
//                    songs = result
//                    searchText= resources.getQuantityString(
//                        R.plurals.songs_found,
//                        result.size,
//                        result.size
//                    )
//                } else {
//                    searchText = "Ничего не найдено"
//                }
//            } catch (e: Exception) {
//                searchText = "Ошибка: ${e.message}"
//            }
//        }
//    }
}