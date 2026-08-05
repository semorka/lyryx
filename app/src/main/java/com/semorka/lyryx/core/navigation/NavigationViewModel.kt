package com.semorka.lyryx.core.navigation

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class NavigationViewModel : ViewModel() {
    private val _backStack = MutableStateFlow<List<Destination>>(listOf(Destination.LoadTrack))
    val backStack = _backStack.asStateFlow()

    val currentScreen: Destination?
        get() = _backStack.value.lastOrNull()

    fun navigateTo(screen: Destination) {
        if (currentScreen != screen) {
            _backStack.update { currentList ->
                currentList.filterNot { it == screen } + screen
            }
        }
    }

    fun onBack() {
        if (_backStack.value.size > 1) {
            _backStack.update { currentList ->
                currentList.dropLast(1)
            }
        }
    }

    fun finishSearchAndOpenPlayer() {
        _backStack.update { currentList ->
            val rootStack = currentList.filterNot { it is Destination.SearchFlow }
            rootStack + Destination.Lyrics
        }
    }
}