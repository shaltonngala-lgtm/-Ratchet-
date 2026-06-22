package com.example.ui.screens

import android.widget.Toast
import androidx.compose.animation.*
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
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.viewmodel.LyricViewModel

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun MixerScreen(viewModel: LyricViewModel) {
    val context = LocalContext.current
    val kits by viewModel.kits.collectAsState()
    val selectedKit by viewModel.selectedKit.collectAsState()

    var activeKitId by remember { mutableStateOf("") }
    
    // Controlled editing states
    var kitName by remember { mutableStateOf("") }
    var kitDesc by remember { mutableStateOf("") }
    var addresses by remember { mutableStateOf("") }
    var selfWords by remember { mutableStateOf("") }
    var actions by remember { mutableStateOf("") }
    var adjectives by remember { mutableStateOf("") }
    var bodyParts by remember { mutableStateOf("") }
    var vibeWords by remember { mutableStateOf("") }
    var hooks by remember { mutableStateOf("") }
    var adlibs by remember { mutableStateOf("") }

    // local search state for dictionary terms
    var localSearchQuery by remember { mutableStateOf("") }

    // Slang Web Crawler & style Optimizer states
    val slangSearchQuery by viewModel.slangSearchQuery.collectAsState()
    val isSlangCrawling by viewModel.isSlangCrawling.collectAsState()
    val slangCrawlStatus by viewModel.slangCrawlStatus.collectAsState()
    val slangCrawlError by viewModel.slangCrawlError.collectAsState()

    val crawledAddresses by viewModel.crawledAddresses.collectAsState()
    val crawledSelfWords by viewModel.crawledSelfWords.collectAsState()
    val crawledActions by viewModel.crawledActions.collectAsState()
    val crawledAdjectives by viewModel.crawledAdjectives.collectAsState()
    val crawledBodyParts by viewModel.crawledBodyParts.collectAsState()
    val crawledVibeWords by viewModel.crawledVibeWords.collectAsState()
    val crawledHooks by viewModel.crawledHooks.collectAsState()
    val crawledAdlibs by viewModel.crawledAdlibs.collectAsState()

    // Synchronize form values whenever the selected kit changes
    LaunchedEffect(selectedKit) {
        selectedKit?.let { kit ->
            activeKitId = kit.id
            kitName = kit.name
            kitDesc = kit.description
            addresses = kit.addresses
            selfWords = kit.selfWords
            actions = kit.actions
            adjectives = kit.adjectives
            bodyParts = kit.bodyParts
            vibeWords = kit.vibeWords
            hooks = kit.hooks
            adlibs = kit.adlibs
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        // --- IMMERSIVE BACKGROUND GLOWS ---
        Box(
            modifier = Modifier
                .size(320.dp)
                .offset(x = 120.dp, y = (-50).dp)
                .background(
                    brush = Brush.radialGradient(
                        colors = listOf(Color(0xFF7D00FF).copy(alpha = 0.12f), Color.Transparent)
                    )
                )
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp)
        ) {
            // Header Title
            Column(modifier = Modifier.padding(top = 16.dp, bottom = 12.dp)) {
                Text(
                    text = "VOCAB MIXER & SEARCH OPTIMIZER",
                    color = Color(0xFFFF007A),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 2.sp
                )
                Text(
                    text = "Sound Profiles",
                    color = Color.White,
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Select, customize, search or run the AI slang crawler to index modern language datasets directly into your session.",
                    color = Color.White.copy(alpha = 0.5f),
                    fontSize = 12.sp,
                    lineHeight = 16.sp,
                    modifier = Modifier.padding(top = 2.dp)
                )
            }

            // LazyRow of Sound Kits
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp)
            ) {
                items(kits) { kit ->
                    val isSelected = kit.id == activeKitId
                    val borderColor = if (isSelected) Color(0xFFFF007A) else Color.White.copy(alpha = 0.1f)
                    val bgColor = if (isSelected) Color(0xFFFF007A).copy(alpha = 0.1f) else Color.White.copy(alpha = 0.02f)

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(16.dp))
                            .background(bgColor)
                            .border(1.dp, borderColor, RoundedCornerShape(16.dp))
                            .clickable {
                                viewModel.selectKit(kit.id)
                            }
                            .padding(horizontal = 16.dp, vertical = 10.dp)
                    ) {
                        Column {
                            Text(
                                text = kit.name,
                                color = if (isSelected) Color.White else Color.White.copy(alpha = 0.7f),
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = if (kit.id.startsWith("custom")) "Custom profile" else "Seeded preset",
                                color = if (isSelected) Color(0xFFFF007A) else Color.White.copy(alpha = 0.4f),
                                fontSize = 10.sp
                            )
                        }
                    }
                }
            }

            // Divider
            Spacer(modifier = Modifier.height(4.dp))

            // Editing Panels Area
            if (activeKitId.isNotEmpty()) {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                ) {
                    // Profile description metadata card
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(20.dp))
                                .background(Color.White.copy(alpha = 0.03f))
                                .border(1.dp, Color.White.copy(alpha = 0.08f), RoundedCornerShape(20.dp))
                                .padding(16.dp)
                        ) {
                            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Text(
                                    text = "Vocal Profile Metadata",
                                    color = Color.White.copy(alpha = 0.4f),
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 1.sp
                               )
                                Text(
                                    text = kitName,
                                    color = Color.White,
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = kitDesc,
                                    color = Color.White.copy(alpha = 0.7f),
                                    fontSize = 12.sp,
                                    lineHeight = 16.sp
                                )
                                
                                // Show delete button if custom kit
                                if (activeKitId.startsWith("custom_")) {
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Button(
                                        onClick = {
                                            viewModel.deleteKit(activeKitId)
                                            Toast.makeText(context, "Kit deleted!", Toast.LENGTH_SHORT).show()
                                        },
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = Color.Red.copy(alpha = 0.1f),
                                            contentColor = Color.Red
                                        ),
                                        shape = RoundedCornerShape(12.dp),
                                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                                        modifier = Modifier.height(36.dp)
                                    ) {
                                        Icon(Icons.Default.Delete, contentDescription = "Delete", modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("Delete Profile", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                    }

                    // --- CRAWLER & WEB SLANG INTEGRATOR CONTAINER CARD ---
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth().animateContentSize(),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFF140D18)),
                            shape = RoundedCornerShape(22.dp),
                            border = BorderStroke(1.dp, Color(0xFF7D00FF).copy(alpha = 0.25f))
                        ) {
                            Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Icon(
                                        imageVector = Icons.Default.AutoAwesome,
                                        contentDescription = "Crawl",
                                        tint = Color(0xFF7D00FF),
                                        modifier = Modifier.size(22.dp)
                                    )
                                    Column {
                                        Text(
                                            text = "100K+ SLANG WEB INTEGRATOR",
                                            color = Color(0xFFFF007A),
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold,
                                            letterSpacing = 1.sp
                                        )
                                        Text(
                                            text = "AI Vibe Slang Scraper",
                                            color = Color.White,
                                            fontSize = 15.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }

                                Text(
                                    text = "Command Gemini to query deep modern street slang records, web archives, and aesthetic genre vernacular directly into your current profile.",
                                    color = Color.White.copy(alpha = 0.6f),
                                    fontSize = 11.sp,
                                    lineHeight = 15.sp
                                )

                                OutlinedTextField(
                                    value = slangSearchQuery,
                                    onValueChange = { viewModel.slangSearchQuery.value = it },
                                    placeholder = { Text("E.g., West Coast Hyphy, Brooklyn Drill, Jersey Bop...", color = Color.White.copy(alpha = 0.3f)) },
                                    modifier = Modifier.fillMaxWidth(),
                                    maxLines = 1,
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = Color(0xFF7D00FF),
                                        unfocusedBorderColor = Color.White.copy(alpha = 0.1f),
                                        focusedTextColor = Color.White,
                                        unfocusedTextColor = Color.White
                                    )
                                )

                                // Loader messages
                                if (isSlangCrawling || slangCrawlStatus != "Idle" || slangCrawlError != null) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clip(RoundedCornerShape(12.dp))
                                            .background(Color.White.copy(alpha = 0.04f))
                                            .padding(10.dp)
                                    ) {
                                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                            Text(
                                                text = if (isSlangCrawling) "SCALING INDEX..." else "RESOLVED INDEX STATUS",
                                                color = Color(0xFF7D00FF),
                                                fontSize = 8.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                            Row(
                                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                if (isSlangCrawling) {
                                                    CircularProgressIndicator(
                                                        color = Color(0xFFFF007A),
                                                        modifier = Modifier.size(12.dp),
                                                        strokeWidth = 1.5.dp
                                                    )
                                                }
                                                Text(
                                                    text = slangCrawlError ?: slangCrawlStatus,
                                                    color = if (slangCrawlError != null) Color.Red.copy(alpha = 0.8f) else Color.White,
                                                    fontSize = 11.sp,
                                                    fontStyle = FontStyle.Italic
                                                )
                                            }
                                        }
                                    }
                                }

                                Button(
                                    onClick = { viewModel.crawlSlangAndStyle(slangSearchQuery) },
                                    enabled = !isSlangCrawling && slangSearchQuery.trim().isNotEmpty(),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = Color(0xFF7D00FF),
                                        contentColor = Color.White,
                                        disabledContainerColor = Color(0xFF7D00FF).copy(alpha = 0.3f)
                                    ),
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier.fillMaxWidth().height(42.dp)
                                ) {
                                    Icon(Icons.Default.AutoAwesome, contentDescription = "Query", modifier = Modifier.size(15.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("CRAWL & OPTIMIZE 100K SLANG INDEX", fontWeight = FontWeight.Bold, fontSize = 11.sp)
                                }

                                // Crawled previews
                                val hasCrawledResults = crawledAddresses.isNotEmpty() || crawledSelfWords.isNotEmpty() || crawledActions.isNotEmpty()
                                if (hasCrawledResults) {
                                    Column(verticalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.padding(top = 4.dp)) {
                                        Text(
                                             text = "CRAWLED STYLE PROFILE RESULTS:",
                                             color = Color(0xFFFF007A),
                                             fontSize = 9.sp,
                                             fontWeight = FontWeight.Bold,
                                             letterSpacing = 0.5.sp
                                        )

                                        val previewData = listOf(
                                            "Addresses" to crawledAddresses,
                                            "Boasts" to crawledSelfWords,
                                            "Actions" to crawledActions,
                                            "Adjectives" to crawledAdjectives,
                                            "Body parts" to crawledBodyParts,
                                            "Vibes" to crawledVibeWords,
                                            "Ad-libs" to crawledAdlibs
                                        ).filter { it.second.trim().isNotEmpty() }

                                        FlowRow(
                                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                                            verticalArrangement = Arrangement.spacedBy(6.dp),
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            previewData.forEach { (cat, contentFlow) ->
                                                val firstItem = contentFlow.split("\n").firstOrNull() ?: ""
                                                val size = contentFlow.split("\n").filter { it.isNotEmpty() }.size
                                                Box(
                                                    modifier = Modifier
                                                        .clip(RoundedCornerShape(8.dp))
                                                        .background(Color.White.copy(alpha = 0.05f))
                                                        .padding(horizontal = 8.dp, vertical = 4.dp)
                                                ) {
                                                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                                        Text(text = "$cat ($size):", color = Color(0xFF7D00FF), fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                                        Text(text = firstItem, color = Color.White.copy(alpha = 0.8f), fontSize = 10.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                                    }
                                                }
                                            }
                                        }

                                        // Integrating button
                                        Button(
                                            onClick = {
                                                viewModel.mergeCrawledSlangToActiveKit()
                                                // Sync local states
                                                addresses = mergeLocalStrings(addresses, crawledAddresses)
                                                selfWords = mergeLocalStrings(selfWords, crawledSelfWords)
                                                actions = mergeLocalStrings(actions, crawledActions)
                                                adjectives = mergeLocalStrings(adjectives, crawledAdjectives)
                                                bodyParts = mergeLocalStrings(bodyParts, crawledBodyParts)
                                                vibeWords = mergeLocalStrings(vibeWords, crawledVibeWords)
                                                hooks = mergeLocalStrings(hooks, crawledHooks)
                                                adlibs = mergeLocalStrings(adlibs, crawledAdlibs)
                                                Toast.makeText(context, "Crawled slangs merged into custom kit presets!", Toast.LENGTH_SHORT).show()
                                            },
                                            colors = ButtonDefaults.buttonColors(
                                                containerColor = Color(0xFFFF007A)
                                            ),
                                            shape = RoundedCornerShape(12.dp),
                                            modifier = Modifier.fillMaxWidth().height(40.dp)
                                        ) {
                                            Icon(Icons.Default.Save, contentDescription = "Merge", modifier = Modifier.size(15.dp))
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text("MERGE INTO ACTIVE MIXER PROFILE", fontWeight = FontWeight.Bold, fontSize = 11.sp)
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // --- SEARCH OPTIMIZER BAR (Local search queries) ---
                    item {
                        OutlinedTextField(
                            value = localSearchQuery,
                            onValueChange = { localSearchQuery = it },
                            placeholder = { Text("Search slang globally in active profile...", color = Color.White.copy(alpha = 0.3f)) },
                            leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search", tint = Color(0xFFFF007A)) },
                            trailingIcon = {
                                if (localSearchQuery.isNotEmpty()) {
                                    IconButton(onClick = { localSearchQuery = "" }) {
                                        Icon(Icons.Default.Close, contentDescription = "Clear", tint = Color.White)
                                    }
                                }
                            },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(14.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Color(0xFFFF007A),
                                unfocusedBorderColor = Color.White.copy(alpha = 0.1f),
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White,
                                focusedContainerColor = Color.White.copy(alpha = 0.02f),
                                unfocusedContainerColor = Color.White.copy(alpha = 0.02f)
                            )
                        )
                    }

                    // CONDITIONAL SEARCH VIEW OR STANDARD EDIT VIEW
                    if (localSearchQuery.isNotEmpty()) {
                        item {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.04f)),
                                shape = RoundedCornerShape(20.dp),
                                border = BorderStroke(1.dp, Color(0xFFFF007A).copy(alpha = 0.2f))
                            ) {
                                Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                                    Text(
                                        text = "SEARCH OPTIMIZER FINDINGS",
                                        color = Color(0xFFFF007A),
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        letterSpacing = 1.sp
                                    )
                                    
                                    val categories = listOf(
                                        "Addresses" to addresses,
                                        "Self-Boasts" to selfWords,
                                        "Explicit Actions" to actions,
                                        "Sensual Adjectives" to adjectives,
                                        "Body Parts" to bodyParts,
                                        "Vibe Lines" to vibeWords,
                                        "Hooks" to hooks,
                                        "Ad-libs" to adlibs
                                    )

                                    var totalMatches = 0

                                    categories.forEach { (catName, catContent) ->
                                        val lines = catContent.split("\n")
                                        val matchingLines = lines.filter { it.contains(localSearchQuery, ignoreCase = true) }
                                        if (matchingLines.isNotEmpty()) {
                                            totalMatches += matchingLines.size
                                            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                                Row(
                                                    modifier = Modifier.fillMaxWidth(),
                                                    horizontalArrangement = Arrangement.SpaceBetween
                                                ) {
                                                    Text(
                                                        text = catName.uppercase(),
                                                        color = Color(0xFF7D00FF),
                                                        fontSize = 10.sp,
                                                        fontWeight = FontWeight.Bold,
                                                        fontFamily = FontFamily.Monospace
                                                    )
                                                    Text(
                                                        text = "${matchingLines.size} match(es)",
                                                        color = Color.White.copy(alpha = 0.4f),
                                                        fontSize = 9.sp
                                                    )
                                                }
                                                matchingLines.forEach { match ->
                                                    Row(
                                                        modifier = Modifier
                                                            .fillMaxWidth()
                                                            .clip(RoundedCornerShape(8.dp))
                                                            .background(Color.White.copy(alpha = 0.04f))
                                                            .padding(10.dp),
                                                        horizontalArrangement = Arrangement.SpaceBetween,
                                                        verticalAlignment = Alignment.CenterVertically
                                                    ) {
                                                        Text(
                                                            text = match,
                                                            color = Color.White,
                                                            fontSize = 13.sp,
                                                            fontWeight = FontWeight.Medium
                                                        )
                                                        Box(
                                                            modifier = Modifier
                                                                .clip(CircleShape)
                                                                .background(Color(0xFFFF007A).copy(alpha = 0.15f))
                                                                .padding(horizontal = 8.dp, vertical = 2.dp)
                                                        ) {
                                                            Text(
                                                                text = "FIND",
                                                                color = Color(0xFFFF007A),
                                                                fontSize = 8.sp,
                                                                fontWeight = FontWeight.Bold
                                                            )
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }

                                    if (totalMatches == 0) {
                                        Box(
                                            modifier = Modifier.fillMaxWidth().padding(vertical = 20.dp),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                text = "No slang matches found in active profile vocabulary.",
                                                color = Color.White.copy(alpha = 0.4f),
                                                fontSize = 12.sp,
                                                fontStyle = FontStyle.Italic
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    } else {
                        // Standard vocabulary edit text areas
                        item {
                            VocabEditField(
                                title = "ADDRESSES (e.g., lover, big daddy)",
                                value = addresses,
                                onValueChange = { addresses = it },
                                placeholder = "Enter words separated by newline"
                            )
                        }
                        item {
                            VocabEditField(
                                title = "SELF-BOASTS (e.g., this freak, baddest bitch)",
                                value = selfWords,
                                onValueChange = { selfWords = it },
                                placeholder = "Enter words separated by newline"
                            )
                        }
                        item {
                            VocabEditField(
                                title = "EXPLICIT RHYTHM ACTIONS (e.g., f**k me, throw it back)",
                                value = actions,
                                onValueChange = { actions = it },
                                placeholder = "Enter words separated by newline"
                            )
                        }
                        item {
                            VocabEditField(
                                title = "SENSUAL ADJECTIVES (e.g., wet, toxic, drippin')",
                                value = adjectives,
                                onValueChange = { adjectives = it },
                                placeholder = "Enter words separated by newline"
                            )
                        }
                        item {
                            VocabEditField(
                                title = "BODY PARTS (e.g., lips, thighs, chest)",
                                value = bodyParts,
                                onValueChange = { bodyParts = it },
                                placeholder = "Enter words separated by newline"
                            )
                        }
                        item {
                            VocabEditField(
                                title = "VIBE STATEMENT LINES",
                                value = vibeWords,
                                onValueChange = { vibeWords = it },
                                placeholder = "Enter phrases separated by newline"
                            )
                        }
                        item {
                            VocabEditField(
                                title = "CATCHY CHORUS HOOKS",
                                value = hooks,
                                onValueChange = { hooks = it },
                                placeholder = "Enter chorus hooks separated by newline"
                            )
                        }
                        item {
                            VocabEditField(
                                title = "AD-LIBS (e.g., (wet wet), (make it clap))",
                                value = adlibs,
                                onValueChange = { adlibs = it },
                                placeholder = "Enter adlibs separated by newline"
                            )
                        }
                    }

                    // Spacer at the bottom
                    item {
                        Spacer(modifier = Modifier.height(10.dp))
                    }
                }
            }

            // Save Mixing Profile Button at bottom
            Button(
                onClick = {
                    if (activeKitId.isNotEmpty()) {
                        viewModel.updateKitVocabulary(
                            kitId = activeKitId,
                            name = kitName,
                            description = kitDesc,
                            addresses = addresses,
                            selfWords = selfWords,
                            actions = actions,
                            adjectives = adjectives,
                            bodyParts = bodyParts,
                            vibeWords = vibeWords,
                            hooks = hooks,
                            adlibs = adlibs
                        )
                        Toast.makeText(context, "$kitName modules saved successfully!", Toast.LENGTH_SHORT).show()
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp)
                    .height(56.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFFF007A),
                    contentColor = Color.White
                ),
                shape = RoundedCornerShape(16.dp)
            ) {
                Icon(
                    imageVector = Icons.Filled.Save,
                    contentDescription = "Save",
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "SAVE MIXING MODULES",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
            }
        }
    }
}

// Utility to merge lists locally for instant Compose state refresh
private fun mergeLocalStrings(existing: String, incoming: String): String {
    if (incoming.trim().isEmpty()) return existing
    val set = existing.split("\n").map { it.trim() }.filter { it.isNotEmpty() }.toMutableSet()
    val incomingList = incoming.split("\n").map { it.trim() }.filter { it.isNotEmpty() }
    set.addAll(incomingList)
    return set.joinToString("\n")
}

@Composable
fun VocabEditField(
    title: String,
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String
) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(
            text = title,
            color = Color.White.copy(alpha = 0.5f),
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Monospace,
            letterSpacing = 1.sp
        )
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            placeholder = { Text(placeholder, color = Color.White.copy(alpha = 0.3f)) },
            modifier = Modifier.fillMaxWidth(),
            maxLines = 8,
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Color(0xFFFF007A),
                unfocusedBorderColor = Color.White.copy(alpha = 0.1f),
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White,
                focusedContainerColor = Color.White.copy(alpha = 0.01f),
                unfocusedContainerColor = Color.White.copy(alpha = 0.01f)
            )
        )
    }
}
