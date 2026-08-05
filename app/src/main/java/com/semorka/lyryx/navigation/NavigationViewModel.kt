package com.semorka.lyryx.navigation

import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.ViewModel

class NavigationViewModel : ViewModel() {
    private val _backStack = mutableStateListOf<Destination>(Destination.LoadTrack)

    val backStack: List<Destination> get() = _backStack

    val currentScreen: Destination?
        get() = _backStack.lastOrNull()

    fun navigateTo(screen: Destination) {
        if (currentScreen != screen) {
            _backStack.remove(screen)
            _backStack.add(screen)
        }
    }

    fun onBack() {
        _backStack.removeLastOrNull()
    }
}