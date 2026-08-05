package com.semorka.lyryx.core.music

import android.net.Uri
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.ViewModel
import com.semorka.lyryx.R
import com.semorka.lyryx.core.data.Track

open class MusicViewModel : ViewModel() {
    var track by mutableStateOf<Track?>(null)
    var currentAudioUri by mutableStateOf<Uri?>(null)
}