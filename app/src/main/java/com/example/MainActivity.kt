package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Casino
import androidx.compose.material.icons.filled.Equalizer
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModelProvider
import com.example.ui.screens.*
import com.example.ui.theme.MyApplicationTheme
import com.example.viewmodel.LyricViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val viewModel = ViewModelProvider(this)[LyricViewModel::class.java]

        setContent {
            MyApplicationTheme {
                MainLayout(viewModel)
            }
        }
    }
}

enum class ScreenTab {
    LYRICS, MIXER, GHOST, SUNO, COLLAB
}

@Composable
fun MainLayout(viewModel: LyricViewModel) {
    var currentTab by remember { mutableStateOf(ScreenTab.LYRICS) }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = Color(0xFF0A080A),
        contentWindowInsets = WindowInsets.safeDrawing, // Edge-to-edge support!
        bottomBar = {
            Column(
                modifier = Modifier
                    .background(Color(0xFF0A080A))
                    .navigationBarsPadding() // Respect device navigation bar/gesture pill!
            ) {
                // Nav Bar
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.SpaceAround,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    BottomTabButton(
                        label = "LYRICS",
                        icon = Icons.Default.MusicNote,
                        isActive = currentTab == ScreenTab.LYRICS,
                        onClick = { currentTab = ScreenTab.LYRICS }
                    )
                    BottomTabButton(
                        label = "MIXER",
                        icon = Icons.Default.Equalizer,
                        isActive = currentTab == ScreenTab.MIXER,
                        onClick = { currentTab = ScreenTab.MIXER }
                    )
                    BottomTabButton(
                        label = "GHOST",
                        icon = Icons.Default.AutoAwesome,
                        isActive = currentTab == ScreenTab.GHOST,
                        onClick = { currentTab = ScreenTab.GHOST }
                    )
                    BottomTabButton(
                        label = "SUNO",
                        icon = Icons.Default.Casino,
                        isActive = currentTab == ScreenTab.SUNO,
                        onClick = { currentTab = ScreenTab.SUNO }
                    )
                    BottomTabButton(
                        label = "COLLAB",
                        icon = Icons.Default.Folder,
                        isActive = currentTab == ScreenTab.COLLAB,
                        onClick = { currentTab = ScreenTab.COLLAB }
                    )
                }

                // Android Navigation Bar Capsule Pill indicator (matches theme)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(width = 64.dp, height = 5.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(Color.White.copy(alpha = 0.2f))
                    )
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (currentTab) {
                ScreenTab.LYRICS -> LyricsScreen(viewModel = viewModel, onNavigateToMixer = { currentTab = ScreenTab.MIXER })
                ScreenTab.MIXER -> MixerScreen(viewModel = viewModel)
                ScreenTab.GHOST -> GhostScreen(viewModel = viewModel)
                ScreenTab.SUNO -> SunoScreen(viewModel = viewModel)
                ScreenTab.COLLAB -> CollabScreen(viewModel = viewModel, onNavigateToLyrics = { currentTab = ScreenTab.LYRICS })
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BottomTabButton(
    label: String,
    icon: ImageVector,
    isActive: Boolean,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp),
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) { onClick() }
            .padding(vertical = 8.dp, horizontal = 12.dp)
            .widthIn(min = 64.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = if (isActive) Color(0xFFFF007A) else Color.White.copy(alpha = 0.4f),
            modifier = Modifier.size(24.dp)
        )
        Text(
            text = label,
            color = if (isActive) Color(0xFFFF007A) else Color.White.copy(alpha = 0.4f),
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 0.5.sp
        )
    }
}
