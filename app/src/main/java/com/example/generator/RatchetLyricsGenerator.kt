package com.example.generator

import com.example.data.LyricKit

object RatchetLyricsGenerator {

    private fun pick(category: String, kit: LyricKit): String {
        val list = when (category) {
            "address" -> kit.getAddressList()
            "self" -> kit.getSelfList()
            "actions" -> kit.getActionList()
            "adjectives" -> kit.getAdjectiveList()
            "body" -> kit.getBodyList()
            "vibe" -> kit.getVibeList()
            "hooks" -> kit.getHookList()
            "adlibs" -> kit.getAdlibList()
            else -> emptyList()
        }
        return if (list.isNotEmpty()) list.random() else "freak"
    }

    fun makeRatchetLine(kit: LyricKit): String {
        val templates = listOf(
            { "${pick("address", kit)}, ${pick("actions", kit)}." },
            { "${pick("self", kit)} the best you ever had." },
            { "This ${pick("body", kit)} so ${pick("adjectives", kit)} for you." },
            { "Tell me how you like it, ${pick("address", kit)}." },
            { pick("vibe", kit) },
            { "Come and ${pick("actions", kit)}, no games." },
            { "${pick("actions", kit)} — I'm that ${pick("adjectives", kit)} bitch." },
            { "I'm a ${pick("adjectives", kit)} freak, ${pick("address", kit)}, you know it." },
            { "Once you get this ${pick("body", kit)}, you ain't leaving." }
        )
        val line = templates.random()()
        return line.replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }
    }

    fun simpleRhymeLine(prevLine: String, kit: LyricKit): String {
        val words = prevLine.split("\\s+".toRegex())
        val lastWord = if (words.isNotEmpty()) {
            words.last().lowercase().filter { it.isLetter() }
        } else {
            ""
        }

        val rhymeMap = mapOf(
            "dick" to listOf("thick", "slick", "trick", "kick"),
            "ass" to listOf("sass", "class", "last", "fast"),
            "pussy" to listOf("pushy", "bushy", "cushy", "woosy"),
            "freak" to listOf("weak", "seek", "peak", "sneak"),
            "bitch" to listOf("rich", "switch", "witch", "glitch"),
            "me" to listOf("see", "be", "free", "please", "tease"),
            "you" to listOf("do", "true", "through", "boo", "crew"),
            "mine" to listOf("fine", "line", "dine", "shine", "9"),
            "good" to listOf("hood", "could", "should", "wood"),
            "love" to listOf("shove", "above", "dove", "glove"),
            "ride" to listOf("slide", "inside", "hide", "tide"),
            "wet" to listOf("set", "let", "sweat", "threat"),
            "hard" to listOf("card", "guard", "regard", "charred"),
            "shit" to listOf("fit", "hit", "lit", "split"),
            "hoe" to listOf("go", "low", "flow", "show", "blow")
        )

        val possibleRhymes = rhymeMap[lastWord] ?: listOf("you", "true", "new")
        val rhymeWord = possibleRhymes.random()

        val fillerTemplates = listOf(
            { "${pick("address", kit)}, I'm so $rhymeWord." },
            { "Ain't no $rhymeWord like this." },
            { "Keep it $rhymeWord, that's the rule." },
            { "And you love it when I'm $rhymeWord." },
            { "Yeah, that shit too $rhymeWord." },
            { "That's why you call me $rhymeWord." }
        )
        val line = fillerTemplates.random()()
        return line.replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }
    }

    fun generateRatchetSong(kit: LyricKit, artist: String = "Ratch Diva", title: String = "Wet Wet Anthem"): String {
        val lyricsBuilder = StringBuilder()

        // 1. Section: Intro (Spoken/Intro)
        lyricsBuilder.append("[Intro]\n")
        val intro = listOf(
            "(Beat drops) … Aye, turn this shit up…",
            "(Moan) … Mmm… this for my freak…",
            "(Spoken) … Yeah… let's get ratchet…"
        ).random()
        lyricsBuilder.append(intro).append("\n\n")

        // 2. Section: Verse 1 (4 lines, AABB rhyme)
        lyricsBuilder.append("[Verse 1]\n")
        val v1l1 = makeRatchetLine(kit)
        val v1l2 = simpleRhymeLine(v1l1, kit)
        val v1l3 = makeRatchetLine(kit)
        val v1l4 = simpleRhymeLine(v1l3, kit)
        lyricsBuilder.append(v1l1).append("\n")
        lyricsBuilder.append(v1l2).append("\n")
        lyricsBuilder.append(v1l3).append("\n")
        lyricsBuilder.append(v1l4).append("\n\n")

        // 3. Section: Pre-Chorus (building tension)
        lyricsBuilder.append("[Pre-Chorus]\n")
        val prePool = listOf(
            "I'm already ${pick("adjectives", kit)}, can you feel it?",
            "${pick("vibe", kit)} — no lie.",
            "Your ${pick("body", kit)} got me ${pick("adjectives", kit)}.",
            "Can't front, I'm ${pick("adjectives", kit)} for you.",
            "And you know ${pick("self", kit)} is all you need."
        )
        val pc1 = prePool.random()
        var pc2 = prePool.random()
        while (pc2 == pc1 && prePool.size > 1) {
            pc2 = prePool.random()
        }
        lyricsBuilder.append(pc1).append("\n")
        lyricsBuilder.append(pc2).append("\n\n")

        // 4. Section: Chorus (Repeatable, catchy)
        lyricsBuilder.append("[Chorus]\n")
        val hooksPool = kit.getHookList()
        val chorusHooks = if (hooksPool.size >= 4) {
            hooksPool.shuffled().take(4)
        } else {
            listOf(
                "this pussy so good, make you lose your mind",
                "I'm the baddest bitch you ever had",
                "dick so good I can't walk right",
                "you ain't never had a freak like me"
            ).shuffled()
        }
        chorusHooks.forEachIndexed { i, hookLine ->
            val adlib = if (i == 0) "${pick("adlibs", kit)} " else ""
            val formatted = if (adlib.isNotEmpty()) "$adlib $hookLine" else hookLine
            lyricsBuilder.append(formatted.replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }).append("\n")
        }
        lyricsBuilder.append("\n")

        // 5. Section: Verse 2
        lyricsBuilder.append("[Verse 2]\n")
        val v2l1 = makeRatchetLine(kit)
        val v2l2 = simpleRhymeLine(v2l1, kit)
        val v2l3 = makeRatchetLine(kit)
        val v2l4 = simpleRhymeLine(v2l3, kit)
        lyricsBuilder.append(v2l1).append("\n")
        lyricsBuilder.append(v2l2).append("\n")
        lyricsBuilder.append(v2l3).append("\n")
        lyricsBuilder.append(v2l4).append("\n\n")

        // 6. Section: Bridge -- most explicit confessions
        lyricsBuilder.append("[Bridge]\n")
        val bridgePool = listOf(
            "I wanna feel that ${pick("body", kit)} all night.",
            "Say my name when you ${pick("actions", kit)}.",
            "Don't stop 'til I ${pick("vibe", kit)}.",
            "You can do whatever to me, ${pick("address", kit)}.",
            "I'll be your ${pick("self", kit)} forever."
        )
        bridgePool.shuffled().take(3).forEach {
            lyricsBuilder.append(it.replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }).append("\n")
        }
        lyricsBuilder.append("\n")

        // 7. Section: Chorus Repeat
        lyricsBuilder.append("[Chorus]\n")
        chorusHooks.forEachIndexed { i, hookLine ->
            val adlib = if (i == 0) "${pick("adlibs", kit)} " else ""
            val formatted = if (adlib.isNotEmpty()) "$adlib $hookLine" else hookLine
            lyricsBuilder.append(formatted.replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }).append("\n")
        }
        lyricsBuilder.append("\n")

        // 8. Section: Outro
        lyricsBuilder.append("[Outro]\n")
        val outro = listOf(
            "(Moan) … Shit, that pussy good…",
            "(Whisper) … Come back to bed, daddy…",
            "(Fade out) … Ratchet love forever…"
        ).random()
        lyricsBuilder.append(outro)

        return lyricsBuilder.toString()
    }
}
