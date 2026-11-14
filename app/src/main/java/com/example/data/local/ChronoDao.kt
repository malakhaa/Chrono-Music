package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.example.data.model.EqPresetEntity
import com.example.data.model.PlaylistEntity
import com.example.data.model.PlaylistTrackCrossRef
import com.example.data.model.PlaylistWithTracks
import com.example.data.model.TrackEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ChronoDao {
    // Tracks
    @Query("SELECT * FROM tracks ORDER BY isBuiltIn DESC, title ASC")
    fun observeAllTracks(): Flow<List<TrackEntity>>

    @Query("SELECT * FROM tracks WHERE id = :trackId")
    suspend fun getTrackById(trackId: String): TrackEntity?

    @Query("SELECT COUNT(*) FROM tracks")
    suspend fun getTrackCount(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTracks(tracks: List<TrackEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTrack(track: TrackEntity)

    @Query("UPDATE tracks SET isFavorite = :favorite WHERE id = :trackId")
    suspend fun setTrackFavorite(trackId: String, favorite: Boolean)

    @Query("UPDATE tracks SET isCachedOffline = :cached WHERE id = :trackId")
    suspend fun setTrackCachedOffline(trackId: String, cached: Boolean)

    @Query("UPDATE tracks SET playCount = playCount + 1 WHERE id = :trackId")
    suspend fun incrementPlayCount(trackId: String)

    @Query("DELETE FROM tracks WHERE id = :trackId AND isBuiltIn = 0")
    suspend fun deleteImportedTrack(trackId: String)

    // Playlists
    @Transaction
    @Query("SELECT * FROM playlists ORDER BY isCurated DESC, createdAt ASC")
    fun observePlaylistsWithTracks(): Flow<List<PlaylistWithTracks>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPlaylist(playlist: PlaylistEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPlaylists(playlists: List<PlaylistEntity>)

    @Query("DELETE FROM playlists WHERE id = :playlistId")
    suspend fun deletePlaylist(playlistId: String)

    @Query("DELETE FROM playlist_tracks WHERE playlistId = :playlistId")
    suspend fun clearPlaylistTracks(playlistId: String)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPlaylistTrackRef(ref: PlaylistTrackCrossRef)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPlaylistTrackRefs(refs: List<PlaylistTrackCrossRef>)

    @Query("DELETE FROM playlist_tracks WHERE playlistId = :playlistId AND trackId = :trackId")
    suspend fun removeTrackFromPlaylist(playlistId: String, trackId: String)

    // EQ & DSP Presets
    @Query("SELECT * FROM eq_presets ORDER BY isBuiltIn DESC, name ASC")
    fun observeEqPresets(): Flow<List<EqPresetEntity>>

    @Query("SELECT COUNT(*) FROM eq_presets")
    suspend fun getEqPresetCount(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEqPreset(preset: EqPresetEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEqPresets(presets: List<EqPresetEntity>)

    @Query("DELETE FROM eq_presets WHERE id = :presetId AND isBuiltIn = 0")
    suspend fun deleteEqPreset(presetId: String)
}
