package com.semorka.lyryx.screens

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.semorka.lyryx.R
import com.semorka.lyryx.music.MockMusicViewModel
import com.semorka.lyryx.music.MusicViewModel
import com.semorka.lyryx.ui.theme.LyryxTheme

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

    Box(Modifier.fillMaxSize()){
        Text(
            text = "Lyryx",
            modifier = Modifier.align(Alignment.TopCenter),
            style = MaterialTheme.typography.titleLarge
        )

        Column(
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.matchParentSize()
        ) {
            OutlinedButton(
                onClick = {
                    audioPickerLauncher.launch("audio/*")
                },
                colors = ButtonDefaults.outlinedButtonColors(
                    containerColor = Color.Transparent,
                    contentColor = MaterialTheme.colorScheme.onPrimary),
                border = BorderStroke(4.dp, MaterialTheme.colorScheme.primary)
            ) {
                Text(stringResource(R.string.choose_audio), style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurface)
            }
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun LoadTrackScreenPreviewLightTheme(){
    LyryxTheme(dynamicColor = false, darkTheme = false){
        val viewModel = MockMusicViewModel.createMusicViewModel()
        Surface(Modifier.systemBarsPadding(), color = MaterialTheme.colorScheme.background){
            LoadTrackScreen(rememberNavController(), viewModel)
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun LoadTrackScreenPreviewDarkTheme(){
    LyryxTheme(dynamicColor = false, darkTheme = true){
        val viewModel = MockMusicViewModel.createMusicViewModel()
        Surface(Modifier.systemBarsPadding(), color = MaterialTheme.colorScheme.background){
            LoadTrackScreen(rememberNavController(), viewModel)
        }
    }
}