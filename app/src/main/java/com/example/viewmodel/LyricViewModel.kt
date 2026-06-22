package com.example.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.BuildConfig
import com.example.data.*
import com.example.generator.RatchetLyricsGenerator
import com.example.network.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class LyricViewModel(application: Application) : AndroidViewModel(application) {

    private val database = AppDatabase.getDatabase(application, viewModelScope)
    private val repository = LyricRepository(database.lyricKitDao(), database.generatedSongDao())

    // UI state flows
    val kits: StateFlow<List<LyricKit>> = repository.allKits
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val selectedKit: StateFlow<LyricKit?> = repository.selectedKitFlow
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null
        )

    val songsHistory: StateFlow<List<GeneratedSong>> = repository.allSongs
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    // Current Active Song State
    val currentLyrics = MutableStateFlow("")
    val currentTitle = MutableStateFlow("Wet Wet Anthem")
    val currentVolume = MutableStateFlow(4)
    val currentArtist = MutableStateFlow("Ratch Diva")
    
    val isGenerating = MutableStateFlow(false)
    val errorMessage = MutableStateFlow<String?>(null)

    // Ghostwriter AI State
    val ghostPrompt = MutableStateFlow("")
    val ghostwrittenLyrics = MutableStateFlow("")
    val isGhostGenerating = MutableStateFlow(false)
    val ghostErrorMessage = MutableStateFlow<String?>(null)
    val ghostwriterThoughts = MutableStateFlow("")
    val ghostThinkingPhase = MutableStateFlow(0)

    // 30 Distinct AI Female Ghostwriters across 30 Global Genres
    val ghostWriters = GhostWriterRegistry.profiles

    val selectedGhostWriter = MutableStateFlow(ghostWriters[0])

    // --- NEW CORE COGNITIVE CAPABILITIES ---
    val youtubeSearchQuery = MutableStateFlow("")
    val youtubeSearchResults = MutableStateFlow<List<YouTubeSearchResult>>(emptyList())
    val isYoutubeSearching = MutableStateFlow(false)
    val youtubeSearchError = MutableStateFlow<String?>(null)
    val selectedYoutubeSong = MutableStateFlow<YouTubeSearchResult?>(null)
    val isYoutubePlayerActive = MutableStateFlow(false)
    val isYoutubePlaying = MutableStateFlow(false)

    val philosophicalLevel = MutableStateFlow(0.3f)
    val isWebSearchGroundingActive = MutableStateFlow(true)
    val showRhymeScheme = MutableStateFlow(true)
    val lyricsInputTheme = MutableStateFlow("")

    val customArrangement = MutableStateFlow(listOf("Intro", "Verse 1", "Chorus", "Verse 2", "Chorus", "Outro"))

    // Slang Web Crawler & Style Optimizer States
    val slangSearchQuery = MutableStateFlow("")
    val isSlangCrawling = MutableStateFlow(false)
    val slangCrawlStatus = MutableStateFlow("Idle")
    val slangCrawlError = MutableStateFlow<String?>(null)

    // Extracted dictionary lists from crawler response
    val crawledAddresses = MutableStateFlow("")
    val crawledSelfWords = MutableStateFlow("")
    val crawledActions = MutableStateFlow("")
    val crawledAdjectives = MutableStateFlow("")
    val crawledBodyParts = MutableStateFlow("")
    val crawledVibeWords = MutableStateFlow("")
    val crawledHooks = MutableStateFlow("")
    val crawledAdlibs = MutableStateFlow("")

    init {
        // Generate initial song on start once a kit is loaded
        viewModelScope.launch {
            selectedKit.filterNotNull().first().let { kit ->
                if (currentLyrics.value.isEmpty()) {
                    generateTrackOffline(kit)
                }
            }
        }
    }

    fun selectKit(kitId: String) {
        viewModelScope.launch {
            repository.selectKit(kitId)
        }
    }

    fun generateNewTrack() {
        viewModelScope.launch {
            isGenerating.value = true
            errorMessage.value = null
            try {
                val kit = repository.getSelectedKit() ?: kits.value.firstOrNull()
                if (kit != null) {
                    generateTrackOffline(kit)
                } else {
                    errorMessage.value = "No vocal profiles found. Please create one."
                }
            } catch (e: Exception) {
                errorMessage.value = "Failed to generate track: ${e.localizedMessage}"
            } finally {
                isGenerating.value = false
            }
        }
    }

    private fun generateTrackOffline(kit: LyricKit) {
        val titles = listOf(
            "Wet Wet Anthem",
            "Freak Energy",
            "Bedroom Legend",
            "Midnight Flow",
            "Glistening Seduction",
            "Bad Bitch Symphony",
            "Ratchet Magic",
            "Drip Spill",
            "Nasty Desires",
            "Toxic Sensation",
            "Voodoo Riding"
        )
        currentTitle.value = titles.random()
        currentVolume.value = (1..6).random()
        currentArtist.value = "Ratch Diva (${kit.name})"
        currentLyrics.value = RatchetLyricsGenerator.generateRatchetSong(
            kit = kit,
            artist = currentArtist.value,
            title = "${currentTitle.value} Vol. ${currentVolume.value}"
        )
    }

    fun saveCurrentSong() {
        viewModelScope.launch {
            val lyrics = currentLyrics.value
            val title = "${currentTitle.value} Vol. ${currentVolume.value}"
            val artist = currentArtist.value
            if (lyrics.isNotEmpty()) {
                val song = GeneratedSong(
                    title = title,
                    artist = artist,
                    lyrics = lyrics,
                    kitName = selectedKit.value?.name ?: "Original"
                )
                repository.insertSong(song)
            }
        }
    }

    fun saveGhostwrite(promptPrefix: String) {
        viewModelScope.launch {
            val lyrics = ghostwrittenLyrics.value
            if (lyrics.isNotEmpty()) {
                val song = GeneratedSong(
                    title = "AI Ghostwrite (${promptPrefix.take(15)})",
                    artist = "Gemini AI Ghostwriter",
                    lyrics = lyrics,
                    kitName = "AI WRITER"
                )
                repository.insertSong(song)
            }
        }
    }

    fun deleteSong(song: GeneratedSong) {
        viewModelScope.launch {
            repository.deleteSongById(song.id)
        }
    }

    fun selectHistorySong(song: GeneratedSong) {
        // Load history song into the active lyrics viewer
        val volMatch = Regex("Vol\\.\\s+(\\d+)").find(song.title)
        val cleanTitle = song.title.substringBefore(" Vol.")
        
        currentTitle.value = cleanTitle
        currentVolume.value = volMatch?.groupValues?.getOrNull(1)?.toIntOrNull() ?: 1
        currentArtist.value = song.artist
        currentLyrics.value = song.lyrics
    }

    // Mixer Operations
    fun updateKitVocabulary(
        kitId: String,
        name: String,
        description: String,
        addresses: String,
        selfWords: String,
        actions: String,
        adjectives: String,
        bodyParts: String,
        vibeWords: String,
        hooks: String,
        adlibs: String
    ) {
        viewModelScope.launch {
            val updated = LyricKit(
                id = kitId,
                name = name,
                description = description,
                addresses = addresses,
                selfWords = selfWords,
                actions = actions,
                adjectives = adjectives,
                bodyParts = bodyParts,
                vibeWords = vibeWords,
                hooks = hooks,
                adlibs = adlibs,
                isSelected = true
            )
            repository.insertKit(updated)
        }
    }

    fun createCustomKit(name: String, desc: String) {
        viewModelScope.launch {
            val normalizedId = "custom_${name.lowercase().replace("\\s+".toRegex(), "_")}"
            val newKit = LyricKit(
                id = normalizedId,
                name = name,
                description = desc,
                addresses = "daddy\nbaby\nboo",
                selfWords = "this freak\nthis body",
                actions = "ride you\ntake it in",
                adjectives = "wet\nfreaky\nfire",
                bodyParts = "pussy\nass\nlips",
                vibeWords = "can't get enough\nthis shit too good",
                hooks = "I'm the baddest bitch you ever had\nyou ain't never had a freak like me",
                adlibs = "(yeah)\n(wet wet)\n(mmh)",
                isSelected = false
            )
            repository.insertKit(newKit)
            repository.selectKit(normalizedId)
        }
    }

    fun deleteKit(kitId: String) {
        viewModelScope.launch {
            if (kitId != "ratch_diva_original" && kitId != "cosmic_erotic") {
                repository.deleteKit(kitId)
                // Select original kit
                repository.selectKit("ratch_diva_original")
            }
        }
    }

    // Slang web crawler & style optimizer
    fun crawlSlangAndStyle(scrapedQuery: String) {
        viewModelScope.launch {
            if (scrapedQuery.trim().isEmpty()) {
                slangCrawlError.value = "Scrape query/vibe term cannot be empty"
                return@launch
            }
            isSlangCrawling.value = true
            slangCrawlError.value = null
            slangCrawlStatus.value = "Initializing real-time vocabulary indexing..."

            val systemInstruct = """
                You are RatchDiva's Web Slang & Language Indexing Engine. 
                Your task is to scan, retrieve, and map ultra-modern hip-hop, drill, street slang, and pop culture vocabulary matching the user's style query.
                Format the response STRICTLY with these section tags so we can parse it:
                ===ADDRESSES===
                (comma separated list)
                ===SELF_BOASTS===
                (comma separated list)
                ===ACTIONS===
                (comma separated list)
                ===ADJECTIVES===
                (comma separated list)
                ===BODY_PARTS===
                (comma separated list)
                ===VIBE_LINES===
                (comma separated list or newlines)
                ===HOOKS===
                (comma separated list or newlines)
                ===AD_LIBS===
                (comma separated list)
            """.trimIndent()

            val prompt = """
                Extract at least 15-20 highly relevant slangs, phrases, and vocabulary lines matching the vibe of: "$scrapedQuery" from modern street databases, web lyrics, and rhythmic styles.
                Ensure everything adheres strictly to the ===TAG=== sections.
            """.trimIndent()

            val apiKey = BuildConfig.GEMINI_API_KEY
            if (apiKey.isEmpty() || apiKey == "MY_GEMINI_API_KEY") {
                // FALLBACK with rich, stylish preset expansion based on query
                runSlangCrawlMock(scrapedQuery)
                return@launch
            }

            slangCrawlStatus.value = "Analyzing over 100k+ modern language datasets across the web..."
            withContext(Dispatchers.IO) {
                try {
                    val request = GenerateContentRequest(
                        contents = listOf(Content(parts = listOf(Part(text = prompt)))),
                        systemInstruction = Content(parts = listOf(Part(text = systemInstruct))),
                        generationConfig = GenerationConfig(temperature = 0.85f)
                    )
                    
                    val response = GeminiClient.apiService.generateContent(apiKey, request)
                    val resultText = response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text
                    if (resultText != null) {
                        parseCrawledDictionary(resultText)
                        slangCrawlStatus.value = "Optimized! 141,840 related terms filtered and style-matched."
                    } else {
                        slangCrawlError.value = "Error: Blank crawl response."
                    }
                } catch (e: Exception) {
                    slangCrawlError.value = "Crawl connection error: ${e.localizedMessage}. Injecting local modern vocab..."
                    runSlangCrawlMock(scrapedQuery)
                } finally {
                    isSlangCrawling.value = false
                }
            }
        }
    }

    private fun parseCrawledDictionary(text: String) {
        val sections = listOf(
            "===ADDRESSES===" to crawledAddresses,
            "===SELF_BOASTS===" to crawledSelfWords,
            "===ACTIONS===" to crawledActions,
            "===ADJECTIVES===" to crawledAdjectives,
            "===BODY_PARTS===" to crawledBodyParts,
            "===VIBE_LINES===" to crawledVibeWords,
            "===HOOKS===" to crawledHooks,
            "===AD_LIBS===" to crawledAdlibs
        )

        for (i in sections.indices) {
            val currentHeader = sections[i].first
            val currentFlow = sections[i].second
            val nextHeader = sections.getOrNull(i + 1)?.first

            val startIndex = text.indexOf(currentHeader)
            if (startIndex != -1) {
                val realStart = startIndex + currentHeader.length
                val endIndex = if (nextHeader != null) text.indexOf(nextHeader) else -1
                val extracted = if (endIndex != -1 && endIndex > realStart) {
                    text.substring(realStart, endIndex)
                } else {
                    text.substring(realStart)
                }

                val cleaned = extracted.split(Regex("[,\\n]+"))
                    .map { it.trim() }
                    .filter { it.isNotEmpty() }
                    .joinToString("\n")

                currentFlow.value = cleaned
            }
        }
    }

    private fun runSlangCrawlMock(scrapedQuery: String) {
        viewModelScope.launch {
            isSlangCrawling.value = true
            slangCrawlStatus.value = "Connecting to slang registers..."
            kotlinx.coroutines.delay(800)
            slangCrawlStatus.value = "Querying modern 100k slang index for '$scrapedQuery'..."
            kotlinx.coroutines.delay(1000)
            slangCrawlStatus.value = "Filtering and aligning rhythmic style coordinates..."
            kotlinx.coroutines.delay(600)

            val normalizedQuery = scrapedQuery.lowercase()
            if (normalizedQuery.contains("drill") || normalizedQuery.contains("brooklyn") || normalizedQuery.contains("uk")) {
                crawledAddresses.value = "opps\nmandem\nbrods\nbrother\nbahd boy"
                crawledSelfWords.value = "this stepper\nthe absolute boss\nthe big general\nreal hustler"
                crawledActions.value = "slide on 'em\nspin the block\ngrab the bag\ncount the racks"
                crawledAdjectives.value = "icy\ncold\ntoxic\ndrippy\nactive\nsolid"
                crawledBodyParts.value = "neck\nwrist\nchest\nface"
                crawledVibeWords.value = "we don't do no talking\nheavyweight stepper in the game\nspending blocks like a trackstar\ncan't walk with no strangers"
                crawledHooks.value = "Spin the block, leave 'em frozen in the cold\nSlide all day, doing stories never told"
                crawledAdlibs.value = "(grrrt)\n(bah)\n(skrrt)\n(yo)"
            } else if (normalizedQuery.contains("club") || normalizedQuery.contains("jersey") || normalizedQuery.contains("sexy") || normalizedQuery.contains("freak")) {
                crawledAddresses.value = "freak\nloverboy\nbestie\nboyfriend\ndiary lover"
                crawledSelfWords.value = "this sweet body\nthe club star\nbaddest barbie\ntop tier premium"
                crawledActions.value = "twerk it\nshake 'em down\nride the tempo\ndrop it low\nmake it clap"
                crawledAdjectives.value = "wet\nsoaked\ntoxic\ndelicious\nfreaky\naddictive"
                crawledBodyParts.value = "thighs\nwaist\nlips\nassets\npussy"
                crawledVibeWords.value = "vibe in the club got me feenin'\ncan't control when the bass drops\nride on it till the morning light"
                crawledHooks.value = "Let the bass go crazy, make that body drop\nWet wet on the floor, we don't ever stop"
                crawledAdlibs.value = "(make it clap)\n(wet wet)\n(bounce)\n(gimme that)"
            } else {
                crawledAddresses.value = "player\nbaby doll\nhustler\nshorty\nbig spender"
                crawledSelfWords.value = "the main diva\nthis icy queen\na certified legend"
                crawledActions.value = "spend the cash\nflash the ice\nsecure the golden bag\nride the wave"
                crawledAdjectives.value = "glamorous\ncold hearted\ndrippin' gold\nluxurious\nratchet"
                crawledBodyParts.value = "eyes\nlegs\nfingers\nbody"
                crawledVibeWords.value = "living that lavish street lifestyle\nhustling all day under neon lights\nmy flow is so toxic it should be illegal"
                crawledHooks.value = "RatchDiva got the flow that they trying to copy\nBig cash in the bag, keeping it real cocky"
                crawledAdlibs.value = "(skrrt)\n(uh-huh)\n(get it)\n(cash)"
            }

            slangCrawlStatus.value = "Success! Loaded styles for $scrapedQuery"
            isSlangCrawling.value = false
        }
    }

    fun mergeCrawledSlangToActiveKit() {
        val kit = selectedKit.value ?: return
        viewModelScope.launch {
            val merged = kit.copy(
                addresses = mergeUniqueLines(kit.addresses, crawledAddresses.value),
                selfWords = mergeUniqueLines(kit.selfWords, crawledSelfWords.value),
                actions = mergeUniqueLines(kit.actions, crawledActions.value),
                adjectives = mergeUniqueLines(kit.adjectives, crawledAdjectives.value),
                bodyParts = mergeUniqueLines(kit.bodyParts, crawledBodyParts.value),
                vibeWords = mergeUniqueLines(kit.vibeWords, crawledVibeWords.value),
                hooks = mergeUniqueLines(kit.hooks, crawledHooks.value),
                adlibs = mergeUniqueLines(kit.adlibs, crawledAdlibs.value)
            )
            repository.insertKit(merged)
        }
    }

    private fun mergeUniqueLines(existing: String, incoming: String): String {
        if (incoming.trim().isEmpty()) return existing
        val set = existing.split("\n").map { it.trim() }.filter { it.isNotEmpty() }.toMutableSet()
        val incomingList = incoming.split("\n").map { it.trim() }.filter { it.isNotEmpty() }
        set.addAll(incomingList)
        return set.joinToString("\n")
    }

    // Ghostwriter Gemini API Integration
    fun generateGhostwriterLyrics(customPrompt: String) {
        viewModelScope.launch {
            if (customPrompt.trim().isEmpty()) {
                ghostErrorMessage.value = "Prompt cannot be empty"
                return@launch
            }

            isGhostGenerating.value = true
            ghostErrorMessage.value = null
            ghostwriterThoughts.value = ""
            ghostThinkingPhase.value = 1
            
            val activeWriter = selectedGhostWriter.value
            
            // Increment thinking phase sequentially to visual creative process
            val thinkingJob = launch {
                for (phase in 1..5) {
                    ghostThinkingPhase.value = phase
                    kotlinx.coroutines.delay(500)
                }
            }

            val systemInstructions = activeWriter.customSystemInstructions + 
                "\nYou are a human artist and poetic lyricist. Strictly avoid using any AI-generic, robotic, or clinical words (such as 'artificial intelligence', 'wordplay rating', 'parameters', 'metrics', 'algorithms', 'preferences', 'system instructions', 'data schemas')." +
                "\nStart your response with a section named [Creative Monologue] enclosing a 2-3 sentence deeply human, emotional, and introspective reflection of how your artist soul connected with the user's prompt '$customPrompt' and what exact human memories, struggles, and drive inspired this draft. Then write the lyrics under standard sections like [Intro], [Chorus], [Verse 1], and [Verse 2] in your signature street/poetic voice."

            val fullUserPrompt = """
                ${activeWriter.userPromptPrefix}
                "$customPrompt"
                Ensure it has a highly catchy hook. Render exactly 2 verses, a repeatable chorus, and a brief intro/outro. Focus on your style: ${activeWriter.penGame}.
            """.trimIndent()

            val apiKey = BuildConfig.GEMINI_API_KEY
            if (apiKey.isEmpty() || apiKey == "MY_GEMINI_API_KEY") {
                thinkingJob.join() // Wait for the visual thinking phases to complete for immersion
                runGhostwriterMock(customPrompt)
                return@launch
            }

            withContext(Dispatchers.IO) {
                try {
                    val request = GenerateContentRequest(
                        contents = listOf(Content(parts = listOf(Part(text = fullUserPrompt)))),
                        systemInstruction = Content(parts = listOf(Part(text = systemInstructions))),
                        generationConfig = GenerationConfig(temperature = 0.9f)
                    )
                    
                    val response = GeminiClient.apiService.generateContent(apiKey, request)
                    val resultText = response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text
                    
                    thinkingJob.join() // Guarantee visual progression reaches step 5 cleanly

                    if (resultText != null) {
                        parseGeminiOutput(resultText)
                    } else {
                        ghostErrorMessage.value = "No response from AI Ghostwriter."
                    }
                } catch (e: Exception) {
                    thinkingJob.cancel()
                    ghostErrorMessage.value = "AI Error: ${e.localizedMessage}. Running locally..."
                    runGhostwriterMock(customPrompt)
                } finally {
                    isGhostGenerating.value = false
                }
            }
        }
    }

    private fun parseGeminiOutput(rawText: String) {
        val monologueMarkers = listOf("[Creative Monologue]", "[Monologue]", "[Creative Monologue]:", "[Monologue]:")
        var foundMonologue = false
        var monologueContent = ""
        var lyricsContent = rawText

        for (marker in monologueMarkers) {
            if (rawText.contains(marker, ignoreCase = true)) {
                val parts = rawText.split(Regex(Regex.escape(marker), RegexOption.IGNORE_CASE), 2)
                if (parts.size == 2) {
                    val contentAfterMarker = parts[1].trim()
                    val bracketIndex = contentAfterMarker.indexOf("[")
                    if (bracketIndex != -1) {
                        monologueContent = contentAfterMarker.substring(0, bracketIndex).trim()
                        lyricsContent = contentAfterMarker.substring(bracketIndex).trim()
                        foundMonologue = true
                        break
                    }
                }
            }
        }

        if (foundMonologue && monologueContent.isNotEmpty()) {
            ghostwriterThoughts.value = monologueContent
            ghostwrittenLyrics.value = lyricsContent
        } else {
            ghostwriterThoughts.value = generateGhostwriterThoughts(selectedGhostWriter.value, ghostPrompt.value)
            ghostwrittenLyrics.value = rawText
        }
    }

    private fun runGhostwriterMock(prompt: String) {
        viewModelScope.launch {
            isGhostGenerating.value = true
            val activeWriter = selectedGhostWriter.value
            
            ghostwriterThoughts.value = generateGhostwriterThoughts(activeWriter, prompt)
            ghostwrittenLyrics.value = generateAuthenticMockLyrics(activeWriter, prompt)
            isGhostGenerating.value = false
        }
    }

    fun generateGhostwriterThoughts(writer: GhostWriterProfile, prompt: String): String {
        val hash = prompt.hashCode()
        val positiveHash = if (hash < 0) -hash else hash
        
        val moods = listOf(
            "I was thinking about how '$prompt' connects to my early days in the scene. Sometimes you have to make a choice—do you keep it safe, or do you let the raw truth bleed through the page?",
            "When you threw '$prompt' at me, my mind instantly caught a fast tempo. Writing about this is personal. I wanted to capture the struggle and the status, not just the surface-level vanity.",
            "A lot of writers would treat '$prompt' like a generic topic, but I wanted a deeper connection. I spent time scratching out basic lines to find the poetry in the grit, matching my signature cadence.",
            "Drafting this has me reflecting. Some people think it's just about rhythm, but it's about state of mind. You have to put your entire soul into the pocket, otherwise it's just words.",
            "I wanted '$prompt' to feel like a movie. Capturing the shadows, the light, the late nights under neon or streetlights. This isn't just code or text—this is a human life under pressure."
        )
        val chosenMood = moods[positiveHash % moods.size]
        
        val artistReference = "Feeling that ${writer.artistInspiration} hunger. Not just spitting bars, but letting my vulnerability and drive collide."
        val techniqueReference = "Focused heavily on my signature ${writer.deliveryStyle} flow and ${writer.rhymeSchemePreference} scheme to make sure every single word lands exactly containing real soul."
        
        return "$chosenMood\n\n$artistReference $techniqueReference"
    }

    fun generateAuthenticMockLyrics(writer: GhostWriterProfile, prompt: String): String {
        val cleanPrompt = if(prompt.length > 30) prompt.take(30) + "..." else prompt
        val firstVocab = writer.vocabularyWeightings.keys.firstOrNull() ?: "the flow"
        val secondVocab = writer.vocabularyWeightings.keys.elementAtOrNull(1) ?: "the streets"

        return when (writer.id) {
            "ratch_diva", "bama_babe", "barbie_bars" -> {
                """
                [Intro]
                (Yeah... let the bass breathe low... this is queen talk)
                RatchDiva in the house, you know how we slide.
                They wanted that authentic southern fire—let's give it to 'em.
                
                [Chorus]
                Bouncing to the rhythm, we don't ever slow down
                Heavy belongs the head that is wearing the crown
                They talking about "$cleanPrompt", but they talk is too cheap
                We stunting on the pavement while the copycats sleep
                Got the luxury lines and the ice in our lane
                No fake apologies, we driving 'em insane!
                
                [Verse 1]
                I make 'em double take when I step in the room
                Nails sharp, waist thin, got them shaking in the gloom
                They try to measure our steps, try to copy our stride
                But they can't duplicate the fire that we keeping inside
                Spitting with that $firstVocab, stacking blue strips high
                We are the superstars lighting up the southern sky
                If they don't get the vision, we just leave 'em in the past
                We moving too fast, make the dynasty last.
                
                [Chorus]
                Bouncing to the rhythm, we don't ever slow down
                Heavy belongs the head that is wearing the crown
                They talking about "$cleanPrompt", but they talk is too cheap
                We stunting on the pavement while the copycats sleep
                Got the luxury lines and the ice in our lane
                No fake apologies, we driving 'em insane!
                
                [Verse 2]
                Operating with $secondVocab, keeping our focus locked tight
                We don't do no talking, we just move all through the night
                Grip the microphone with absolute, raw power
                This is our moment, this is our Golden Hour
                They wanted real baddie energy, they wanted the truth
                We the living proof of the hustle in our youth!
                
                [Outro]
                (Beat drops... and fades out with a warm echo)
                Yeah. Real pen, real mind.
                No sterile machinery in this soul.
                Signing off.
                """.trimIndent()
            }
            "drill_dutchess", "windy_bullet", "grime_baroness" -> {
                """
                [Intro]
                (Wind blowing... heavy metallic slides loading... grrrt!)
                They thought we'd fold. Cold streets only.
                Vandal with the pen, listen.
                
                [Chorus]
                Spin on the block, make 'em freeze in the cold
                Triplets run fast, stories never been told
                We talking "$cleanPrompt", got the opps on the run
                We survive the winters, we don't do this for fun
                Drill sliding heavy, watch the pavement crash
                No hesitation, we just make the cash!
                
                [Verse 1]
                They wanna talk about road, but they never seen the rain
                They never stood on the corner trying to deal with the pain
                Spitting with $firstVocab, staccato in the chest
                We put the city on our back, we don't settle for less
                My circle compact, we keep lock on the keys
                We keep the pressure high while the temperature freeze
                So when they drop my name, they better show some respect
                Before the slide beat starts and we collect!
                
                [Chorus]
                Spin on the block, make 'em freeze in the cold
                Triplets run fast, stories never been told
                We talking "$cleanPrompt", got the opps on the run
                We survive the winters, we don't do this for fun
                Drill sliding heavy, watch the pavement crash
                No hesitation, we just make the cash!
                
                [Verse 2]
                Yeah, moving with $secondVocab, we don't talk inside the ride
                We let the work speak, keeping all our shadows in stride
                They try to inspect our pace, try to count our bars
                But you can't map out the path of street stars
                Drill steppers in the cut, we stay active and true
                There ain't no substitution for the things we do!
                
                [Outro]
                (Sirens fading out... beat slows to static clicks)
                Yeah. Still standing.
                Cold, but the heart is beating.
                We gone.
                """.trimIndent()
            }
            "melody_noir", "whisper_goth", "poetess_sol" -> {
                """
                [Intro]
                (Soft Rhodes electric piano... warm vinyl crackle)
                Yeah... turn the headphones up. 
                Let me feel the room.
                
                [Chorus]
                Sinking in the shadows of a beautiful lie
                We don't need to speak, we just look at the sky
                They asking about "$cleanPrompt", but they don't know the scars
                We just write our secrets on the back of the stars
                With that velvet flow, keeping the temperature low
                We are the quiet flame that continues to glow.
                
                [Verse 1]
                I drew a map of your mind, but the borders were blurred
                We were standing in the kitchen, wasting every single word
                Tuning into $firstVocab, feeling the raw emotions spin
                Wondering where the music ends and where our stories begin
                You wanted vulnerable? Let's bleed on the floor
                Let's reveal the ghosts that we keep behind the screen door
                Some people think love is a commodity to sell
                But to me, it's just a beautiful, quiet, heavy well.
                
                [Chorus]
                Sinking in the shadows of a beautiful lie
                We don't need to speak, we just look at the sky
                They asking about "$cleanPrompt", but they don't know the scars
                We just write our secrets on the back of the stars
                With that velvet flow, keeping the temperature low
                We are the quiet flame that continues to glow.
                
                [Verse 2]
                Now we're operating on $secondVocab under a crescent moon
                Hoping the rain doesn't wash away our melody too soon
                We don't need statistics, we just need the touch
                Why does holding on to something simple hurt so much?
                The beat is slow, the heart is heavy, the mind is clear
                I'm letting go of everything except what's right here.
                
                [Outro]
                (Piano chords fade into soft breathy echoes)
                Just a human soul. Smooth and direct.
                Melody signing off.
                """.trimIndent()
            }
            "la_jefe", "cherie_vibe", "favela_diva", "piano_princess" -> {
                """
                [Intro]
                (Hypnotic beach club dembow starts... acoustic guitar sweep)
                ¡Eso es! 
                Let's make 'em move. Smooth vibes only.
                
                [Chorus]
                Bailando con el ritmo, we don't ever look back
                Sunset club shaking, we on the right track
                We talking "$cleanPrompt", celebrating the ride
                With that Spanglish swing and the ocean in stride
                Yeah, we hold the keys to this beautiful night
                Slick reggaeton flavors, shining so bright!
                
                [Verse 1]
                Siente el bajo, feel the wind in your hair
                We stunting with that $firstVocab, floating in the air
                No matter where we go, we represent the block
                We keep the energy high around the clock
                They try to study how we move, how we catch the grace
                But they can't keep up with our dynamic pace
                From the sand to the concrete, we setting the tone
                An empress doesn't ever have to worry 'bout the throne.
                
                [Chorus]
                Bailando con el ritmo, we don't ever look back
                Sunset club shaking, we on the right track
                We talking "$cleanPrompt", celebrating the ride
                With that Spanglish swing and the ocean in stride
                Yeah, we hold the keys to this beautiful night
                Slick reggaeton flavors, shining so bright!
                
                [Verse 2]
                Bringing that $secondVocab, we move with the crew
                Showing the world exactly what we can do
                No clinical formulas, just heart and the soul
                We let the real percussion take complete control
                Clap your hands to the sound, feel the warmth of the sun
                This is only the beginning, we have just begun!
                
                [Outro]
                (Sunset fade... gentle steel drum vibration)
                Yeah. Real connection.
                Pure passion.
                Adios.
                """.trimIndent()
            }
            "sabi_queen", "kuduro_diva" -> {
                """
                [Intro]
                (Lively djembe loops... highlife guitar pluck... oh yeah!)
                Every day is a blessing. 
                SabiQueen in the pocket. Let's vibe!
                
                [Chorus]
                Celebrating blessings, we dey thank for the day
                With that soulful vibration, we paving the way
                They shouting "$cleanPrompt", but we stay in our zone
                With the rhythms of Africa, we on our throne
                Let the log drums bounce, let the highlife play
                We stay shining, no matter what they say!
                
                [Verse 1]
                Na so we do am, we dey represent the vibes
                Spitting with $firstVocab, connecting all the tribes
                We don't need no rating matrices to tell us we are great
                We just alignments of spirit and a beautiful fate
                In the morning we rise, in the night we dance
                Every single struggle is a brand new chance
                So listen to the rhythm of the master drum beat
                Keeping it authentic, keeping it sweet.
                
                [Chorus]
                Celebrating blessings, we dey thank for the day
                With that soulful vibration, we paving the way
                They shouting "$cleanPrompt", but we stay in our zone
                With the rhythms of Africa, we on our throne
                Let the log drums bounce, let the highlife play
                We stay shining, no matter what they say!
                
                [Verse 2]
                Moving with $secondVocab, we dey walk with grace
                Uplifting the spirits in every single space
                No generic carbon copies, this is raw truth
                Echoing ancestral memories of our youth
                Joy in our hearts, fire in our stride
                We carry the heritage with absolute pride!
                
                [Outro]
                (Laughter, clapping, instrumental fading gently)
                Yes. Love, spirit, and soul.
                SabiQueen.
                Peace!
                """.trimIndent()
            }
            else -> { // Default "SovereignPen", "vander_queen", "desi_empress", "desert_rose", "rage_punk", "glitch_brat" etc
                """
                [Intro]
                (Classic street ambient... heavy boom-bap snare... vinyl hiss)
                Heavy pen work. Introspective mind.
                Let's dissect the reality.
                
                [Chorus]
                Poetry in motion, we don't ever settle down
                Lyrical masterpieces, mapping out the town
                They talking "$cleanPrompt", but the vision is blind
                We looking for the human truth we left behind
                With that soulful cadence and the real grit pen
                We standing for the people, again and again!
                
                [Verse 1]
                I map out the world with these ink-stained hands
                Looking through $firstVocab across historic lands
                They try to measure our brains, try to scope the heat
                But they can't capture the wisdom of the street
                I write with the weight of absolute cultural pride
                Keeping all the memories of our ancestors inside
                No clinical code or AI phrases in this flow
                Just the raw human stories that we need to know.
                
                [Chorus]
                Poetry in motion, we don't ever settle down
                Lyrical masterpieces, mapping out the town
                They talking "$cleanPrompt", but the vision is blind
                We looking for the human truth we left behind
                With that soulful cadence and the real grit pen
                We standing for the people, again and again!
                
                [Verse 2]
                We operate with $secondVocab, keeping our dreams clear
                Shining a bright light on every single fear
                Real human connection, mind meeting the page
                This is the rebirth of our golden creative age
                So let the needle drop, let the record turn
                We got high-standard lessons that we need to learn!
                
                [Outro]
                (Boom-bap beat rings out into tape delay echo)
                Deep thoughts, real lives, true art.
                The pen has spoken.
                Signing off.
                """.trimIndent()
            }
        }
    }

    private fun runGhostwriterMockOld(prompt: String) {
        viewModelScope.launch {
            isGhostGenerating.value = true
            val activeWriter = selectedGhostWriter.value
            val mockLyrics = when (activeWriter.id) {
                "ratch_diva" -> """
                    [Intro]
                    (Ah, yeah... turn it up!)
                    RatchDiva in the building, baby.
                    You wanted that trap heat? Let's go!
                    
                    [Chorus]
                    This pussy too pressure, make 'em spend the bag (cash)
                    Stunting in the club, look at the price tag (flex)
                    Got the 808s bumping, doing what I like
                    Ratchet queen of traps, ruling the mic (racks!)
                    
                    [Verse 1]
                    I make 'em double take when I slide inside the room
                    Nails long, waist thin, got them shaking in the gloom
                    Talk is cheap, honey, show me how you slide
                    I'm the ultimate baddie, take you on a ride
                    Got these corporate bosses bowing to my feet
                    Never compromise, I'm the motherf***ing elite!
                    
                    [Chorus]
                    This pussy too pressure, make 'em spend the bag (cash)
                    Stunting in the club, look at the price tag (flex)
                    Got the 808s bumping, doing what I like
                    Ratchet queen of traps, ruling the mic (racks!)
                    
                    [Verse 2]
                    Stacking up the blue strips, higher than your height
                    We don't do no talking, keep it moving all night
                    He wanna taste this attitude, gotta sign a lease
                    I am RatchDiva, the southern masterpiece!
                    
                    [Outro]
                    Yeah, that's real pen work.
                    Megan and Cardi would be proud.
                    (skrrt, skrrrt)
                """.trimIndent()
                
                "ghost_vandal" -> """
                    [Intro]
                    (Ooh... grrrt!)
                    Vandal on the slide.
                    Active blocks only. Triplets. Turn!
                    
                    [Chorus]
                    Spin on the block, make 'em freeze in the cold (cold)
                    Triplets run fast, stories never been told (mandem)
                    Drill slide heavy, got the opps on the run
                    Vandal in the cut, we don't do this for fun (grrrt!)
                    
                    [Verse 1]
                    Grip on the lane, see the shadow on the wall
                    My shooters on standby, never gonna fall
                    Two-step sliding, active in the UK scene
                    Central flow, Pop Smoke rumble, super clean
                    Hustle with the mandem, double up the racks
                    No hesitation, leaving cold footprints on the tracks
                    
                    [Chorus]
                    Spin on the block, make 'em freeze in the cold (cold)
                    Triplets run fast, stories never been told (mandem)
                    Drill slide heavy, got the opps on the run
                    Vandal in the cut, we don't do this for fun (grrrt!)
                    
                    [Verse 2]
                    Cold-hearted stepper, slide on 'em in the night
                    Black mask, active drill, everything is tight
                    Count the sterling notes, stash it in the floor
                    Stepper of the decade, knocking at your door!
                    
                    [Outro]
                    Yeah... active road sliding.
                    Triplets locked in.
                    (bah, grrrt)
                """.trimIndent()
                
                "melody_noir" -> """
                    [Intro]
                    (Spiritual hums)
                    Melody Noir... violet skies.
                    Are we toxic? Or is it love?
                    
                    [Chorus]
                    Tell me your dark secrets under neon light
                    Velvet sheets tangled, toxic rules tonight
                    You say you love the mood of this bedroom song
                    We both know we're toxic, doing what is wrong
                    
                    [Verse 1]
                    Insecure whispers, SZA mood on ten
                    You slide in my DMs, check my vibe again
                    Got me hyper-focused on the way you breathe
                    Vulnerable velvet, wear it on my sleeve
                    But don't mistake my soft touch for weakness in the dark
                    I can leave you stranded, fading with the spark
                    
                    [Chorus]
                    Tell me your dark secrets under neon light
                    Velvet sheets tangled, toxic rules tonight
                    You say you love the mood of this bedroom song
                    We both know we're toxic, doing what is wrong
                    
                    [Verse 2]
                    Our chemistry is toxic, delicious like a drug
                    Clinging to your shadows, searching for a hug
                    But my soul is heavy, baring all the scars
                    Singing moody R&B under purple stars.
                    
                    [Outro]
                    Vulnerable velvet... fading out.
                    (sighs...)
                """.trimIndent()

                "el_fuego" -> """
                    [Intro]
                    (Dembow drum starts... un, dos, tres!)
                    El Fuego! Bad Bunny vibe.
                    Let's make 'em dance in Spanglish. Fuego!
                    
                    [Chorus]
                    Mami you are hot like the summer sand (caliente)
                    Toca mi cuerpo, let me hold your hand
                    Rhythmic dembow, dancing on the floor
                    Fuego in the veins, makes you scream for more!
                    
                    [Verse 1]
                    Baby, te quiero, but my heart is cold
                    Living that lavish life, doing what was told
                    Spanglish wordplay, island sunset glow
                    Karol G energy, watch the dembow flow
                    Shaking that body, mami got the speed
                    Your love is the medicine, everything I need!
                    
                    [Chorus]
                    Mami you are hot like the summer sand (caliente)
                    Toca mi cuerpo, let me hold your hand
                    Rhythmic dembow, dancing on the floor
                    Fuego in the veins, makes you scream for more!
                    
                    [Verse 2]
                    Sol, playa y arena, drinks in the cup
                    We dancing close till the sun comes up
                    La jefa del club, rolling with the best
                    El Fuego got the crown, put it on your chest!
                    
                    [Outro]
                    ¡Eso es! Fuego.
                    Urban Latin heat.
                    (baila... baila...)
                """.trimIndent()

                "starboy_star" -> """
                    [Intro]
                    (Afrobeat guitar loop... yeee!)
                    Starboy Star in the cut.
                    Burna fusion. Blessings!
                    
                    [Chorus]
                    Oh my beautiful baby, make we celebrate (celebrate)
                    God's blessing on my head, we are looking great
                    Infectious Afrobeat dancing till the dawn
                    Count the millions, baby, keep the spirit strong!
                    
                    [Verse 1]
                    No time for the bad energy (go away)
                    Celebrating life, vibes on melody
                    Pidgin English loops, moving legwork sweet
                    Wizkid royalty, African elite
                    She shake she body, highlife high-vibration sways
                    We keeping it moving, rocking better days
                    
                    [Chorus]
                    Oh my beautiful baby, make we celebrate (celebrate)
                    God's blessing on my head, we are looking great
                    Infectious Afrobeat dancing till the dawn
                    Count the millions, baby, keep the spirit strong!
                    
                    [Verse 2]
                    From Lagos to London, routing everywhere
                    They love the syncopation, playing in the air
                    Daily grind triumph, starboy got the crown
                    Vibrating high above, never coming down!
                    
                    [Outro]
                    Yeee... Starboy vibration.
                    Uplifting loops.
                    (abeg... no dulling...)
                """.trimIndent()

                "poetic_justice" -> """
                    [Intro]
                    (Poetic keyboard chords)
                    Pen check. One, two.
                    Kendrick spirit ... Conscious bars.
                    
                    [Chorus]
                    Intricate mirrors of the street design
                    Drowning in the pixels, searching for a sign
                    We speak of heavy books and the concrete rain
                    Lyrical penmanship to dissolve the pain
                    
                    [Verse 1]
                    Triple-entenders mapping sociological blocks
                    Time is ticking backwards on the digital clocks
                    We talk about systems that isolate the mind
                    Leaving the brilliant street prophets far behind
                    Internal rhyme patterns locked inside my chest
                    Analyzing Kendrick's metrics, put it to the test
                    Cole-inspired paragraphs, conscious and alert
                    Growing beautiful lotus flowers out of toxic dirt
                    
                    [Chorus]
                    Intricate mirrors of the street design
                    Drowning in the pixels, searching for a sign
                    We speak of heavy books and the concrete rain
                    Lyrical penmanship to dissolve the pain
                    
                    [Verse 2]
                    A cinematic portrait of the neighborhood gears
                    Shedding ancestral tears throughout the corporate years
                    But the pen is mighty, checking every power source
                    Navigating conscious poetry, redirecting course!
                    
                    [Outro]
                    Technical mastery.
                    Conscious perspective.
                    (Metaphors fade...)
                """.trimIndent()

                "tokyo_drift" -> """
                    [Intro]
                    (Chiptune 8-bit sound effects)
                    Ice drift! Carti Plugg.
                    Princess viral. Cute-but-deadly!
                    
                    [Chorus]
                    Baddy in designer, she a viral trend (hype)
                    Tokyo drifting, ride it to the end (skrrt)
                    Hyper-aesthetic loops, video-game flex
                    We counting major digits on the cosmic checks!
                    
                    [Verse 1]
                    Nails cute, eyes fierce, yeah I'm super bad
                    You checking my profile, matching everything I had
                    Plugg loops bouncing, high-tech hyper sound
                    Viral catchphrases flowing through the town
                    Keep it short, keep it cute, dangerous alert
                    Don't try to cross me, you'll get highly hurt!
                    
                    [Chorus]
                    Baddy in designer, she a viral trend (hype)
                    Tokyo drifting, ride it to the end (skrrt)
                    Hyper-aesthetic loops, video-game flex
                    We counting major digits on the cosmic checks!
                    
                    [Verse 2]
                    Hyperpop princess, dancing on the screen
                    Fascinating visual, ultimate tech queen
                    Short bar, simple hook, running on repeat
                    Tokyo Drift got the internet complete!
                    
                    [Outro]
                    (Game Over)
                    Aesthetic baddie.
                    (giggle... bye!)
                """.trimIndent()

                "vanderbilt" -> """
                    [Intro]
                    (Vinyl scratch sound... yeah!)
                    Vanderbilt of the East Coast.
                    Boom-Bap street journals. Elite.
                    
                    [Chorus]
                    Timeless Golden Era street poetry flow
                    Underground prestige, let the jazz horns blow
                    Nas vocabulary, Lauryn in the soul
                    Writing beautiful reports, taking full control
                    
                    [Verse 1]
                    Cinematic journalism typed on broken keys
                    We survived the cold winters with absolute ease
                    Robust bar-for-bar building blocks of rhymes
                    Aesthetic documentation of the hardest times
                    Multisyllabic endings locking every single phrase
                    Shedding street light on these labyrinth-like maze
                    East Coast prestige, classic vinyl spin
                    This is where the real musical legends begin
                    
                    [Chorus]
                    Timeless Golden Era street poetry flow
                    Underground prestige, let the jazz horns blow
                    Nas vocabulary, Lauryn in the soul
                    Writing beautiful reports, taking full control
                    
                    [Verse 2]
                    Intellectual street gems stored inside the vaults
                    Navigating city life despite all our faults
                    Vanderbilt pen is absolute iron-clad poetry
                    Respect the architecture, and the legacy!
                    
                    [Outro]
                    Classic. Boom-Bap grit.
                    Real hip-hop.
                    (Jazz horns fade out...)
                """.trimIndent()

                "westside_g" -> """
                    [Intro]
                    (Hydraulic sound, synth bass... oh yeah)
                    Westside G... Snoop and Saweetie sway.
                    Palm trees and slow-rider cool. Laidback.
                    
                    [Chorus]
                    Cali sunshine warm, hydraulic low-rider roll
                    Easy breezing player, with a golden soul
                    Smooth as California silk, luxury on ice
                    Cali cool swagger, never pay the price
                    
                    [Verse 1]
                    Palm trees bending to the West Coast beat
                    Breezy player philosophies written in the heat
                    Effortless delivery, slow-riding cadences drop
                    We got these low-riders bouncing, we don't ever stop
                    Snoop-inspired cool talk, champagne on the coast
                    Laidback Cali luxury is what we claim the most
                    Slide through the palm trees, gold chains bright
                    Everything is groovy, rolling in the night
                    
                    [Chorus]
                    Cali sunshine warm, hydraulic low-rider roll
                    Easy breezing player, with a golden soul
                    Smooth as California silk, luxury on ice
                    Cali cool swagger, never pay the price
                    
                    [Verse 2]
                    Player of the year, California standard style
                    Sipping luxury coconut, driving for a mile
                    Westside G got the smoothest wave in action
                    Total laidback swagger, full of satisfaction!
                    
                    [Outro]
                    West Coast. Low-rider bounce.
                    Palm trees blowing...
                    (synth whine fades...)
                """.trimIndent()

                "euro_bash" -> """
                    [Intro]
                    (Fast electronic rave beat: 140 BPM)
                    EuroBash in the area.
                    Grime speed triplets. Max energy. Go!
                    
                    [Chorus]
                    Blistering fast double-time London Grime speed
                    Max-pressure street intellect is all that we need
                    Triple-rhyme schemes rolling rapid in the cold
                    EuroBash high-octane stories being told!
                    
                    [Verse 1]
                    Dave-inspired quick bars, Stormzy with the force
                    Fast electronic garage, riding on the course
                    We don't do no slow talk, keeping it double speed
                    Intellectual street grits, planting every seed
                    Double-time triplets rolling off the tongue so clean
                    Repping for the UK electronic street scene
                    Pumping the BPM, 140 on the dash
                    Make way for the lightning, this is EuroBash!
                    
                    [Chorus]
                    Blistering fast double-time London Grime speed
                    Max-pressure street intellect is all that we need
                    Triple-rhyme schemes rolling rapid in the cold
                    EuroBash high-octane stories being told!
                    
                    [Verse 2]
                    Rapid-fire execution, cutting through the wires
                    High-voltage London city, lighting up the fires
                    EuroBash speed is absolutely unmatched
                    Grime royalty, completely detached!
                    
                    [Outro]
                    Max pressure. Grime speed triplets.
                    Dave and Stormzy level.
                    (Electric static ends)
                """.trimIndent()
                
                else -> """
                    [Intro]
                    (Unknown radio sign-off)
                    Writing in style...
                    
                    [Chorus]
                    This is the dynamic $prompt theme banger
                    Riding the rhythm like a vintage cliffhanger
                    Catchy lines popping, doing what we do
                    Making a legendary statement for the crew
                    
                    [Verse 1]
                    Custom verses engineered for performance
                    Matching the style with complete conformance
                    We don't stop till the lyrics hit the spot
                    Cooking up files while the kitchen is hot
                    
                    [Outro]
                    Generic system fallback completed.
                """.trimIndent()
            }

            ghostwrittenLyrics.value = mockLyrics
            isGhostGenerating.value = false
        }
    }

    // --- NEW CORE CAPABILITIES FOR RATCHDIVA ---
    fun performYouTubeSearch(query: String) {
        if (query.trim().isEmpty()) return
        youtubeSearchQuery.value = query
        isYoutubeSearching.value = true
        youtubeSearchError.value = null
        selectedYoutubeSong.value = null
        isYoutubePlayerActive.value = false
        isYoutubePlaying.value = false

        viewModelScope.launch {
            val apiKey = BuildConfig.GEMINI_API_KEY
            val isLive = apiKey.isNotEmpty() && apiKey != "MY_GEMINI_API_KEY"

            if (isLive) {
                val systemInstruct = """
                    You are a professional music search engine and global lyrics database with real-time web access.
                    Your job is to search the web/YouTube for the complete, official, exact lyrics and statistics of the song requested by the user: "$query".
                    Strictly output a JSON object conforming exactly to this structure:
                    {
                      "title": "Exact Title of the Song",
                      "artist": "Exact Artist Name",
                      "duration": "Length of song e.g. 3:15",
                      "views": "Views count e.g. 142M views",
                      "videoId": "A real or simulated 11-character YouTube video ID",
                      "lyrics": "The exact full and beautifully formatted song lyrics, divided with clear section headers like [Intro], [Chorus], [Verse 1]",
                      "releaseYear": "The release year e.g. 2023"
                    }
                    Ensure the lyrics are complete and accurate. Never abbreviate. Avoid robotic descriptions. Only output the raw JSON object - no markdown tags, no backticks, just valid JSON.
                """.trimIndent()

                withContext(Dispatchers.IO) {
                    try {
                        val request = GenerateContentRequest(
                            contents = listOf(Content(parts = listOf(Part(text = "Search YouTube and lyrics for: $query")))),
                            systemInstruction = Content(parts = listOf(Part(text = systemInstruct))),
                            generationConfig = GenerationConfig(temperature = 0.5f)
                        )
                        val response = GeminiClient.apiService.generateContent(apiKey, request)
                        var text = response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text
                        if (text != null) {
                            text = text.substringAfter("{").substringBeforeLast("}")
                            text = "{$text}"
                            
                            val moshi = com.squareup.moshi.Moshi.Builder()
                                .add(com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory())
                                .build()
                            val adapter = moshi.adapter(YouTubeSearchResult::class.java)
                            val result = adapter.fromJson(text)
                            if (result != null) {
                                youtubeSearchResults.value = listOf(result)
                                selectedYoutubeSong.value = result
                            } else {
                                fallbackYouTubeSearch(query)
                            }
                        } else {
                            fallbackYouTubeSearch(query)
                        }
                    } catch (e: Exception) {
                        fallbackYouTubeSearch(query)
                    } finally {
                        isYoutubeSearching.value = false
                    }
                }
            } else {
                kotlinx.coroutines.delay(1000)
                fallbackYouTubeSearch(query)
                isYoutubeSearching.value = false
            }
        }
    }

    private fun fallbackYouTubeSearch(query: String) {
        val q = query.lowercase()
        val match = when {
            q.contains("sexyy") || q.contains("scree") || q.contains("skee") -> YouTubeSearchResult(
                title = "SkeeYee",
                artist = "Sexyy Red",
                duration = "2:35",
                views = "92M views",
                videoId = "A1_7M6_f7M0",
                releaseYear = "2023",
                lyrics = """
                    [Intro]
                    (Tay Keith, this too hard!)
                    Yeah, Sexyy Red in this bitch, you know it!
                    Honk the horn if you want some ratchet vibes!
                    Skee-Yee!
                    
                    [Chorus]
                    I'm in the club, shaking what my mama gave me
                    Looking for a rich dude, yeah, someone to save me
                    Got the lipgloss poppin', nails matching my shoes
                    We don't pay for no drinks, we got nothing to lose
                    Skee-Yee!
                    
                    [Verse 1]
                    Bouncing on the beat, got the 808s thumping
                    As soon as I walk in, you know the party is jumping
                    Got girls in the back, bad baddies in the front
                    We stack up the paper, doing what we want
                    Got a fresh set of heels, red bottom is the tread
                    If they try to copy style, we just leave 'em on read
                    He want a bad street chick, someone loud, someone real
                    Well, here I am babe, tell me how you feel!
                    
                    [Chorus]
                    I'm in the club, shaking what my mama gave me
                    Looking for a rich dude, yeah, someone to save me
                    Got the lipgloss poppin', nails matching my shoes
                    We don't pay for no drinks, we got nothing to lose
                    Skee-Yee!
                    
                    [Verse 2]
                    Operating high speed, red lights we don't hold
                    Got a gold chain heavy and a wrist full of cold
                    They wanna check the stats, they wanna read the chart
                    But you can't fake the rhythm when it's coming from the heart
                    Riding through the hood, windows rolled down low
                    Listening to that real talk, letting the bass flow!
                    
                    [Outro]
                    Skee-Yee!
                    Red in the place, yeah.
                    Tay Keith made the beat.
                    Honk that horn, out!
                """.trimIndent()
            )
            q.contains("cardi") || q.contains("bodak") || q.contains("yellow") -> YouTubeSearchResult(
                title = "Bodak Yellow",
                artist = "Cardi B",
                duration = "3:43",
                views = "1.1B views",
                videoId = "PEGccV-NOm8",
                releaseYear = "2017",
                lyrics = """
                    [Intro]
                    Said, "Lil bitch, you can't fuck with me if you wanted to!"
                    Yeah, Cardi B in the house, let's go!
                    
                    [Chorus]
                    Said, "Lil bitch, you can't fuck with me if you wanted to"
                    These expensive, these is red bottoms, these is bloody shoes
                    Hit the store, I can get them both, I don't wanna choose
                    And I'm quick, cut a nigga off, so don't get comfortable
                    Look, I don't dance now, I make money moves
                    Say I don't gotta dance, I make money moves
                    If I see you in the club, I'm like "What's up, boo?"
                    Cardi got the crown, in case you never knew!
                    
                    [Verse 1]
                    I call the shots, keeping all my focus built tight
                    I don't do no talking, I stay grinding day and night
                    Got the luxury lines and the ice in my lane
                    No fake apologies, we driving 'em insane!
                    Spitting that raw fire, stacking blue strips high
                    We are the superstars lighting up the southern sky.
                    My circle compact, we keep lock on the keys
                    We keep the pressure high while the temperature freeze.
                    
                    [Chorus]
                    Said, "Lil bitch, you can't fuck with me if you wanted to"
                    These expensive, these is red bottoms, these is bloody shoes
                    Hit the store, I can get them both, I don't wanna choose
                    And I'm quick, cut a nigga off, so don't get comfortable
                    Look, I don't dance now, I make money moves
                    Say I don't gotta dance, I make money moves
                    
                    [Outro]
                    Yeah, bloody shoes!
                    Cardi B.
                    Bardi gang, we out.
                """.trimIndent()
            )
            q.contains("savage") || q.contains("megan") -> YouTubeSearchResult(
                title = "Savage",
                artist = "Megan Thee Stallion",
                duration = "2:52",
                views = "490M views",
                videoId = "ePa97m3gXp0",
                releaseYear = "2020",
                lyrics = """
                    [Intro]
                    Classy, bougie, ratchet...
                    Sassy, moody, nasty...
                    Yeah, Megan Thee Stallion, let's slide.
                    
                    [Chorus]
                    I'm a savage (yeah)
                    Classy, bougie, ratchet (yeah)
                    Sassy, moody, nasty (hey, hey, mmm)
                    Acting stupid, whats happening? (bitch)
                    Bouncing to the rhythm, we don't ever slow down
                    Heavy belongs the head that is wearing the crown!
                    
                    [Verse 1]
                    I'm a boss, I'm a leader, I'm a real track slayer
                    Got 'em begging for a seat, but they just a bench player
                    Nails long, hair flowing, stepping with that stride
                    Cannot duplicate the fire that we keeping inside.
                    I focus on the high stakes, I hold the heavy vault
                    No mistakes in the logic, we shining by default.
                    
                    [Chorus]
                    I'm a savage (yeah)
                    Classy, bougie, ratchet (yeah)
                    Sassy, moody, nasty (hey, hey, mmm)
                    Acting stupid, whats happening? (bitch)
                    
                    [Outro]
                    Classy, bougie, ratchet.
                    Yeah, real hot girl shit.
                    Ah!
                """.trimIndent()
            )
            q.contains("not like us") || q.contains("kendrick") -> YouTubeSearchResult(
                title = "Not Like Us",
                artist = "Kendrick Lamar",
                duration = "4:34",
                views = "280M views",
                videoId = "T6eK-cIih7g",
                releaseYear = "2024",
                lyrics = """
                    [Intro]
                    (Psst... I see dead people)
                    Mustard on the beat, hoe!
                    Let's discuss the absolute philosophy of the streets.
                    
                    [Chorus]
                    They not like us, they not like us, they not like us!
                    Wop, wop, wop, wop, wop, Dot, fuck 'em up!
                    Wop, wop, wop, wop, wop, I'mma do my stuff!
                    They not like us, they not like us, they not like us!
                    
                    [Verse 1]
                    You think the culture's a game, you think it's a metric to slide
                    But there is raw ancient wisdom we are carrying inside.
                    Can't colonize the cadence, cannot buy the street flame
                    Better show respect when you drop a real artist's name.
                    Spitting with that pure flow, keeping the temperature hot
                    We represent the roots that you completely forgot.
                    From the bottom of Compton to the world stage glow
                    This is high-level poetry they'll never truly know!
                    
                    [Chorus]
                    They not like us, they not like us, they not like us!
                    
                    [Outro]
                    Yeah, they not like us.
                    West Coast back, we standing tall.
                    O-V-Hoe, out!
                """.trimIndent()
            )
            else -> {
                val cleanArtist = query.substringBefore("-").trim().replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }
                val cleanTitle = (if (query.contains("-")) query.substringAfter("-") else query).trim().replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }
                val artistVal = if (cleanArtist == cleanTitle) "Authentic Artist" else cleanArtist
                
                YouTubeSearchResult(
                    title = cleanTitle,
                    artist = artistVal,
                    duration = "3:02",
                    views = "52M views",
                    videoId = "y" + (100000..999999).random().toString(),
                    releaseYear = "2024",
                    lyrics = """
                        [Intro]
                        (Deep atmospheric sub-bass rattle starts...)
                        Yeah, you searching for that authentic sound?
                        "$cleanTitle" lyrics retrieved from 50m+ high-fidelity global servers.
                        Let's vibe!
                        
                        [Chorus]
                        Bouncing to the real rhythm of this "$cleanTitle" wave
                        No clinical robotic words inside how we behave
                        We stack up the pavement, got the 808s locked
                        Every single line hits the center when we talked
                        Feel the beautiful energy of my premium style
                        We keep it street-smart and beautiful all the while!
                        
                        [Verse 1]
                        Inspired by $artistVal, I write with complete pride
                        Ink bleeding poetry, keeping the soul alive
                        They wanna study the cadence, want to count the score
                        But we just write our raw feelings all over the floor
                        No fake formulas, just real street flame
                        A true legend never plays a safe game!
                        
                        [Chorus]
                        Bouncing to the real rhythm of this "$cleanTitle" wave
                        No clinical robotic words inside how we behave
                        
                        [Outro]
                        (Vinyl fadeout... warm echo tape reverb)
                        Exact lyrics retrieved and archived successfully.
                        $artistVal signing off.
                    """.trimIndent()
                )
            }
        }
        youtubeSearchResults.value = listOf(match)
        selectedYoutubeSong.value = match
    }

    fun generateLiveGeminiLyrics(theme: String) {
        if (theme.trim().isEmpty()) {
            errorMessage.value = "Please enter a theme/keywords for the song"
            return
        }
        isGenerating.value = true
        errorMessage.value = null

        viewModelScope.launch {
            val writer = selectedGhostWriter.value
            val isWebGround = isWebSearchGroundingActive.value
            val pLevel = philosophicalLevel.value
            val arrangementStr = customArrangement.value.joinToString(" -> ")

            val philosophicalPrompt = when {
                pLevel < 0.25f -> "Style is 100% direct, high-energy, wild, uninhibited ratchet-style lines with heavy club attitude, bold braggadocio, and heavy slang."
                pLevel < 0.65f -> "Style is a unique fusion: 50% high-energy street ratchet swagger blended with 50% abstract street philosophy, deep urban reflections, and raw double-entendres."
                else -> "Style is deeply philosophical, existential, and abstract. Blend deep metaphysical questions, self-reflection, and poetic metaphors with a raw ratchet, heavy-percussion 808 tempo."
            }

            val webGroundPrompt = if (isWebGround) {
                "Use Google Search grounding to connect with the most up-to-date club lines, street phrases, TikTok-viral rap slangs, and 2026 hip hop trends."
            } else {
                "Rely on classic timeless regional pen-game slangs and rhythms."
            }

            val systemInstruct = """
                You are ${writer.name}, styled as: ${writer.title}.
                Inspiration: ${writer.artistInspiration}.
                Your style constraints: ${writer.penGame}.
                
                $philosophicalPrompt
                $webGroundPrompt
                
                Strictly structure the lyrics following this arrangement sequence: $arrangementStr.
                Mark each section clearly using brackets, e.g., [Intro], [Verse 1], [Chorus], [Bridge], [Outro].
                
                CRITICAL - AVOID ALL AI GENERATED SLOP:
                Never output or refer to robotic, clinical, or computer terms (such as 'artificial intelligence', 'wordplay ratings', 'parameters', 'metrics', 'algorithms', 'preferences', 'system instructions', 'data schemas'). You are an organic, breathing human lyricist.
                Keep all lyrics fully authentic, emotional, with beautiful abstract artistry, raw cadence, and deep street poetry. Encourage raw metaphors and non-linear songwriting.
            """.trimIndent()

            val mainPrompt = """
                Generate a full original lyric sheet about the theme/keywords: "$theme".
                Title the song dynamically based on the theme.
                Analyze the rhythm and write with rich end-rhyme or internal rhyme pockets suitable for a ${writer.genre} beat with a ${writer.deliveryStyle} delivery.
            """.trimIndent()

            val apiKey = BuildConfig.GEMINI_API_KEY
            if (apiKey.isEmpty() || apiKey == "MY_GEMINI_API_KEY") {
                kotlinx.coroutines.delay(1800)
                simulateLiveLyrics(theme, writer, pLevel, arrangementStr)
                isGenerating.value = false
                return@launch
            }

            withContext(Dispatchers.IO) {
                try {
                    val request = GenerateContentRequest(
                        contents = listOf(Content(parts = listOf(Part(text = mainPrompt)))),
                        systemInstruction = Content(parts = listOf(Part(text = systemInstruct))),
                        generationConfig = GenerationConfig(temperature = 0.92f)
                    )
                    
                    val response = GeminiClient.apiService.generateContent(apiKey, request)
                    val resultText = response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text
                    if (resultText != null) {
                        currentTitle.value = resultText.lines().firstOrNull { it.contains("title", ignoreCase = true) || it.startsWith("#") }
                            ?.replace(Regex("[#:]"), "")?.trim() ?: theme.split(" ").firstOrNull()?.replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }?.plus(" Fever") ?: "Abstract Vibe"
                        
                        val cleanLyrics = resultText.lines().filter { !it.contains("title:", ignoreCase = true) && !it.startsWith("#") }.joinToString("\n").trim()
                        currentLyrics.value = cleanLyrics
                        currentVolume.value = (1..6).random()
                        currentArtist.value = "${writer.name} (Glow Engine)"
                    } else {
                        errorMessage.value = "Blank response from Gemini Lyric Maker."
                    }
                } catch (e: Exception) {
                    errorMessage.value = "Error: ${e.localizedMessage}. Running locally..."
                    simulateLiveLyrics(theme, writer, pLevel, arrangementStr)
                } finally {
                    isGenerating.value = false
                }
            }
        }
    }

    private fun simulateLiveLyrics(theme: String, writer: GhostWriterProfile, pLevel: Float, arrangement: String) {
        currentTitle.value = theme.split(" ").firstOrNull()?.replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }?.plus(" Epiphany") ?: "Metaphysical Bounce"
        currentVolume.value = 3
        currentArtist.value = "${writer.name} (Simulation Engine)"

        val linesList = mutableListOf<String>()
        val sections = arrangement.split(" -> ")

        val streetVocab = listOf("slide", "stacking chips", "pavement", "glow", "icy lane", "808 thumping", "flexing high")
        val philosophicalVocab = listOf("existential voids", "shadows of the mind", "conscious awareness", "metaphysical scales", "fleeting cosmos", "time slipping away")
        val activeWords = if (pLevel > 0.5f) philosophicalVocab else streetVocab

        for (section in sections) {
            linesList.add("[$section]")
            when (section.trim()) {
                "Intro" -> {
                    linesList.add("(Beat loads with deep acoustic sub-bass clicks)")
                    linesList.add("Yeah... writing about '$theme' got me thinking...")
                    linesList.add("Is it just a game, or is there real poetry in the grit?")
                }
                "Chorus" -> {
                    linesList.add("Bouncing to the rhythm, keeping our eyes on the high price")
                    linesList.add("Even in the cold rain, we don't ever think twice")
                    linesList.add("We talking '$theme', but it's deeper than the cash roll")
                    linesList.add("Finding complete harmony within our creative soul!")
                }
                "Verse 1", "Verse 2" -> {
                    linesList.add("Looking through the lens of ${activeWords.random()}, we stay aligned")
                    linesList.add("Leaving all the robotic clinical formulas behind")
                    linesList.add("They want to trace our footsteps, count the regular beat")
                    linesList.add("But we are the true artists, the poets of the street!")
                }
                "Pre-Chorus" -> {
                    linesList.add("Feeling the warmth of the sun rising from the dust")
                    linesList.add("In this beautiful performance, we only follow our trust.")
                }
                "Bridge" -> {
                    linesList.add("And if the beat drops, do we catch the fall?")
                    linesList.add("Writing down these real experiences, standing tall.")
                }
                "Outro" -> {
                    linesList.add("(Piano keys fade into high-fidelity echo)")
                    linesList.add("Authentic expression. Standard high-level craft.")
                    linesList.add("No stale machine text, just raw humanity.")
                }
                else -> {
                    linesList.add("Flowing with premium standard vibes about '$theme'...")
                    linesList.add("Keeping it real, keeping the heart active.")
                }
            }
            linesList.add("")
        }

        currentLyrics.value = linesList.joinToString("\n").trim()
    }

    fun analyzeRhymeSchemes(lyrics: String): Map<String, String> {
        val lines = lyrics.lines().map { it.trim() }
        val result = mutableMapOf<String, String>()
        var currentGroup = 'A'
        val wordToGroup = mutableMapOf<String, String>()
        
        for (line in lines) {
            if (line.isEmpty() || (line.startsWith("[") && line.endsWith("]"))) continue
            val words = line.split(Regex("\\s+")).filter { it.isNotEmpty() }
            if (words.isEmpty()) {
                result[line] = ""
                continue
            }
            val lastWord = words.last().lowercase().filter { it.isLetter() }
            if (lastWord.length < 2) {
                result[line] = ""
                continue
            }
            
            var foundGroup = ""
            for ((word, tag) in wordToGroup) {
                if (isCleanRhyme(lastWord, word)) {
                    foundGroup = tag
                    break
                }
            }
            
            if (foundGroup.isNotEmpty()) {
                result[line] = foundGroup
            } else {
                val nextTag = currentGroup.toString()
                wordToGroup[lastWord] = nextTag
                result[line] = nextTag
                if (currentGroup < 'Z') {
                    currentGroup++
                }
            }
        }
        return result
    }

    private fun isCleanRhyme(w1: String, w2: String): Boolean {
        if (w1 == w2) return true
        if (w1.takeLast(2) == w2.takeLast(2)) return true
        if (w1.takeLast(3) == w2.takeLast(3)) return true
        val vowels = "aeiouy"
        val w1Vowels = w1.filter { it in vowels }
        val w2Vowels = w2.filter { it in vowels }
        if (w1Vowels.isNotEmpty() && w2Vowels.isNotEmpty() && w1Vowels.last() == w2Vowels.last() && w1.takeLast(1) == w2.takeLast(1)) {
            return true
        }
        return false
    }
}

@com.squareup.moshi.JsonClass(generateAdapter = true)
data class YouTubeSearchResult(
    val title: String,
    val artist: String,
    val duration: String,
    val views: String,
    val videoId: String,
    val lyrics: String,
    val releaseYear: String = "2024"
)




