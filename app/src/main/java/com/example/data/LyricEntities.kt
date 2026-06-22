package com.example.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Entity(tableName = "lyric_kits")
data class LyricKit(
    @PrimaryKey val id: String,
    val name: String,
    val description: String,
    val addresses: String,     // Newline-separated lists
    val selfWords: String,
    val actions: String,
    val adjectives: String,
    val bodyParts: String,
    val vibeWords: String,
    val hooks: String,
    val adlibs: String,
    val isSelected: Boolean = false
) {
    fun getAddressList(): List<String> = splitWords(addresses)
    fun getSelfList(): List<String> = splitWords(selfWords)
    fun getActionList(): List<String> = splitWords(actions)
    fun getAdjectiveList(): List<String> = splitWords(adjectives)
    fun getBodyList(): List<String> = splitWords(bodyParts)
    fun getVibeList(): List<String> = splitWords(vibeWords)
    fun getHookList(): List<String> = splitWords(hooks)
    fun getAdlibList(): List<String> = splitWords(adlibs)

    private fun splitWords(raw: String): List<String> {
        return raw.split("\n")
            .map { it.trim() }
            .filter { it.isNotEmpty() }
    }
}

@Entity(tableName = "generated_songs")
data class GeneratedSong(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val artist: String,
    val lyrics: String,
    val kitName: String,
    val createdAt: Long = System.currentTimeMillis()
)

@Dao
interface LyricKitDao {
    @Query("SELECT * FROM lyric_kits ORDER BY name ASC")
    fun getAllKits(): Flow<List<LyricKit>>

    @Query("SELECT * FROM lyric_kits WHERE isSelected = 1 LIMIT 1")
    fun getSelectedKitFlow(): Flow<LyricKit?>

    @Query("SELECT * FROM lyric_kits WHERE isSelected = 1 LIMIT 1")
    suspend fun getSelectedKit(): LyricKit?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertKit(kit: LyricKit)

    @Query("UPDATE lyric_kits SET isSelected = 0")
    suspend fun deselectAll()

    @Transaction
    suspend fun selectKit(kitId: String) {
        deselectAll()
        updateSelection(kitId, true)
    }

    @Query("UPDATE lyric_kits SET isSelected = :selected WHERE id = :kitId")
    suspend fun updateSelection(kitId: String, selected: Boolean)

    @Query("DELETE FROM lyric_kits WHERE id = :kitId")
    suspend fun deleteKit(kitId: String)
}

@Dao
interface GeneratedSongDao {
    @Query("SELECT * FROM generated_songs ORDER BY createdAt DESC")
    fun getAllSongs(): Flow<List<GeneratedSong>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSong(song: GeneratedSong)

    @Query("DELETE FROM generated_songs WHERE id = :id")
    suspend fun deleteSongById(id: Int)
}
