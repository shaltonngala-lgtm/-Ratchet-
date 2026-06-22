package com.example.ui.screens

import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Casino
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
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
import kotlin.math.sin
import kotlin.math.cos
import kotlin.random.Random

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SunoScreen(viewModel: LyricViewModel) {
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current

    // --- MATHEMATICAL COMBINATORIAL DATASETS ---
    val baseCategories = listOf(
        "Tech-House Grid", "Retrowave Synthpop", "Valkyrie Doom Metal", 
        "UK Drill Slide Beat", "Dusty Chillhop Lofi", "Hyperpop Glitchcore", 
        "West-African Afro-Fusion", "Desert Tuareg Rock", "Balkan Copper Brass", 
        "Acid Jazz-Funk Groove", "Vintage Cassette Shoegaze", "Cyberpunk Ebmdarkwave", 
        "Industrial Noise Metal", "Neoclassical Math-Rock", "Vaporwave Soundscape"
    ) // 15

    val timeSignatures = listOf(
        "4/4 Common Time", "3/4 Waltz Meter", "6/8 Compound feel", 
        "5/4 Odd-Time Quintupulse", "7/8 Balkan Asymmetrical", "12/8 Shuffle Cycle", 
        "9/8 Complex micro-meter", "11/8 Asymmetric Grid", "13/8 Non-Euclidean Wave",
        "5/8 Swift Irregular Meter", "15/8 Fibonacci Division"
    ) // 11

    val harmonicModes = listOf(
        "Aeolian Natural Minor", "Phrygian Dominant Spanish", "Dorian Mode Jazz", 
        "Lydian Mode Shimmer", "Double Harmonic Byzantine", "Pentatonic Eastern Airs", 
        "Mixolydian Blues-Rock", "Locrian Academic Dissonant", "Acoustic Lydian b7", 
        "Hungarian Gypsy Minor", "Whole Tone Hexatonic", "Super-Locrian Altered"
    ) // 12

    val polyrhythmRatios = listOf(
        "1:1 Lockstep Straight", "3:2 Hemiola Syncopate", "4:3 Passacaglia Tug", 
        "5:4 Quartz Spacing", "Triplet Swing Wave", "Double-Time Hat Division", 
        "Sextuplet Gallop Accent", "Dotted-Eighth Delay", "Asymmetric Drift", 
        "Fibonacci Fraction Beat"
    ) // 10

    val acousticEnvironments = listOf(
        "Dry Studio Vault", "80s Gated Snare Cavern", "Gigantic Stone Cathedral", 
        "Dusty Cassette Magnetic tape", "Binaural Spatial ASMR", "Open Arena Echoes", 
        "Bitcrushed Digital Field", "Underwater Resonant Lowpass", "Vacuum Space Absorption", 
        "Spring Coil Reverb Shimmer"
    ) // 10

    val leadInstruments = listOf(
        "Rhodes Electric Keyboard", "808 Sub-Bass Drops", "Acoustic Grand Concert", 
        "Wavetable Analog Synth", "Overdriven Valve Guitar", "Fingerstyle Gut Guitar", 
        "Traditional Koto Shamisen", "Metallic Percussion Click", "FM Mineral Glass Bell", 
        "Ambient Breathy Vocal Pad"
    ) // 10

    val soundDesigns = listOf(
        "Sub-bass focus (30Hz floor)", "Crisp sibilance (8kHz Air)", "Warm tube saturation", 
        "Telephonic bandpass lo-fi", "24-bit dynamic contrast", "Transient-sharp compression", 
        "Sidechained volume envelope", "Phased comb filter sweep", "Resonant dynamic filter sweep", 
        "High shelved dynamic peak"
    ) // 10

    val culturalOrigins = listOf(
        "UK Drill sliding bass", "Yoruba syncopated master drums", "Tokyo Akihabara chiptune bleeps", 
        "Nashville resonator steel", "Berlin minimal acid sequencer", "Parisian accordion jazz chord", 
        "Kingston dub analog feedback", "Latin dembow syncopation", "Stockholm pristine pop design", 
        "Moroccan Gnaawa triplet clap"
    ) // 10

    val l1 = baseCategories.size
    val l2 = timeSignatures.size
    val l3 = harmonicModes.size
    val l4 = polyrhythmRatios.size
    val l5 = acousticEnvironments.size
    val l6 = leadInstruments.size
    val l7 = soundDesigns.size
    val l8 = culturalOrigins.size

    // Total combination count = 15 * 11 * 12 * 10 * 10 * 10 * 10 * 10 = 198,000,000 combinations
    val totalCombinations = 198000000L

    // State: Current Seed Address (1 to 198,000,000)
    var seedAddress by rememberSaveable { mutableStateOf(42424242L) }

    // Derive indices deterministically from the seed address
    val rawIndex = (seedAddress - 1L).coerceIn(0L, totalCombinations - 1L)

    var temp = rawIndex
    val idx8 = (temp % l8).toInt(); temp /= l8
    val idx7 = (temp % l7).toInt(); temp /= l7
    val idx6 = (temp % l6).toInt(); temp /= l6
    val idx5 = (temp % l5).toInt(); temp /= l5
    val idx4 = (temp % l4).toInt(); temp /= l4
    val idx3 = (temp % l3).toInt(); temp /= l3
    val idx2 = (temp % l2).toInt(); temp /= l2
    val idx1 = (temp % l1).toInt()

    // Interactive overrides states (if user wants to manually select to change seed address)
    var selectedBaseCategoryIdx by remember { mutableStateOf(idx1) }
    var selectedTimeSignatureIdx by remember { mutableStateOf(idx2) }
    var selectedHarmonicModeIdx by remember { mutableStateOf(idx3) }
    var selectedPolyrhythmIdx by remember { mutableStateOf(idx4) }
    var selectedAcousticIdx by remember { mutableStateOf(idx5) }
    var selectedLeadIdx by remember { mutableStateOf(idx6) }
    var selectedSoundDesignIdx by remember { mutableStateOf(idx7) }
    var selectedCulturalIdx by remember { mutableStateOf(idx8) }

    // Sync selections if seed address changed from randomizer or slider
    LaunchedEffect(seedAddress) {
        selectedBaseCategoryIdx = idx1
        selectedTimeSignatureIdx = idx2
        selectedHarmonicModeIdx = idx3
        selectedPolyrhythmIdx = idx4
        selectedAcousticIdx = idx5
        selectedLeadIdx = idx6
        selectedSoundDesignIdx = idx7
        selectedCulturalIdx = idx8
    }

    // Recalculate seed address if user overrides dropdowns manually
    val triggerAddressRecomputation = { b: Int, t: Int, h: Int, p: Int, a: Int, l: Int, s: Int, c: Int ->
        val computedIndex = b.toLong() + 
                t.toLong() * l1 + 
                h.toLong() * l1 * l2 + 
                p.toLong() * l1 * l2 * l3 + 
                a.toLong() * l1 * l2 * l3 * l4 + 
                l.toLong() * l1 * l2 * l3 * l4 * l5 + 
                s.toLong() * l1 * l2 * l3 * l4 * l5 * l6 + 
                c.toLong() * l1 * l2 * l3 * l4 * l5 * l6 * l7
        seedAddress = (computedIndex + 1L).coerceIn(1L, totalCombinations)
    }

    // Determine numerical attributes based on combinations
    val calculatedBpm = remember(seedAddress) { 60 + (seedAddress % 141).toInt() }
    val pitchFrequencies = listOf("A", "A#", "B", "C", "C#", "D", "D#", "E", "F", "F#", "G", "G#")
    val keyRoot = remember(seedAddress) { pitchFrequencies[(seedAddress % 12).toInt()] }
    val keyType = remember(seedAddress) { if (seedAddress % 2 == 0L) "Minor" else "Major" }
    val isA432Hz = remember(seedAddress) { seedAddress % 7 == 0L }
    val fineTuning = if (isA432Hz) "432Hz Core" else "440Hz Concert"

    // Complex math details
    val goldenRatioMultiplier = 1.6180339887
    val waveRatio = remember(seedAddress) { (((seedAddress % 99) + 1) * goldenRatioMultiplier).coerceIn(1.0, 100.0) }
    val lissajousRatio = remember(selectedPolyrhythmIdx) { 
        when (selectedPolyrhythmIdx) {
            1 -> 3f to 2f
            2 -> 4f to 3f
            3 -> 5f to 4f
            else -> 1f to 1f
        }
    }

    // Generate output strings
    // 1. Suno Style Tags: <= 120 character limit, highly compact
    val rawTag = "${calculatedBpm}bpm, $keyRoot $keyType, ${fineTuning.take(5)}, " +
            "${baseCategories[selectedBaseCategoryIdx].lowercase().replace(" ", "-")}, " +
            "${timeSignatures[selectedTimeSignatureIdx].take(8).lowercase().trim()}, " +
            "${harmonicModes[selectedHarmonicModeIdx].lowercase().split(" ").first()}, " +
            "${polyrhythmRatios[selectedPolyrhythmIdx].lowercase().split(" ").first()}, " +
            "${leadInstruments[selectedLeadIdx].lowercase().split(" ").first()}"
    val sunoStyleTag = if (rawTag.length > 120) rawTag.take(117) + "..." else rawTag

    // 2. Full Technical Directives: Unlimited length, massive detail
    val technicalDirectives = """
        [System Math Seed Address: #$seedAddress / $totalCombinations]
        [Tempo: $calculatedBpm BPM | Metronome Subdivision Shift: ${polyrhythmRatios[selectedPolyrhythmIdx]}]
        [Frequency Matrix: $fineTuning Root Tuning | Prime Scale Frequency Focus: ${String.format("%.2f", 440.0 * Math.pow(2.0, (pitchFrequencies.indexOf(keyRoot) - 9) / 12.0))} Hz]
        [Acoustic Spatial Depth: ${acousticEnvironments[selectedAcousticIdx]}]
        [Harmonic Progression Mode: Modern $keyRoot $keyType under the ${harmonicModes[selectedHarmonicModeIdx]} architecture]
        [Sound signature: ${soundDesigns[selectedSoundDesignIdx]} integrated with a dynamic $waveRatio% wave ratio]
        [Primary orchestration timbre: ${leadInstruments[selectedLeadIdx]} syncopated over ${culturalOrigins[selectedCulturalIdx]} style grids]
    """.trimIndent()

    Box(modifier = Modifier.fillMaxSize()) {
        // Immersive cosmic purple background glow
        Box(
            modifier = Modifier
                .size(310.dp)
                .align(Alignment.TopEnd)
                .offset(x = 80.dp, y = (-20).dp)
                .background(
                    brush = Brush.radialGradient(
                        colors = listOf(Color(0xFFFF007A).copy(alpha = 0.12f), Color.Transparent)
                    )
                )
        )
        Box(
            modifier = Modifier
                .size(310.dp)
                .align(Alignment.BottomEnd)
                .offset(x = 100.dp, y = 100.dp)
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
            // Header
            Column(modifier = Modifier.padding(top = 16.dp, bottom = 4.dp)) {
                Text(
                    text = "SUNO V5.5 DETAILED PROMPT ENGINE",
                    color = Color(0xFFFF007A),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 2.sp
                )
                Text(
                    text = "Quantum Music Math",
                    color = Color.White,
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Combinatorial synth-engine modeling infinite acoustic dimensions to structure hyper-precise audio prompts for Suno V5.5 algorithms.",
                    color = Color.White.copy(alpha = 0.5f),
                    fontSize = 11.sp,
                    lineHeight = 15.sp,
                    modifier = Modifier.padding(top = 2.dp)
                )
            }

            // Real-Time Math Fourier Waveform Graphics Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 10.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.02f)),
                shape = RoundedCornerShape(20.dp),
                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.06f))
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Icon(
                                imageVector = Icons.Default.Calculate,
                                contentDescription = "Math",
                                tint = Color(0xFFFF007A),
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = "FOURIER SYNTHESIS OSCILLOSCOPE",
                                color = Color.White.copy(alpha = 0.5f),
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace,
                                letterSpacing = 1.sp
                            )
                        }
                        Text(
                            text = "Seed Multiplier: ${String.format("%.3f", waveRatio)}x",
                            color = Color(0xFFFF007A),
                            fontSize = 9.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Live computed Waveform visualization using Canvas
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(72.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(Color.Black.copy(alpha = 0.3f))
                            .border(1.dp, Color.White.copy(alpha = 0.04f), RoundedCornerShape(10.dp))
                    ) {
                        Canvas(modifier = Modifier.fillMaxSize()) {
                            val path = Path()
                            val width = size.width
                            val height = size.height
                            val midY = height / 2f
                            
                            val points = 200
                            val step = width / points
                            
                            val freqX = lissajousRatio.first
                            val freqY = lissajousRatio.second

                            for (i in 0 until points) {
                                val x = i * step
                                val t = (i.toFloat() / points) * 2f * Math.PI.toFloat()
                                
                                // Mathematically compute a compound sine+cosine waveform representing selected math parameters
                                val yOffset = sin(t * freqX * 4f + (seedAddress.toFloat() / 1000f)) * 0.4f + 
                                              cos(t * freqY * 9f - (seedAddress.toFloat() / 500f)) * 0.3f + 
                                              sin(t * (waveRatio.toFloat() / 10f)) * 0.15f
                                
                                val y = midY + (yOffset * (height * 0.45f))
                                if (i == 0) {
                                    path.moveTo(x, y)
                                } else {
                                    path.lineTo(x, y)
                                }
                            }
                            
                            drawPath(
                                path = path,
                                color = Color(0xFFFF007A).copy(alpha = 0.8f),
                                style = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round)
                            )

                            // Secondary grid lines
                            drawLine(
                                color = Color.White.copy(alpha = 0.05f),
                                start = Offset(0f, midY),
                                end = Offset(width, midY),
                                strokeWidth = 1f
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Metric summary board
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("ROOT FREQUENCY", color = Color.White.copy(alpha = 0.4f), fontSize = 8.sp, fontWeight = FontWeight.Bold)
                            Text("$keyRoot ($keyType)", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("TEMPO RATIO", color = Color.White.copy(alpha = 0.4f), fontSize = 8.sp, fontWeight = FontWeight.Bold)
                            Text("$calculatedBpm BPM", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text("PITCH STANDARD", color = Color.White.copy(alpha = 0.4f), fontSize = 8.sp, fontWeight = FontWeight.Bold)
                            Text(fineTuning, color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            // Scrollable Settings Space
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Quantum Seed Controller
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.02f)),
                        shape = RoundedCornerShape(16.dp),
                        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.06f))
                    ) {
                        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(
                                        text = "QUANTUM MUSIC SEED REGISTER",
                                        color = Color(0xFFFF007A),
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold,
                                        letterSpacing = 1.sp
                                    )
                                    Text(
                                        text = "Address: #$seedAddress",
                                        color = Color.White,
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }

                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    // Casino Scrambler Button
                                    Button(
                                        onClick = {
                                            seedAddress = Random.nextLong(1L, totalCombinations + 1L)
                                        },
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = Color(0xFFFF007A).copy(alpha = 0.15f),
                                            contentColor = Color(0xFFFF007A)
                                        ),
                                        shape = RoundedCornerShape(10.dp),
                                        contentPadding = PaddingValues(horizontal = 12.dp)
                                    ) {
                                        Icon(Icons.Default.Casino, contentDescription = "Scramble", modifier = Modifier.size(14.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("SCRAMBLE", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }

                            Text(
                                text = "Deterministically slice through one of 198 Million mathematically distinct combinations using dynamic algebraic seed mapping.",
                                color = Color.White.copy(alpha = 0.5f),
                                fontSize = 10.sp,
                                lineHeight = 13.sp
                            )

                            // Slider matching total combinations
                            Slider(
                                value = seedAddress.toFloat(),
                                onValueChange = { seedAddress = it.toLong().coerceIn(1L, totalCombinations) },
                                valueRange = 1f..totalCombinations.toFloat(),
                                colors = SliderDefaults.colors(
                                    thumbColor = Color(0xFFFF007A),
                                    activeTrackColor = Color(0xFFFF007A).copy(alpha = 0.6f),
                                    inactiveTrackColor = Color.White.copy(alpha = 0.05f)
                                ),
                                modifier = Modifier.fillMaxWidth()
                            )

                            // Prompt output copy boards
                            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                Text(
                                    text = "SUNO STYLE COMPATIBLE TAGS (UNDER 120 CHARS)",
                                    color = Color.White.copy(alpha = 0.4f),
                                    fontSize = 8.sp,
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 0.5.sp
                                )
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(Color.White.copy(alpha = 0.04f))
                                        .border(1.dp, Color.White.copy(alpha = 0.04f), RoundedCornerShape(8.dp))
                                        .padding(8.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = sunoStyleTag,
                                        color = Color.White,
                                        fontSize = 11.sp,
                                        fontStyle = FontStyle.Italic,
                                        modifier = Modifier.weight(1f),
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    IconButton(
                                        onClick = {
                                            clipboardManager.setText(AnnotatedString(sunoStyleTag))
                                            Toast.makeText(context, "Suno Style tags copied!", Toast.LENGTH_SHORT).show()
                                        },
                                        modifier = Modifier.size(24.dp)
                                    ) {
                                        Icon(Icons.Default.ContentCopy, contentDescription = "Copy", tint = Color(0xFFFF007A), modifier = Modifier.size(14.dp))
                                    }
                                }

                                Text(
                                    text = "TECHNICAL DIRECTION DIRECTIVES Prompt",
                                    color = Color.White.copy(alpha = 0.4f),
                                    fontSize = 8.sp,
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 0.5.sp,
                                    modifier = Modifier.padding(top = 4.dp)
                                )
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(Color.White.copy(alpha = 0.04f))
                                        .border(1.dp, Color.White.copy(alpha = 0.04f), RoundedCornerShape(8.dp))
                                        .padding(8.dp)
                                ) {
                                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                        Text(
                                            text = technicalDirectives,
                                            color = Color.White.copy(alpha = 0.8f),
                                            fontSize = 9.sp,
                                            fontFamily = FontFamily.Monospace,
                                            lineHeight = 12.sp
                                        )
                                        Spacer(modifier = Modifier.height(4.dp))
                                        AlignSelfCopyButton(
                                            onClick = {
                                                clipboardManager.setText(AnnotatedString(technicalDirectives))
                                                Toast.makeText(context, "Technical Math Directives copied!", Toast.LENGTH_SHORT).show()
                                            }
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // Interactive 1M+ Genre Selector Dropdown Overrides
                item {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 12.dp)
                    ) {
                        Text(
                            text = "INDIVIDUAL ALGEBRAIC GENRE SELECTION MODULES",
                            color = Color.White.copy(alpha = 0.4f),
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        )

                        // 8 Dropdowns arranged beautifully
                        SunoMatrixDropdown(
                            label = "Base Class Category",
                            items = baseCategories,
                            selectedIdx = selectedBaseCategoryIdx,
                            onSelect = {
                                selectedBaseCategoryIdx = it
                                triggerAddressRecomputation(it, selectedTimeSignatureIdx, selectedHarmonicModeIdx, selectedPolyrhythmIdx, selectedAcousticIdx, selectedLeadIdx, selectedSoundDesignIdx, selectedCulturalIdx)
                            }
                        )

                        SunoMatrixDropdown(
                            label = "Metric Timing & Meter",
                            items = timeSignatures,
                            selectedIdx = selectedTimeSignatureIdx,
                            onSelect = {
                                selectedTimeSignatureIdx = it
                                triggerAddressRecomputation(selectedBaseCategoryIdx, it, selectedHarmonicModeIdx, selectedPolyrhythmIdx, selectedAcousticIdx, selectedLeadIdx, selectedSoundDesignIdx, selectedCulturalIdx)
                            }
                        )

                        SunoMatrixDropdown(
                            label = "Harmonic Mode Architecture",
                            items = harmonicModes,
                            selectedIdx = selectedHarmonicModeIdx,
                            onSelect = {
                                selectedHarmonicModeIdx = it
                                triggerAddressRecomputation(selectedBaseCategoryIdx, selectedTimeSignatureIdx, it, selectedPolyrhythmIdx, selectedAcousticIdx, selectedLeadIdx, selectedSoundDesignIdx, selectedCulturalIdx)
                            }
                        )

                        SunoMatrixDropdown(
                            label = "Polyrhythm Accent Ratio",
                            items = polyrhythmRatios,
                            selectedIdx = selectedPolyrhythmIdx,
                            onSelect = {
                                selectedPolyrhythmIdx = it
                                triggerAddressRecomputation(selectedBaseCategoryIdx, selectedTimeSignatureIdx, selectedHarmonicModeIdx, it, selectedAcousticIdx, selectedLeadIdx, selectedSoundDesignIdx, selectedCulturalIdx)
                            }
                        )

                        SunoMatrixDropdown(
                            label = "Reverb Acoustic Spatial Space",
                            items = acousticEnvironments,
                            selectedIdx = selectedAcousticIdx,
                            onSelect = {
                                selectedAcousticIdx = it
                                triggerAddressRecomputation(selectedBaseCategoryIdx, selectedTimeSignatureIdx, selectedHarmonicModeIdx, selectedPolyrhythmIdx, it, selectedLeadIdx, selectedSoundDesignIdx, selectedCulturalIdx)
                            }
                        )

                        SunoMatrixDropdown(
                            label = "Lead Instrument Timbre Resonance",
                            items = leadInstruments,
                            selectedIdx = selectedLeadIdx,
                            onSelect = {
                                selectedLeadIdx = it
                                triggerAddressRecomputation(selectedBaseCategoryIdx, selectedTimeSignatureIdx, selectedHarmonicModeIdx, selectedPolyrhythmIdx, selectedAcousticIdx, it, selectedSoundDesignIdx, selectedCulturalIdx)
                            }
                        )

                        SunoMatrixDropdown(
                            label = "High Fidelity Sound Design Shimmer",
                            items = soundDesigns,
                            selectedIdx = selectedSoundDesignIdx,
                            onSelect = {
                                selectedSoundDesignIdx = it
                                triggerAddressRecomputation(selectedBaseCategoryIdx, selectedTimeSignatureIdx, selectedHarmonicModeIdx, selectedPolyrhythmIdx, selectedAcousticIdx, selectedLeadIdx, it, selectedCulturalIdx)
                            }
                        )

                        SunoMatrixDropdown(
                            label = "Cultural Syncretism Influence",
                            items = culturalOrigins,
                            selectedIdx = selectedCulturalIdx,
                            onSelect = {
                                selectedCulturalIdx = it
                                triggerAddressRecomputation(selectedBaseCategoryIdx, selectedTimeSignatureIdx, selectedHarmonicModeIdx, selectedPolyrhythmIdx, selectedAcousticIdx, selectedLeadIdx, selectedSoundDesignIdx, it)
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun SunoMatrixDropdown(
    label: String,
    items: List<String>,
    selectedIdx: Int,
    onSelect: (Int) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(
            text = label.uppercase(),
            color = Color.White.copy(alpha = 0.4f),
            fontSize = 8.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Monospace,
            letterSpacing = 0.5.sp
        )

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(Color.White.copy(alpha = 0.03f))
                .border(1.dp, Color.White.copy(alpha = 0.08f), RoundedCornerShape(12.dp))
                .clickable { expanded = true }
                .padding(horizontal = 12.dp, vertical = 10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = items.getOrNull(selectedIdx) ?: "Default option",
                    color = Color.White,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Icon(
                    imageVector = Icons.Default.ArrowDropDown,
                    contentDescription = "Expand",
                    tint = Color.White.copy(alpha = 0.6f),
                    modifier = Modifier.size(18.dp)
                )
            }

            DropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false },
                modifier = Modifier
                    .fillMaxWidth(0.85f)
                    .background(Color(0xFF141217))
                    .border(1.dp, Color.White.copy(alpha = 0.1f), RoundedCornerShape(8.dp))
            ) {
                items.forEachIndexed { idx, item ->
                    DropdownMenuItem(
                        text = {
                            Text(
                                text = item,
                                color = if (idx == selectedIdx) Color(0xFFFF007A) else Color.White,
                                fontSize = 12.sp,
                                fontWeight = if (idx == selectedIdx) FontWeight.Bold else FontWeight.Normal
                            )
                        },
                        onClick = {
                            onSelect(idx)
                            expanded = false
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun AlignSelfCopyButton(onClick: () -> Unit) {
    Box(
        modifier = Modifier.fillMaxWidth(),
        contentAlignment = Alignment.BottomEnd
    ) {
        Button(
            onClick = onClick,
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFFFF007A).copy(alpha = 0.12f),
                contentColor = Color(0xFFFF007A)
            ),
            shape = RoundedCornerShape(8.dp),
            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
            modifier = Modifier.height(28.dp)
        ) {
            Icon(Icons.Default.ContentCopy, contentDescription = "Copy", modifier = Modifier.size(11.dp))
            Spacer(modifier = Modifier.width(4.dp))
            Text("COPY MATH DIRECTIVES", fontSize = 9.sp, fontWeight = FontWeight.Bold)
        }
    }
}
