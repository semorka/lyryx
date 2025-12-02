package com.semorka.lyryx.screens

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.modifier.modifierLocalConsumer
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.semorka.lyryx.R
import com.semorka.lyryx.music.MockMusicViewModel
import com.semorka.lyryx.music.MusicViewModel
import com.semorka.lyryx.ui.theme.LyryxTheme
import kotlinx.coroutines.delay

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

//            Row(
//                modifier = Modifier.fillMaxWidth(),
//                horizontalArrangement = Arrangement.Center
//            ){
//                Row(
//                    horizontalArrangement = Arrangement.spacedBy(16.dp),
//                    verticalAlignment = Alignment.CenterVertically
//                ){
//                    Text(
//                        text = "LRCLIB",
//                        style = MaterialTheme.typography.bodyLarge
//                    )
//                    Text(
//                        text = "GENIUS",
//                        style = MaterialTheme.typography.bodyLarge
//                    )
//                    Text(
//                        text = "SUPABASE",
//                        style = MaterialTheme.typography.bodyLarge
//                    )
//                }
//            }
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
            viewModel.currentAudioUri?.let {
                OutlinedButton(
                    onClick = {
                        navController.navigate("Search")
                    },
                    modifier = Modifier.width(250.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        containerColor = Color.Transparent,
                        contentColor = MaterialTheme.colorScheme.onPrimary),
                    border = BorderStroke(4.dp, MaterialTheme.colorScheme.primary)
                ) {
                    Text("Find lyrics online", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurface)
                }
                OutlinedButton(
                    onClick = {
                        navController.navigate("Library")
                    },
                    modifier = Modifier.width(250.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        containerColor = Color.Transparent,
                        contentColor = MaterialTheme.colorScheme.onPrimary),
                    border = BorderStroke(4.dp, MaterialTheme.colorScheme.primary)
                ) {
                    Text("Saved lyrics", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurface)
                }
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

@Composable
fun TextCarousel(
    texts: List<String> = listOf("GENIUS", "LRCLIB", "SUPABASE"),
    modifier: Modifier = Modifier
) {
    var currentIndex by remember { mutableIntStateOf(0) }

    LaunchedEffect(Unit) {
        while (true) {
            delay(3000)
            currentIndex = (currentIndex + 1) % texts.size
        }
    }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(28.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            AnimatedContent(
                targetState = currentIndex,
                transitionSpec = {
                    slideIntoContainer(
                        towards = AnimatedContentTransitionScope.SlideDirection.Start,
                        animationSpec = tween(500)
                    ) togetherWith slideOutOfContainer(
                        towards = AnimatedContentTransitionScope.SlideDirection.Start,
                        animationSpec = tween(500)
                    )
                },
                label = "text-carousel"
            ) { index ->
                Text(
                    text = texts[index],
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}