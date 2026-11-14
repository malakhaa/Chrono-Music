package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.data.model.EqPresetEntity
import com.example.data.model.PlaylistEntity
import com.example.data.model.PlaylistTrackCrossRef
import com.example.data.model.TrackEntity

@Database(
    entities = [
        TrackEntity::class,
        PlaylistEntity::class,
        PlaylistTrackCrossRef::class,
        EqPresetEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class ChronoDatabase : RoomDatabase() {
    abstract fun chronoDao(): ChronoDao

    companion object {
        @Volatile
        private var INSTANCE: ChronoDatabase? = null

        fun getInstance(context: Context): ChronoDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    ChronoDatabase::class.java,
                    "chrono_music.db"
                ).build().also { INSTANCE = it }
            }
        }
    }
}
