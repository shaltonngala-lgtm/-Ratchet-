package com.example.ui.screens

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.clickable
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.platform.testTag
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.BuildConfig
import com.example.viewmodel.LyricViewModel

@Composable
fun GhostScreen(viewModel: LyricViewModel) {
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current

    val ghostPrompt by viewModel.ghostPrompt.collectAsState()
    val ghostwrittenLyrics by viewModel.ghostwrittenLyrics.collectAsState()
    val isGhostGenerating by viewModel.isGhostGenerating.collectAsState()
    val ghostErrorMessage by viewModel.ghostErrorMessage.collectAsState()
    val selectedWriter by viewModel.selectedGhostWriter.collectAsState()
    val ghostwriterThoughts by viewModel.ghostwriterThoughts.collectAsState()
    val ghostThinkingPhase by viewModel.ghostThinkingPhase.collectAsState()
    
    // Modern Filter for Ratchet vs Philosophical selection (helpful for mobile users without laptop)
    var activeFilter by remember { mutableStateOf("ALL") } // "ALL", "RATCHET", "PHILOSOPHICAL"
    val filteredWriters = remember(activeFilter, viewModel.ghostWriters) {
        when (activeFilter) {
            "RATCHET" -> viewModel.ghostWriters.filter { 
                it.id in listOf("ratch_diva", "drill_dutchess", "la_jefe", "neon_pixie", "westcoast_dolly", "grime_baroness", "gyalis_queen", "k_vixen")
            }
            "PHILOSOPHICAL" -> viewModel.ghostWriters.filter {
                it.id in listOf("existential_alchemist", "sovereign_pen", "melody_noir", "sabi_queen", "poetess_sol")
            }
            else -> viewModel.ghostWriters
        }
    }

    // Check if real API Key is configured
    val isApiKeyConfigured = remember {
        BuildConfig.GEMINI_API_KEY.isNotEmpty() && BuildConfig.GEMINI_API_KEY != "MY_GEMINI_API_KEY"
    }

    Box(modifier = Modifier.fillMaxSize()) {
        // --- IMMERSIVE BACKGROUND GLOWS ---
        Box(
            modifier = Modifier
                .size(280.dp)
                .align(Alignment.BottomStart)
                .offset(x = (-80).dp, y = 50.dp)
                .background(
                    brush = Brush.radialGradient(
                        colors = listOf(Color(0xFFFF007A).copy(alpha = 0.15f), Color.Transparent)
                    )
                )
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp)
        ) {
            // Header
            Column(modifier = Modifier.padding(top = 16.dp, bottom = 8.dp)) {
                Text(
                    text = "AI GHOSTWRITER MATRIX",
                    color = Color(0xFFFF007A),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 2.sp
                )
                Text(
                    text = "Select Pen Game",
                    color = Color.White,
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Access 10 distinct ghostwriters spanning 9 global genres, each with unique rhyming structures and signature wordplay.",
                    color = Color.White.copy(alpha = 0.5f),
                    fontSize = 11.sp,
                    modifier = Modifier.padding(top = 1.dp)
                )
            }

            // API key instruction disclaimer if key is missing
            if (!isApiKeyConfigured) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFF2C191D))
                        .border(1.dp, Color(0xFFFF007A).copy(alpha = 0.2f), RoundedCornerShape(12.dp))
                        .padding(8.dp)
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = "Info",
                            tint = Color(0xFFFF007A),
                            modifier = Modifier.size(16.dp)
                        )
                        Column {
                            Text(
                                text = "Running in Simulation Mode",
                                color = Color.White,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Add GEMINI_API_KEY in the Secrets panel inside AI Studio to activate real-time AI generation.",
                                color = Color.White.copy(alpha = 0.7f),
                                fontSize = 9.sp,
                                lineHeight = 11.sp
                            )
                        }
                    }
                }
            }

            // Modern Ghostwriters Selection Carousel & Philosophy/Ratchet Filter Tabs
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "CHOOSE GHOSTWRITER",
                    color = Color.White.copy(alpha = 0.4f),
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
                
                // Beautifully designed segmented chips for fast toggle
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    listOf("ALL" to "🌐 All", "RATCHET" to "👸 Ratchet", "PHILOSOPHICAL" to "🌌 Philos").forEach { (filterVal, label) ->
                        val isSelected = activeFilter == filterVal
                        val activeColor = if (filterVal == "RATCHET") Color(0xFFFF007A) else if (filterVal == "PHILOSOPHICAL") Color(0xFF00E5FF) else Color(0xFF7D00FF)
                        
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isSelected) activeColor.copy(alpha = 0.15f) else Color.White.copy(alpha = 0.03f))
                                .border(1.dp, if (isSelected) activeColor else Color.White.copy(alpha = 0.08f), RoundedCornerShape(8.dp))
                                .clickable { activeFilter = filterVal }
                                .padding(horizontal = 6.dp, vertical = 3.dp)
                        ) {
                            Text(
                                text = label,
                                color = if (isSelected) activeColor else Color.White.copy(alpha = 0.6f),
                                fontSize = 8.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(filteredWriters) { writer ->
                    val isSelected = writer.id == selectedWriter.id
                    val borderAlpha = if (isSelected) 0.8f else 0.1f
                    val bgAlpha = if (isSelected) 0.15f else 0.03f
                    val textColor = if (isSelected) Color(0xFFFF007A) else Color.White

                    Card(
                        modifier = Modifier
                            .width(135.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .border(
                                width = if (isSelected) 1.5.dp else 1.dp,
                                color = if (isSelected) Color(0xFFFF007A) else Color.White.copy(alpha = borderAlpha),
                                shape = RoundedCornerShape(14.dp)
                            )
                            .clickable {
                                viewModel.selectedGhostWriter.value = writer
                            }
                            .testTag("writer_card_${writer.id}"),
                        colors = CardDefaults.cardColors(
                            containerColor = if (isSelected) Color(0xFFFF007A).copy(alpha = 0.15f) else Color.White.copy(alpha = bgAlpha)
                        )
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(10.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Text(text = writer.emoji, fontSize = 24.sp)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = writer.name,
                                color = Color.White,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1
                            )
                            Text(
                                text = writer.genre,
                                color = textColor,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Medium,
                                maxLines = 1
                            )
                        }
                    }
                }
            }

            // Detailed Writer Portrait Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.02f)),
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.05f))
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                Text(text = selectedWriter.emoji, fontSize = 16.sp)
                                Text(
                                    text = "${selectedWriter.name} — ${selectedWriter.title}",
                                    color = Color(0xFFFF007A),
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Text(
                                text = "Inspired by ${selectedWriter.artistInspiration}",
                                color = Color.White.copy(alpha = 0.6f),
                                fontSize = 10.sp,
                                fontStyle = FontStyle.Italic
                            )
                        }
                        
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(Color(0xFFFF007A).copy(alpha = 0.1f))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = selectedWriter.genre.uppercase(),
                                color = Color(0xFFFF007A),
                                fontSize = 8.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = selectedWriter.description,
                        color = Color.White.copy(alpha = 0.7f),
                        fontSize = 11.sp,
                        lineHeight = 14.sp
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Writing style metrics
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Wordplay
                        Column(modifier = Modifier.weight(1f)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Pen Game", color = Color.White.copy(alpha = 0.5f), fontSize = 8.sp, fontWeight = FontWeight.Bold)
                                Text("${(selectedWriter.wordplayRating * 20).toInt()}%", color = Color.White, fontSize = 8.sp)
                            }
                            Spacer(modifier = Modifier.height(2.dp))
                            LinearProgressIndicator(
                                progress = { selectedWriter.wordplayRating / 5.0f },
                                color = Color(0xFFFF007A),
                                trackColor = Color.White.copy(alpha = 0.05f),
                                strokeCap = StrokeCap.Round,
                                modifier = Modifier.fillMaxWidth().height(3.dp)
                            )
                        }

                        // Metaphor
                        Column(modifier = Modifier.weight(1f)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Wordplay", color = Color.White.copy(alpha = 0.5f), fontSize = 8.sp, fontWeight = FontWeight.Bold)
                                Text("${(selectedWriter.metaphorRating * 20).toInt()}%", color = Color.White, fontSize = 8.sp)
                            }
                            Spacer(modifier = Modifier.height(2.dp))
                            LinearProgressIndicator(
                                progress = { selectedWriter.metaphorRating / 5.0f },
                                color = Color(0xFFFF007A),
                                trackColor = Color.White.copy(alpha = 0.05f),
                                strokeCap = StrokeCap.Round,
                                modifier = Modifier.fillMaxWidth().height(3.dp)
                            )
                        }

                        // Rhyming
                        Column(modifier = Modifier.weight(1f)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Rhythm Meter", color = Color.White.copy(alpha = 0.5f), fontSize = 8.sp, fontWeight = FontWeight.Bold)
                                Text("${(selectedWriter.rhymeRating * 20).toInt()}%", color = Color.White, fontSize = 8.sp)
                            }
                            Spacer(modifier = Modifier.height(2.dp))
                            LinearProgressIndicator(
                                progress = { selectedWriter.rhymeRating / 5.0f },
                                color = Color(0xFFFF007A),
                                trackColor = Color.White.copy(alpha = 0.05f),
                                strokeCap = StrokeCap.Round,
                                modifier = Modifier.fillMaxWidth().height(3.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                    
                    // Style parameters
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .weight(1.5f)
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color.White.copy(alpha = 0.05f))
                                .border(1.dp, Color.White.copy(alpha = 0.08f), RoundedCornerShape(8.dp))
                                .padding(6.dp)
                        ) {
                            Column {
                                Text("CADENCE / DELIVERY", color = Color.White.copy(alpha = 0.4f), fontSize = 7.sp, fontWeight = FontWeight.Bold)
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(selectedWriter.deliveryStyle, color = Color.White, fontSize = 9.sp, fontWeight = FontWeight.Medium, maxLines = 1)
                            }
                        }
                        Box(
                            modifier = Modifier
                                .weight(1.5f)
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color.White.copy(alpha = 0.05f))
                                .border(1.dp, Color.White.copy(alpha = 0.08f), RoundedCornerShape(8.dp))
                                .padding(6.dp)
                        ) {
                            Column {
                                Text("RHYME SCHEME PREF", color = Color.White.copy(alpha = 0.4f), fontSize = 7.sp, fontWeight = FontWeight.Bold)
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(selectedWriter.rhymeSchemePreference, color = Color.White, fontSize = 9.sp, fontWeight = FontWeight.Medium, maxLines = 1)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Vocabulary weightings
                    Text(
                        "VOCABULARY WEIGHTINGS",
                        color = Color.White.copy(alpha = 0.4f),
                        fontSize = 8.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.5.sp,
                        modifier = Modifier.padding(bottom = 4.dp)
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        selectedWriter.vocabularyWeightings.forEach { (vocabKey, rating) ->
                            Column(modifier = Modifier.weight(1f)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(vocabKey, color = Color.White.copy(alpha = 0.7f), fontSize = 8.sp, maxLines = 1)
                                    Text("$rating%", color = Color(0xFFFF007A), fontSize = 8.sp, fontWeight = FontWeight.Bold)
                                }
                                Spacer(modifier = Modifier.height(2.dp))
                                LinearProgressIndicator(
                                    progress = { rating / 100f },
                                    color = Color(0xFFFF007A).copy(alpha = 0.7f),
                                    trackColor = Color.White.copy(alpha = 0.05f),
                                    strokeCap = StrokeCap.Round,
                                    modifier = Modifier.fillMaxWidth().height(2.dp)
                                )
                            }
                        }
                    }
                }
            }

            // Input prompt card
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.03f)),
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.08f))
            ) {
                Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "What theme should ${selectedWriter.name} write about?",
                        color = Color.White.copy(alpha = 0.5f),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.5.sp
                    )
                    OutlinedTextField(
                        value = ghostPrompt,
                        onValueChange = { viewModel.ghostPrompt.value = it },
                        placeholder = { Text("E.g., high stakes, icy status, fast highway flexes...", color = Color.White.copy(alpha = 0.3f)) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFFFF007A),
                            unfocusedBorderColor = Color.White.copy(alpha = 0.1f),
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("ghost_prompt_input"),
                        maxLines = 3
                    )

                    Button(
                        onClick = { viewModel.generateGhostwriterLyrics(ghostPrompt) },
                        enabled = !isGhostGenerating,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFFFF007A),
                            contentColor = Color.White,
                            disabledContainerColor = Color(0xFFFF007A).copy(alpha = 0.4f)
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(44.dp)
                            .testTag("ghost_generate_button")
                    ) {
                        if (isGhostGenerating) {
                            CircularProgressIndicator(color = Color.White, modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
                        } else {
                            Icon(Icons.Default.AutoAwesome, contentDescription = "AI", modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("GENERATE GHOSTWRITE LYRICS", fontWeight = FontWeight.Bold, fontSize = 11.sp)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // AI results viewport card
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(24.dp))
                    .background(Color.White.copy(alpha = 0.02f))
                    .border(1.dp, Color.White.copy(alpha = 0.08f), RoundedCornerShape(24.dp))
                    .padding(20.dp)
            ) {
                if (ghostwrittenLyrics.isEmpty() && !isGhostGenerating) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Box(
                                modifier = Modifier
                                    .size(48.dp)
                                    .background(Color.White.copy(alpha = 0.04f), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.AutoAwesome, contentDescription = "AI icon", tint = Color.White.copy(alpha = 0.3f))
                            }
                            Text(
                                text = "Awaiting AI commands...",
                                color = Color.White.copy(alpha = 0.3f),
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                } else if (isGhostGenerating) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            Text(
                                text = "🧠 CREATIVE BRAIN ENGINE ACTIVE",
                                color = Color(0xFFFF007A),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.sp
                            )
                            
                            Text(
                                text = "Tuning into the raw frequencies of ${selectedWriter.name}'s soul...",
                                color = Color.White.copy(alpha = 0.5f),
                                fontSize = 10.sp,
                                fontStyle = FontStyle.Italic,
                                modifier = Modifier.padding(bottom = 4.dp)
                            )
                            
                            // Visual horizontal progression line
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(6.dp)
                                    .clip(RoundedCornerShape(3.dp))
                                    .background(Color.White.copy(alpha = 0.05f))
                            ) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxHeight()
                                        .fillMaxWidth(fraction = if (ghostThinkingPhase > 0) ghostThinkingPhase / 5.0f else 0.1f)
                                        .background(Brush.horizontalGradient(listOf(Color(0xFFFF007A), Color(0xFFFFAA00))))
                                )
                            }
                            
                            Spacer(modifier = Modifier.height(4.dp))

                            // 5 Artistic Thinking Phases list
                            val thinkingPhases = listOf(
                                Triple("PULSE SYNC", "Calibrating baseline rhythm, 808 velocity & beat pockets...", "🎧"),
                                Triple("SOUL DRIFT", "Recalling memories, regional struggles, the raw truth...", "💭"),
                                Triple("SILENT PEN WAR", "Scrubbing clinical robotic words, writing street-smart rhymes...", "✍️"),
                                Triple("CADENCE GRIP", "Translating words into ${selectedWriter.name}'s signature flow...", "🎙️"),
                                Triple("INK FUSE", "Aligning emotional vulnerability and raw performance attitude...", "🔥")
                            )

                            Column(
                                modifier = Modifier.fillMaxWidth(),
                                verticalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                thinkingPhases.forEachIndexed { index, (title, description, icon) ->
                                    val phaseIndex = index + 1
                                    val isActive = ghostThinkingPhase == phaseIndex
                                    val isCompleted = ghostThinkingPhase > phaseIndex

                                    val cardBgColor = when {
                                        isActive -> Color(0xFFFF007A).copy(alpha = 0.08f)
                                        isCompleted -> Color.White.copy(alpha = 0.02f)
                                        else -> Color.Transparent
                                    }

                                    val tc = when {
                                        isActive -> Color.White
                                        isCompleted -> Color.White.copy(alpha = 0.6f)
                                        else -> Color.White.copy(alpha = 0.25f)
                                    }

                                    val subTc = when {
                                        isActive -> Color.White.copy(alpha = 0.7f)
                                        isCompleted -> Color.White.copy(alpha = 0.4f)
                                        else -> Color.White.copy(alpha = 0.15f)
                                    }

                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clip(RoundedCornerShape(12.dp))
                                            .background(cardBgColor)
                                            .border(
                                                width = 1.dp,
                                                color = if (isActive) Color(0xFFFF007A).copy(alpha = 0.3f) else Color.Transparent,
                                                shape = RoundedCornerShape(12.dp)
                                            )
                                            .padding(10.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                                    ) {
                                        // Left Indicator
                                        Box(
                                            modifier = Modifier.size(34.dp),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            when {
                                                isCompleted -> {
                                                    Text("✅", fontSize = 16.sp)
                                                }
                                                isActive -> {
                                                    CircularProgressIndicator(
                                                        color = Color(0xFFFF007A),
                                                        modifier = Modifier.size(20.dp),
                                                        strokeWidth = 2.dp
                                                    )
                                                }
                                                else -> {
                                                    Text(icon, fontSize = 20.sp)
                                                }
                                            }
                                        }

                                        // Text Details
                                        Column {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                                            ) {
                                                Text(
                                                    text = title,
                                                    color = if (isActive) Color(0xFFFF007A) else tc,
                                                    fontSize = 10.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    letterSpacing = 0.5.sp
                                                )
                                                if (isActive) {
                                                    Text(
                                                        text = "• ACTIVE",
                                                        color = Color(0xFFFFAA00),
                                                        fontSize = 8.sp,
                                                        fontWeight = FontWeight.Bold
                                                    )
                                                }
                                            }
                                            Spacer(modifier = Modifier.height(2.dp))
                                            Text(
                                                text = description,
                                                color = subTc,
                                                fontSize = 9.sp,
                                                lineHeight = 11.sp
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                } else {
                    Column(modifier = Modifier.fillMaxSize()) {
                        // Action menu headers on top of card
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "LYRIC CELL OUTPUT",
                                color = Color(0xFFFF007A),
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.sp
                            )

                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                // Copy
                                IconButton(
                                    onClick = {
                                        clipboardManager.setText(AnnotatedString(ghostwrittenLyrics))
                                        Toast.makeText(context, "Lyrics copied!", Toast.LENGTH_SHORT).show()
                                    },
                                    modifier = Modifier
                                        .size(34.dp)
                                        .background(Color.White.copy(alpha = 0.05f), CircleShape)
                                ) {
                                    Icon(Icons.Default.ContentCopy, contentDescription = "Copy", tint = Color.White, modifier = Modifier.size(15.dp))
                                }

                                // Save to histories
                                IconButton(
                                    onClick = {
                                        viewModel.saveGhostwrite(ghostPrompt)
                                        Toast.makeText(context, "Collab archive saved!", Toast.LENGTH_SHORT).show()
                                    },
                                    modifier = Modifier
                                        .size(34.dp)
                                        .background(Color.White.copy(alpha = 0.05f), CircleShape)
                                ) {
                                    Icon(Icons.Default.Save, contentDescription = "Save", tint = Color(0xFFFF007A), modifier = Modifier.size(15.dp))
                                }
                            }
                        }

                        // Display lyric lines nicely formatted
                        LazyColumn(modifier = Modifier.fillMaxSize()) {
                            if (ghostwriterThoughts.isNotEmpty()) {
                                item {
                                    Card(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(bottom = 16.dp),
                                        colors = CardDefaults.cardColors(containerColor = Color(0xFFFF007A).copy(alpha = 0.05f)),
                                        shape = RoundedCornerShape(14.dp),
                                        border = BorderStroke(1.dp, Color(0xFFFF007A).copy(alpha = 0.15f))
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(12.dp),
                                            verticalAlignment = Alignment.Top,
                                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                                        ) {
                                            Text("🧠", fontSize = 20.sp)
                                            Column {
                                                Text(
                                                    text = "GHOSTWRITER'S SOUL REFLECTION",
                                                    color = Color(0xFFFF007A),
                                                    fontSize = 8.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    letterSpacing = 1.sp
                                                )
                                                Spacer(modifier = Modifier.height(4.dp))
                                                Text(
                                                    text = ghostwriterThoughts,
                                                    color = Color.White.copy(alpha = 0.85f),
                                                    fontSize = 11.sp,
                                                    lineHeight = 14.sp,
                                                    fontStyle = FontStyle.Italic
                                                )
                                            }
                                        }
                                    }
                                }
                            }

                            val lines = ghostwrittenLyrics.split("\n")
                            items(lines) { line ->
                                val isHeader = line.startsWith("[") && line.endsWith("]")
                                val color = if (isHeader) Color(0xFFFF007A) else Color.White
                                val fontStyle = if (isHeader) FontStyle.Normal else FontStyle.Italic
                                val weight = if (isHeader) FontWeight.Bold else FontWeight.SemiBold
                                val fontSize = if (isHeader) 12.sp else 16.sp
                                
                                Text(
                                    text = line,
                                    color = color,
                                    fontSize = fontSize,
                                    fontStyle = fontStyle,
                                    fontWeight = weight,
                                    fontFamily = if (isHeader) FontFamily.Monospace else FontFamily.Default,
                                    modifier = Modifier.padding(vertical = if (isHeader) 8.dp else 2.dp)
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))
        }
    }
}
