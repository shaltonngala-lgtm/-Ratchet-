package com.example.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(entities = [LyricKit::class, GeneratedSong::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun lyricKitDao(): LyricKitDao
    abstract fun generatedSongDao(): GeneratedSongDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context, scope: CoroutineScope): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "ratchdiva_db"
                )
                    .addCallback(AppDatabaseCallback(scope))
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }

    private class AppDatabaseCallback(
        private val scope: CoroutineScope
    ) : RoomDatabase.Callback() {
        override fun onCreate(db: SupportSQLiteDatabase) {
            super.onCreate(db)
            INSTANCE?.let { database ->
                scope.launch(Dispatchers.IO) {
                    populateDatabase(database.lyricKitDao())
                }
            }
        }

        suspend fun populateDatabase(dao: LyricKitDao) {
            // Default Kit: Ratch Diva Original
            val defaultKit = LyricKit(
                id = "ratch_diva_original",
                name = "Ratch Diva Original",
                description = "Street-level, raunchy, rhythmic, confidence-boosting bad bitch vibes.",
                addresses = "daddy\nbaby\nshawty\npapi\nlover\nboo\nmy nigga\nbig daddy\nsexy\nhandsome\nmy king",
                selfWords = "this pussy\nthis bitch\nthis freak\nyour hoe\nyour slut\nyour fantasy\nthis wet wet\nthis body",
                actions = "fuck me\nlick me\nsuck on me\nride you\ntake this dick\nput it in\neat it up\nbend me over\nslide inside\ngrip that ass\nmake me cum\nspank me\nchoke me\npull my hair\ntalk that shit\nthrow it back",
                adjectives = "wet\ntight\nthick\nfreaky\nnasty\nratchet\nhorny\nsoaked\ndrippin'\nthrobbin'\nhard\ndeep\nslow\nrough\nsmooth\nfire\ntoxic",
                bodyParts = "pussy\nass\ntitties\nthighs\nlips\ntongue\nback\nwaist\nhips\nneck\nchest\nprint",
                vibeWords = "I need that dick\ncan't get enough\nyou got me open\nthis shit too good\nI'm feenin'\nI'm addicted\nyou know what it is\nI'm your freak\nyou love this shit\nI'm the best you ever had",
                hooks = "this pussy so good, make you lose your mind\nI'm the baddest bitch you ever had\ndick so good I can't walk right\nyou ain't never had a freak like me\nI'll make you fall in love with this wet wet\nhe say I'm ratchet, I say I'm real\nfuck me like you mean it, daddy\nI'm a hoe for you, and you love that shit",
                adlibs = "(uh)\n(yeah)\n(wet wet)\n(mmh)\n(ahh, shit)\n(fuck, daddy)\n(right there)\n(oooh)\n(hmm, that's it)\n(throw it back)\n(make it clap)",
                isSelected = true
            )

            // Cosmic Erotic Kit
            val cosmicKit = LyricKit(
                id = "cosmic_erotic",
                name = "Cosmic Erotic",
                description = "Intergalactic sensuality, stellar connections, cosmic rhythms & deep gravity pull.",
                addresses = "star boy\nlover\nalien boo\nmoon daddy\ngalaxy king\nsweet space\ncosmic king",
                selfWords = "this starlight\nthis black hole\nthis neon galaxy\nyour gravitational pull\nyour hyperdrive",
                actions = "float inside me\npull me closer\nheat me up\nbeam me up\ntranscend this dimension\nglide through hyperspace\ngrab my stardust\nwrap me up\ntalk celestial\ntilt my orbit",
                adjectives = "glowing\ncelestial\nendless\nwarm\nwild\nspicy\nelectric\nnebular\nfloating\nsupernova\nmagnetic",
                bodyParts = "stardust\norbit\nconstellations\nlips\ncenter of gravity\nhips\nbreath\nskin\nsoul",
                vibeWords = "you got me floating in orbit\nthis gravity is too strong\nI need that stellar energy\nour connection is infinite\nI'm addicted to your galaxy\nyou're my ultimate supergiant\nyou love this starlight",
                hooks = "this gravity is taking over your space-time\nI'm the stars you wanna get lost in\nspace dick got me walking on light speed\nyou ain't never had celestial head like this\nI'll make you float in deep space all night\nhe says it's magic, I say it's gravity\nwrap your arms around my universe\nI'm your alien hoe and you love this ship",
                adlibs = "(float)\n(blast off)\n(yeah)\n(mmh cosmic)\n(space baby)\n(pull me in)\n(oooh celestial)",
                isSelected = false
            )

            dao.insertKit(defaultKit)
            dao.insertKit(cosmicKit)
        }
    }
}
