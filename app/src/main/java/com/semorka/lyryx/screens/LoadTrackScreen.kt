package com.semorka.lyryx.screens

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.semorka.lyryx.music.MockMusicViewModel
import com.semorka.lyryx.music.MusicViewModel

@Composable
fun LoadTrackScreen(
    navController: NavController,
    viewModel: MusicViewModel
){
    val audioPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent(),
        onResult = { uri ->
            uri?.let {
                viewModel.currentAudioUri = it
                navController.navigate("Search")
            }
        }
    )

    Column(
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.fillMaxSize()
    ) {
        Button(
            onClick = {
                audioPickerLauncher.launch("audio/*")
            }
        ) {
            Text("Выбрать аудиофайл")
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun LoadTrackScreenPreview(){
    val viewModel = MockMusicViewModel.createMusicViewModel()
    Box(Modifier.systemBarsPadding()){
        LoadTrackScreen(rememberNavController(), viewModel)
    }
}