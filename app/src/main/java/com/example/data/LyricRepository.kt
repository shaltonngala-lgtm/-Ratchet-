package com.example.data

import kotlinx.coroutines.flow.Flow

class LyricRepository(
    private val lyricKitDao: LyricKitDao,
    private val generatedSongDao: GeneratedSongDao
) {
    val allKits: Flow<List<LyricKit>> = lyricKitDao.getAllKits()
    val selectedKitFlow: Flow<LyricKit?> = lyricKitDao.getSelectedKitFlow()
    val allSongs: Flow<List<GeneratedSong>> = generatedSongDao.getAllSongs()

    suspend fun getSelectedKit(): LyricKit? {
        return lyricKitDao.getSelectedKit()
    }

    suspend fun selectKit(kitId: String) {
        lyricKitDao.selectKit(kitId)
    }

    suspend fun insertKit(kit: LyricKit) {
        lyricKitDao.insertKit(kit)
    }

    suspend fun deleteKit(kitId: String) {
        lyricKitDao.deleteKit(kitId)
    }

    suspend fun insertSong(song: GeneratedSong) {
        generatedSongDao.insertSong(song)
    }

    suspend fun deleteSongById(id: Int) {
        generatedSongDao.deleteSongById(id)
    }
}
