package com.semorka.lyryx.net.deezer

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.semorka.lyryx.net.networkClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.http.isSuccess
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch


class DeezerViewModel: ViewModel(){
    private val _trackState = MutableStateFlow<List<DeezerTrack>>(emptyList())
    val trackState = _trackState.asStateFlow()
    fun findTrack(query: String) {
        viewModelScope.launch {
            try {
                val response = networkClient.get("https://api.deezer.com/search") {
                    parameter("q", query)
                }
                if (response.status.isSuccess()) {
                    val wrapper = response.body<DeezerResponse>()
                    _trackState.value = wrapper.data
                }
            } catch (e: Exception) {
                e.printStackTrace()
                _trackState.value = emptyList()
            }
        }
    }
}