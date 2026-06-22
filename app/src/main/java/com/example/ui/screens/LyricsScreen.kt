package com.example.ui.screens

import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.layout
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.viewmodel.LyricViewModel

@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
fun LyricsScreen(
    viewModel: LyricViewModel,
    onNavigateToMixer: () -> Unit
) {
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current

    // Observe State flows from LyricViewModel
    val currentLyrics by viewModel.currentLyrics.collectAsState()
    val currentTitle by viewModel.currentTitle.collectAsState()
    val currentVolume by viewModel.currentVolume.collectAsState()
    val currentArtist by viewModel.currentArtist.collectAsState()
    val isGenerating by viewModel.isGenerating.collectAsState()
    val selectedKit by viewModel.selectedKit.collectAsState()

    // Search and dynamic variables
    val youtubeSearchQuery by viewModel.youtubeSearchQuery.collectAsState()
    val youtubeResults by viewModel.youtubeSearchResults.collectAsState()
    val isYoutubeSearching by viewModel.isYoutubeSearching.collectAsState()
    val selectedYoutubeSong by viewModel.selectedYoutubeSong.collectAsState()
    val isYoutubePlayerActive by viewModel.isYoutubePlayerActive.collectAsState()
    val isYoutubePlaying by viewModel.isYoutubePlaying.collectAsState()

    val philosophicalLevel by viewModel.philosophicalLevel.collectAsState()
    val isWebSearchActive by viewModel.isWebSearchGroundingActive.collectAsState()
    val showRhymeScheme by viewModel.showRhymeScheme.collectAsState()
    val lyricsInputTheme by viewModel.lyricsInputTheme.collectAsState()
    val customArrangement by viewModel.customArrangement.collectAsState()

    val selectedGhostWriter by viewModel.selectedGhostWriter.collectAsState()
    val ghostWriters = viewModel.ghostWriters

    var activeTab by remember { mutableStateOf(0) } // 0 = AI Lyric Engraver, 1 = YouTube Lyrics Database
    var showResetDialog by remember { mutableStateOf(false) }
    var newKitName by remember { mutableStateOf("") }
    var newKitDesc by remember { mutableStateOf("") }

    // Vinyl spinning infinite transition
    val infiniteTransition = rememberInfiniteTransition(label = "VinylSpin")
    val spinningAngle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(4000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "SpinAngle"
    )

    Box(modifier = Modifier.fillMaxSize()) {
        // --- IMMERSIVE BACKGROUND GLOWS ---
        Box(
            modifier = Modifier
                .size(280.dp)
                .offset(x = (-80).dp, y = 80.dp)
                .background(
                    brush = Brush.radialGradient(
                        colors = listOf(Color(0xFFFF007A).copy(alpha = 0.18f), Color.Transparent)
                    )
                )
        )
        Box(
            modifier = Modifier
                .size(300.dp)
                .align(Alignment.BottomEnd)
                .offset(x = 100.dp, y = (-120).dp)
                .background(
                    brush = Brush.radialGradient(
                        colors = listOf(Color(0xFF7D00FF).copy(alpha = 0.15f), Color.Transparent)
                    )
                )
        )

        // --- MAIN SCROLL CONTAINER ---
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp)
        ) {
            // Header Info Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp, bottom = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(
                                brush = Brush.sweepGradient(
                                    colors = listOf(Color(0xFFFF007A), Color(0xFF7D00FF), Color(0xFFFF007A))
                                )
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "RD",
                            color = Color.White,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Column {
                        Text(
                            text = if (activeTab == 0) "RatchDiva Engine" else "YouTube Music Center",
                            color = Color.White,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "ACTIVE HIGH-FIDELITY DECK",
                            color = Color(0xFFFF007A),
                            fontSize = 8.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        )
                    }
                }

                // Create Vocal Kit Button
                IconButton(
                    onClick = { showResetDialog = true },
                    modifier = Modifier
                        .size(36.dp)
                        .background(Color.White.copy(alpha = 0.05f), CircleShape)
                ) {
                    Icon(
                        imageVector = Icons.Filled.Settings,
                        contentDescription = "Vocal Kit Settings",
                        tint = Color.White.copy(alpha = 0.8f),
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            // --- SECONDARY TAB SELECTOR ROW ---
            TabRow(
                selectedTabIndex = activeTab,
                containerColor = Color.Transparent,
                contentColor = Color(0xFFFF007A),
                indicator = { tabPositions ->
                    TabRowDefaults.SecondaryIndicator(
                        Modifier.tabIndicatorOffset(tabPositions[activeTab]),
                        color = Color(0xFFFF007A)
                    )
                },
                modifier = Modifier.padding(bottom = 12.dp)
            ) {
                Tab(
                    selected = activeTab == 0,
                    onClick = { activeTab = 0 },
                    text = {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Icon(imageVector = Icons.Filled.AutoAwesome, contentDescription = null, modifier = Modifier.size(16.dp))
                            Text("Lyric Engraver", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    },
                    modifier = Modifier.testTag("tab_engraver")
                )
                Tab(
                    selected = activeTab == 1,
                    onClick = { activeTab = 1 },
                    text = {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Icon(imageVector = Icons.Filled.Search, contentDescription = null, modifier = Modifier.size(16.dp))
                            Text("YouTube Search", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    },
                    modifier = Modifier.testTag("tab_youtube")
                )
            }

            // --- TAB CONTENT SWITCHER ---
            AnimatedContent(
                targetState = activeTab,
                transitionSpec = {
                    fadeIn(animationSpec = tween(220)) togetherWith fadeOut(animationSpec = tween(220))
                },
                label = "TabTransition",
                modifier = Modifier.weight(1f)
            ) { targetTab ->
                if (targetTab == 0) {
                    // TAB 0: LYRIC ENGINE & GENERATOR CONTROLS
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        // Theme Keywords textfield
                        OutlinedTextField(
                            value = lyricsInputTheme,
                            onValueChange = { viewModel.lyricsInputTheme.value = it },
                            placeholder = { Text("E.g., money schemes, late night cosmic drives...", color = Color.White.copy(alpha = 0.4f), fontSize = 13.sp) },
                            label = { Text("Theme or Vibe Keywords", color = Color(0xFFFF007A), fontSize = 12.sp, fontWeight = FontWeight.Bold) },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Color(0xFFFF007A),
                                unfocusedBorderColor = Color.White.copy(alpha = 0.15f),
                                focusedContainerColor = Color.White.copy(alpha = 0.02f),
                                unfocusedContainerColor = Color.White.copy(alpha = 0.02f),
                                focusedLabelColor = Color(0xFFFF007A),
                                unfocusedLabelColor = Color.White.copy(alpha = 0.5f),
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White
                            ),
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth().testTag("lyrics_theme_input")
                        )

                        // --- CREATIVE STYLE SEGREGATION (RATCHET DIVA VS PHILOSOPHICAL ABSTRACT) ---
                        Card(
                            colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.02f)),
                            border = borderStroke(Color.White.copy(alpha = 0.06f)),
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                Text(
                                    text = "SELECT GENERATOR FREQUENCY",
                                    color = Color.White.copy(alpha = 0.6f),
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 1.sp
                                )

                                // Row of three interactive segments with clean distinction & auto-summoning integrations
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    val currentMode = when {
                                        philosophicalLevel < 0.25f -> 0
                                        philosophicalLevel < 0.65f -> 1
                                        else -> 2
                                    }

                                    // Mode 0: Pure Ratchet Diva Style
                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .heightIn(min = 58.dp)
                                            .clip(RoundedCornerShape(12.dp))
                                            .background(if (currentMode == 0) Color(0xFFFF007A).copy(alpha = 0.15f) else Color.White.copy(alpha = 0.03f))
                                            .border(
                                                width = if (currentMode == 0) 1.5.dp else 1.dp,
                                                color = if (currentMode == 0) Color(0xFFFF007A) else Color.White.copy(alpha = 0.08f),
                                                shape = RoundedCornerShape(12.dp)
                                            )
                                            .clickable {
                                                viewModel.philosophicalLevel.value = 0.0f
                                                // Auto-summon RatchDiva profile
                                                val ratch = ghostWriters.find { it.id == "ratch_diva" }
                                                if (ratch != null) {
                                                    viewModel.selectedGhostWriter.value = ratch
                                                }
                                            }
                                            .padding(6.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                            Text("👸", fontSize = 18.sp)
                                            Spacer(modifier = Modifier.height(2.dp))
                                            Text("RATCHDIVA", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                            Text("Pure Street", color = Color.White.copy(alpha = 0.4f), fontSize = 7.sp)
                                        }
                                    }

                                    // Mode 1: Hybrid Blend Style
                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .heightIn(min = 58.dp)
                                            .clip(RoundedCornerShape(12.dp))
                                            .background(if (currentMode == 1) Color(0xFF7D00FF).copy(alpha = 0.15f) else Color.White.copy(alpha = 0.03f))
                                            .border(
                                                width = if (currentMode == 1) 1.5.dp else 1.dp,
                                                color = if (currentMode == 1) Color(0xFF7D00FF) else Color.White.copy(alpha = 0.08f),
                                                shape = RoundedCornerShape(12.dp)
                                            )
                                            .clickable {
                                                viewModel.philosophicalLevel.value = 0.5f
                                            }
                                            .padding(6.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                            Text("✨", fontSize = 18.sp)
                                            Spacer(modifier = Modifier.height(2.dp))
                                            Text("HYBRID BLEND", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold, maxLines = 1)
                                            Text("50/50 Soul", color = Color.White.copy(alpha = 0.4f), fontSize = 7.sp)
                                        }
                                    }

                                    // Mode 2: Philosophical Abstract Style
                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .heightIn(min = 58.dp)
                                            .clip(RoundedCornerShape(12.dp))
                                            .background(if (currentMode == 2) Color(0xFF00E5FF).copy(alpha = 0.15f) else Color.White.copy(alpha = 0.03f))
                                            .border(
                                                width = if (currentMode == 2) 1.5.dp else 1.dp,
                                                color = if (currentMode == 2) Color(0xFF00E5FF) else Color.White.copy(alpha = 0.08f),
                                                shape = RoundedCornerShape(12.dp)
                                            )
                                            .clickable {
                                                viewModel.philosophicalLevel.value = 1.0f
                                                // Auto-summon Socrates808
                                                val socrates = ghostWriters.find { it.id == "existential_alchemist" }
                                                if (socrates != null) {
                                                    viewModel.selectedGhostWriter.value = socrates
                                                }
                                            }
                                            .padding(6.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                            Text("🌌", fontSize = 18.sp)
                                            Spacer(modifier = Modifier.height(2.dp))
                                            Text("PHILOSOPHICAL", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                            Text("Deep Cosmic", color = Color.White.copy(alpha = 0.4f), fontSize = 7.sp)
                                        }
                                    }
                                }

                                // Interactive Quick Explanation based on active mode
                                val (statusTitle, statusDesc, statusColor) = when {
                                    philosophicalLevel < 0.25f -> Triple("Queen Baddie Mode Active", "Summoned RatchDiva. Writing 100% direct, high-energy Southern trap club lines with bold braggadocio and pure confidence.", Color(0xFFFF007A))
                                    philosophicalLevel < 0.65f -> Triple("Hybrid Soul Engaged", "Mashing high-energy street baddie with existential street philosophy, deep urban reflections, and raw double-entendres.", Color(0xFF7D00FF))
                                    else -> Triple("Abstract Oracle Engaged", "Summoned Socrates808. Writing deeply philosophical, cosmic and abstract questions with modern multi-layered poetry over heavy sub-bass gravity.", Color(0xFF00E5FF))
                                }

                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(statusColor.copy(alpha = 0.05f))
                                        .border(1.dp, statusColor.copy(alpha = 0.15f), RoundedCornerShape(10.dp))
                                        .padding(8.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Text(
                                        text = if (philosophicalLevel < 0.25f) "👑" else if (philosophicalLevel < 0.65f) "✨" else "🌌",
                                        fontSize = 16.sp
                                    )
                                    Column {
                                        Text(
                                            text = statusTitle.uppercase(),
                                            color = statusColor,
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold,
                                            letterSpacing = 0.5.sp
                                        )
                                        Text(
                                            text = statusDesc,
                                            color = Color.White.copy(alpha = 0.7f),
                                            fontSize = 9.sp,
                                            lineHeight = 11.sp
                                        )
                                    }
                                }

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Icon(imageVector = Icons.Filled.Language, contentDescription = "Web", tint = Color(0xFF00E5FF), modifier = Modifier.size(16.dp))
                                        Column {
                                            Text("Web Trend Grounding", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                            Text("Search live club slang and trends", color = Color.White.copy(alpha = 0.4f), fontSize = 8.sp)
                                        }
                                    }
                                    Switch(
                                        checked = isWebSearchActive,
                                        onCheckedChange = { viewModel.isWebSearchGroundingActive.value = it },
                                        colors = SwitchDefaults.colors(
                                            checkedThumbColor = Color(0xFFFF007A),
                                            checkedTrackColor = Color(0xFFFF007A).copy(alpha = 0.4f),
                                            uncheckedThumbColor = Color.White.copy(alpha = 0.4f),
                                            uncheckedTrackColor = Color.White.copy(alpha = 0.1f)
                                        ),
                                        modifier = Modifier.scale(0.8f).testTag("switch_web_search")
                                    )
                                }
                            }
                        }

                        // Arrangement presets customizer
                        Card(
                            colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.02f)),
                            border = borderStroke(Color.White.copy(alpha = 0.05f)),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                Text(
                                    text = "ARRANGEMENT BLOCKS STRUCTURE",
                                    color = Color.White.copy(alpha = 0.6f),
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 1.sp
                                )

                                LazyRow(
                                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    items(customArrangement) { section ->
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(8.dp))
                                                .background(Color(0xFF1F1A24))
                                                .border(1.dp, Color(0xFFFF007A).copy(alpha = 0.3f), RoundedCornerShape(8.dp))
                                                .padding(horizontal = 10.dp, vertical = 6.dp)
                                                .clickable {
                                                    // Remove section on click
                                                    if (customArrangement.size > 1) {
                                                        viewModel.customArrangement.value = customArrangement.filter { it != section }
                                                    }
                                                }
                                        ) {
                                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                                Text(text = section.uppercase(), color = Color.White, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                                Icon(imageVector = Icons.Filled.Close, contentDescription = "Remove", tint = Color.White.copy(alpha = 0.5f), modifier = Modifier.size(10.dp))
                                            }
                                        }
                                    }

                                    item {
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(8.dp))
                                                .background(Color.White.copy(alpha = 0.06f))
                                                .clickable {
                                                    val presets = listOf("Verse 1", "Verse 2", "Chorus", "Pre-Chorus", "Bridge", "Hook First", "Outro")
                                                    val nextSection = presets.random()
                                                    viewModel.customArrangement.value = customArrangement + nextSection
                                                }
                                                .padding(horizontal = 8.dp, vertical = 6.dp),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                                Icon(imageVector = Icons.Filled.Add, contentDescription = "Add", tint = Color(0xFFFF007A), modifier = Modifier.size(12.dp))
                                                Text("ADD BLOCK", color = Color(0xFFFF007A), fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        // Rhyme scheme & view configuration items row
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Rhyme scheme analyzer badge markers are active.",
                                color = Color.White.copy(alpha = 0.4f),
                                fontSize = 10.sp
                            )

                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                Text("Rhymes", color = Color.White.copy(alpha = 0.7f), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                Switch(
                                    checked = showRhymeScheme,
                                    onCheckedChange = { viewModel.showRhymeScheme.value = it },
                                    colors = SwitchDefaults.colors(
                                        checkedThumbColor = Color(0xFFFF007A),
                                        checkedTrackColor = Color(0xFFFF007A).copy(alpha = 0.4f)
                                    ),
                                    modifier = Modifier.scale(0.7f).testTag("switch_rhymes")
                                )
                            }
                        }

                        // Lyrics Canvas Board Viewport
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(24.dp))
                                .background(Color.White.copy(alpha = 0.02f))
                                .border(1.dp, Color.White.copy(alpha = 0.08f), RoundedCornerShape(24.dp))
                                .padding(16.dp)
                        ) {
                            if (isGenerating) {
                                Box(
                                    modifier = Modifier.fillMaxSize(),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                        CircularProgressIndicator(color = Color(0xFFFF007A))
                                        Text("Engraving Cosmic Stanzas...", color = Color.White.copy(alpha = 0.6f), fontSize = 12.sp, fontStyle = FontStyle.Italic)
                                    }
                                }
                            } else if (currentLyrics.isEmpty()) {
                                Box(
                                    modifier = Modifier.fillMaxSize(),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "No lyrics generated yet.\nInput theme above & tap GENERATE LIVE to begin.",
                                        color = Color.White.copy(alpha = 0.3f),
                                        fontSize = 12.sp,
                                        textAlign = TextAlign.Center,
                                        letterSpacing = 0.5.sp
                                    )
                                }
                            } else {
                                Column(modifier = Modifier.fillMaxSize()) {
                                    // Card Header
                                    Row(
                                        modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column {
                                            Text(
                                                text = currentTitle.uppercase(),
                                                color = Color.White,
                                                fontSize = 20.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                            Text(
                                                text = "Vocal profile: $currentArtist — Vol. $currentVolume",
                                                color = Color.White.copy(alpha = 0.4f),
                                                fontSize = 11.sp,
                                                fontFamily = FontFamily.Serif
                                            )
                                        }

                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(6.dp))
                                                .background(Color(0xFFFF007A).copy(alpha = 0.12f))
                                                .padding(horizontal = 8.dp, vertical = 2.dp)
                                        ) {
                                            Text(
                                                text = "GEMINI RETRIEVED",
                                                color = Color(0xFFFF007A),
                                                fontSize = 8.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    }

                                    val rhymeAnalysis = remember(currentLyrics) { viewModel.analyzeRhymeSchemes(currentLyrics) }

                                    val sections = remember(currentLyrics) {
                                        currentLyrics.split("\n\n")
                                            .filter { it.trim().isNotEmpty() }
                                            .map { block ->
                                                val lines = block.split("\n")
                                                val header = lines.firstOrNull() ?: ""
                                                val contentLines = lines.drop(1)
                                                header to contentLines
                                            }
                                    }

                                    LazyColumn(
                                        verticalArrangement = Arrangement.spacedBy(16.dp),
                                        modifier = Modifier.fillMaxSize()
                                    ) {
                                        items(sections) { (header, contentLines) ->
                                            val isHeaderBrackets = header.startsWith("[") && header.endsWith("]")
                                            val cleanHeader = if (isHeaderBrackets) header else "[$header]"

                                            val headerColor = when {
                                                header.contains("Chorus", ignoreCase = true) -> Color(0xFFFF007A)
                                                header.contains("Intro", ignoreCase = true) -> Color(0xFF7D00FF)
                                                header.contains("Bridge", ignoreCase = true) -> Color(0xFFFFB800)
                                                else -> Color.White.copy(alpha = 0.5f)
                                            }

                                            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                                Text(
                                                    text = cleanHeader.uppercase(),
                                                    color = headerColor,
                                                    fontSize = 10.sp,
                                                    fontFamily = FontFamily.Monospace,
                                                    fontWeight = FontWeight.Bold,
                                                    letterSpacing = 1.sp
                                                )

                                                contentLines.forEach { line ->
                                                    val rhymeTag = rhymeAnalysis[line.trim()] ?: ""
                                                    
                                                    Row(
                                                        verticalAlignment = Alignment.CenterVertically,
                                                        horizontalArrangement = Arrangement.SpaceBetween,
                                                        modifier = Modifier.fillMaxWidth()
                                                    ) {
                                                        Text(
                                                            text = line,
                                                            color = Color.White,
                                                            fontSize = 15.sp,
                                                            fontWeight = FontWeight.Medium,
                                                            lineHeight = 18.sp,
                                                            modifier = Modifier.weight(1f)
                                                        )

                                                        if (showRhymeScheme && rhymeTag.isNotEmpty()) {
                                                            Box(
                                                                modifier = Modifier
                                                                    .padding(start = 6.dp)
                                                                    .clip(RoundedCornerShape(4.dp))
                                                                    .background(
                                                                        when (rhymeTag) {
                                                                            "A" -> Color(0xFFFF007A).copy(alpha = 0.15f)
                                                                            "B" -> Color(0xFF7D00FF).copy(alpha = 0.15f)
                                                                            "C" -> Color(0xFFFFB800).copy(alpha = 0.15f)
                                                                            "D" -> Color(0xFF00E5FF).copy(alpha = 0.15f)
                                                                            else -> Color.White.copy(alpha = 0.08f)
                                                                        }
                                                                    )
                                                                    .border(
                                                                        0.5.dp,
                                                                        when (rhymeTag) {
                                                                            "A" -> Color(0xFFFF007A).copy(alpha = 0.4f)
                                                                            "B" -> Color(0xFF7D00FF).copy(alpha = 0.4f)
                                                                            "C" -> Color(0xFFFFB800).copy(alpha = 0.4f)
                                                                            "D" -> Color(0xFF00E5FF).copy(alpha = 0.4f)
                                                                            else -> Color.White.copy(alpha = 0.15f)
                                                                        },
                                                                        RoundedCornerShape(4.dp)
                                                                    )
                                                                    .padding(horizontal = 4.dp, vertical = 2.dp)
                                                            ) {
                                                                Text(
                                                                    text = rhymeTag,
                                                                    color = when (rhymeTag) {
                                                                        "A" -> Color(0xFFFF007A)
                                                                        "B" -> Color(0xFF7D00FF)
                                                                        "C" -> Color(0xFFFFB800)
                                                                        "D" -> Color(0xFF00E5FF)
                                                                        else -> Color.White
                                                                    },
                                                                    fontSize = 8.sp,
                                                                    fontWeight = FontWeight.Bold,
                                                                    fontFamily = FontFamily.Monospace
                                                                )
                                                            }
                                                        }
                                                    }
                                                }
                                            }
                                        }

                                        item {
                                            Spacer(modifier = Modifier.height(16.dp))
                                        }
                                    }
                                }
                            }
                        }

                        // Live Generate Controls Board
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Button(
                                onClick = {
                                    if (lyricsInputTheme.trim().isNotEmpty()) {
                                        viewModel.generateLiveGeminiLyrics(lyricsInputTheme)
                                    } else {
                                        Toast.makeText(context, "Please write a theme first!", Toast.LENGTH_SHORT).show()
                                    }
                                },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(52.dp)
                                    .testTag("button_generate_live"),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color.White,
                                    contentColor = Color.Black
                                ),
                                shape = RoundedCornerShape(14.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.AutoAwesome,
                                    contentDescription = "Glow",
                                    tint = Color.Black,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "GENERATE LIVE",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 1.sp
                                )
                            }

                            // Copy Board Button
                            IconButton(
                                onClick = {
                                    if (currentLyrics.isNotEmpty()) {
                                        clipboardManager.setText(AnnotatedString(currentLyrics))
                                        Toast.makeText(context, "Lyrics clipped!", Toast.LENGTH_SHORT).show()
                                    }
                                },
                                modifier = Modifier
                                    .size(52.dp)
                                    .background(Color(0xFF16151A), RoundedCornerShape(14.dp))
                                    .border(1.dp, Color.White.copy(alpha = 0.1f), RoundedCornerShape(14.dp))
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.ContentCopy,
                                    contentDescription = "Copy text",
                                    tint = Color.White,
                                    modifier = Modifier.size(18.dp)
                                )
                            }

                            // Save Collab track
                            IconButton(
                                onClick = {
                                    if (currentLyrics.isNotEmpty()) {
                                        viewModel.saveCurrentSong()
                                        Toast.makeText(context, "Archived to Collab History!", Toast.LENGTH_SHORT).show()
                                    }
                                },
                                modifier = Modifier
                                    .size(52.dp)
                                    .background(Color(0xFF16151A), RoundedCornerShape(14.dp))
                                    .border(1.dp, Color.White.copy(alpha = 0.1f), RoundedCornerShape(14.dp))
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Save,
                                    contentDescription = "Save track",
                                    tint = Color(0xFFFF007A),
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                } else {
                    // TAB 1: YOUTUBE SEARCH ENGINE OVER 50M+ SONGS WITH LYRICS
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        var searchInput by remember { mutableStateOf("") }

                        OutlinedTextField(
                            value = searchInput,
                            onValueChange = { searchInput = it },
                            placeholder = { Text("E.g., Sexyy Red SkeeYee or Kendrick Lamar Not Like Us", color = Color.White.copy(alpha = 0.4f), fontSize = 13.sp) },
                            label = { Text("Search 50m+ YouTube Music Track Index", color = Color(0xFFFF007A), fontSize = 12.sp) },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Color(0xFFFF007A),
                                unfocusedBorderColor = Color.White.copy(alpha = 0.15f),
                                focusedLabelColor = Color(0xFFFF007A),
                                unfocusedTextColor = Color.White,
                                focusedTextColor = Color.White
                            ),
                            singleLine = true,
                            trailingIcon = {
                                IconButton(onClick = { viewModel.performYouTubeSearch(searchInput) }) {
                                    Icon(imageVector = Icons.Filled.Search, contentDescription = "Search", tint = Color(0xFFFF007A))
                                }
                            },
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier.fillMaxWidth().testTag("youtube_search_input")
                        )

                        if (isYoutubeSearching) {
                            Box(modifier = Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                    CircularProgressIndicator(color = Color(0xFFFF007A))
                                    Text("Navigating YouTube metadata server pipelines...", color = Color.White.copy(alpha = 0.6f), fontSize = 12.sp)
                                }
                            }
                        } else if (selectedYoutubeSong == null) {
                            Box(modifier = Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Icon(imageVector = Icons.Filled.Language, contentDescription = null, tint = Color.White.copy(alpha = 0.2f), modifier = Modifier.size(48.dp))
                                    Text(
                                        text = "Search exact artists & songs.\nPowered by Google Search Grounding to reach 50m+ tracks.",
                                        color = Color.White.copy(alpha = 0.4f),
                                        fontSize = 12.sp,
                                        textAlign = TextAlign.Center
                                    )
                                }
                            }
                        } else {
                            val song = selectedYoutubeSong!!

                            // Simulated YouTube Player with dynamic rotating disc
                            Card(
                                colors = CardDefaults.cardColors(containerColor = Color(0xFF131116)),
                                border = borderStroke(Color(0xFFFF007A).copy(alpha = 0.25f)),
                                shape = RoundedCornerShape(20.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    // Spinning Vinyl Disc
                                    Box(
                                        modifier = Modifier
                                            .size(64.dp)
                                            .clip(CircleShape)
                                            .background(Color.Black)
                                            .border(1.5.dp, Color(0xFFFF007A), CircleShape)
                                            .rotate(if (isYoutubePlaying) spinningAngle else 0f),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(24.dp)
                                                .clip(CircleShape)
                                                .background(Color(0xFF2E2B35))
                                        ) {
                                            Icon(
                                                imageVector = Icons.Filled.MusicNote,
                                                contentDescription = null,
                                                tint = Color(0xFFFF007A),
                                                modifier = Modifier.size(12.dp).align(Alignment.Center)
                                            )
                                        }
                                    }

                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = song.title,
                                            color = Color.White,
                                            fontSize = 15.sp,
                                            fontWeight = FontWeight.Bold,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                        Text(
                                            text = song.artist,
                                            color = Color(0xFFFF007A),
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Medium,
                                            maxLines = 1
                                        )
                                        Text(
                                            text = "${song.views} • Length ${song.duration} • Released ${song.releaseYear}",
                                            color = Color.White.copy(alpha = 0.5f),
                                            fontSize = 9.sp
                                        )
                                    }

                                    // Play / pause simulation button
                                    IconButton(
                                        onClick = { viewModel.isYoutubePlaying.value = !isYoutubePlaying },
                                        modifier = Modifier
                                            .size(44.dp)
                                            .background(Color(0xFFFF007A), CircleShape)
                                    ) {
                                        Icon(
                                            imageVector = if (isYoutubePlaying) Icons.Filled.Pause else Icons.Filled.PlayArrow,
                                            contentDescription = "Toggle play simulation",
                                            tint = Color.White,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                }
                            }

                            // Dynamic Equalizer visual wave representation
                            if (isYoutubePlaying) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(16.dp)
                                        .padding(horizontal = 8.dp),
                                    horizontalArrangement = Arrangement.SpaceEvenly,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    val listHeights = listOf(12, 16, 8, 14, 5, 15, 9, 13, 6, 12, 16, 7, 14)
                                    listHeights.forEach { h ->
                                        Box(
                                            modifier = Modifier
                                                .width(2.5.dp)
                                                .height(h.dp)
                                                .clip(RoundedCornerShape(1.dp))
                                                .background(Color(0xFF00E5FF))
                                        )
                                    }
                                }
                            }

                            // Retrieved lyrics board
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(20.dp))
                                    .background(Color.White.copy(alpha = 0.02f))
                                    .border(1.dp, Color.White.copy(alpha = 0.06f), RoundedCornerShape(20.dp))
                                    .padding(14.dp)
                            ) {
                                val parseSections = remember(song.lyrics) {
                                    song.lyrics.split("\n\n")
                                        .filter { it.trim().isNotEmpty() }
                                        .map { block ->
                                            val lines = block.split("\n")
                                            val header = lines.firstOrNull() ?: ""
                                            val content = lines.drop(1)
                                            header to content
                                        }
                                }

                                LazyColumn(
                                    modifier = Modifier.fillMaxSize(),
                                    verticalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    items(parseSections) { (head, lines) ->
                                        Column {
                                            Text(
                                                text = head.uppercase(),
                                                color = Color(0xFFFF007A),
                                                fontSize = 9.sp,
                                                fontWeight = FontWeight.Bold,
                                                fontFamily = FontFamily.Monospace
                                            )
                                            lines.forEach { line ->
                                                Text(
                                                    text = line,
                                                    color = Color.White,
                                                    fontSize = 14.sp,
                                                    lineHeight = 17.sp,
                                                    modifier = Modifier.padding(vertical = 1.dp)
                                                )
                                            }
                                        }
                                    }
                                }
                            }

                            // Port lyrics action bar
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Button(
                                    onClick = {
                                        viewModel.currentLyrics.value = song.lyrics
                                        viewModel.currentTitle.value = song.title
                                        viewModel.currentArtist.value = "${song.artist} (Retrieved)"
                                        viewModel.currentVolume.value = 1
                                        activeTab = 0
                                        Toast.makeText(context, "Lyrics ported to workspace deck successfully!", Toast.LENGTH_SHORT).show()
                                    },
                                    modifier = Modifier.weight(1f).height(50.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF007A), contentColor = Color.White),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Icon(imageVector = Icons.Filled.ArrowDownward, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("PORT TO COMPOSER", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }

                                Button(
                                    onClick = {
                                        viewModel.lyricsInputTheme.value = "Create an abstract, heavy remix in the style of Socrates808 of the track: " + song.title
                                        activeTab = 0
                                        Toast.makeText(context, "Imported as prompt template! Taps GENERATE LIVE to execute.", Toast.LENGTH_LONG).show()
                                    },
                                    modifier = Modifier.weight(1f).height(50.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = Color.White.copy(alpha = 0.08f), contentColor = Color.White),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Icon(imageVector = Icons.Filled.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("REMIX WITH GHOST", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // --- DIALOGS FOR CREATING CUSTOM PRESET VOCAL KITS ---
    if (showResetDialog) {
        AlertDialog(
            onDismissRequest = { showResetDialog = false },
            title = {
                Text(
                    text = "Configure Vocal Kit Preset",
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        text = "Customize vocabulary weighting parameters or design offline dictionaries inside the MIXER tab afterwards.",
                        color = Color.White.copy(alpha = 0.7f),
                        fontSize = 12.sp
                    )
                    OutlinedTextField(
                        value = newKitName,
                        onValueChange = { newKitName = it },
                        label = { Text("Profile/Vocalist Name") },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFFFF007A),
                            unfocusedBorderColor = Color.White.copy(alpha = 0.2f),
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        ),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = newKitDesc,
                        onValueChange = { newKitDesc = it },
                        label = { Text("Short Pitch/Vibe") },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFFFF007A),
                            unfocusedBorderColor = Color.White.copy(alpha = 0.2f),
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        ),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        if (newKitName.trim().isNotEmpty()) {
                            viewModel.createCustomKit(newKitName, newKitDesc)
                            showResetDialog = false
                            newKitName = ""
                            newKitDesc = ""
                            Toast.makeText(context, "Vocal Kit created! Customize parameters inside the MIXER.", Toast.LENGTH_LONG).show()
                            onNavigateToMixer()
                        }
                    }
                ) {
                    Text("CREATE", color = Color(0xFFFF007A), fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showResetDialog = false }) {
                    Text("CANCEL", color = Color.White.copy(alpha = 0.5f))
                }
            },
            containerColor = Color(0xFF141318),
            shape = RoundedCornerShape(20.dp)
        )
    }
}

private fun borderStroke(c: Color) = BorderStroke(1.dp, c)
