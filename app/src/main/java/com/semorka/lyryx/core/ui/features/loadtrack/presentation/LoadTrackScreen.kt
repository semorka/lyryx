package com.semorka.lyryx.core.ui.features.loadtrack.presentation

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.width
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.semorka.lyryx.R
import com.semorka.lyryx.core.music.MusicViewModel
import com.semorka.lyryx.core.ui.features.loadtrack.TextCarousel

@Composable
fun LoadTrackScreen(
    viewModel: MusicViewModel,
    onFileSelected: () -> Unit
){
    val audioPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent(),
        onResult = { uri ->
            uri?.let {
                viewModel.currentAudioUri = it
                onFileSelected()
            }
        }
    )

    Box(Modifier.fillMaxSize()){
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.CenterHorizontally
        ){
            Text(
                text = "Lyryx",
                style = MaterialTheme.typography.titleLarge
            )

            Text(
                text = "Powered by",
                style = MaterialTheme.typography.titleSmall
            )

            TextCarousel()
        }

        Column(
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.matchParentSize()
        ) {
            OutlinedButton(
                onClick = {
                    audioPickerLauncher.launch("audio/*")
                },
                modifier = Modifier.width(250.dp),
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