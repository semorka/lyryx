package com.semorka.lyryx.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase


@Database(entities = [(LyricsEntity::class)], version = 1)
abstract class LyricsRoomDatabase: RoomDatabase() {

    abstract fun lyricsDao(): LyricsDao

    companion object {
        private var INSTANCE: LyricsRoomDatabase? = null
        fun getInstance(context: Context): LyricsRoomDatabase {

            synchronized(this) {
                var instance = INSTANCE
                if (instance == null) {
                    instance = Room.databaseBuilder(
                        context.applicationContext,
                        LyricsRoomDatabase::class.java,
                        "lyricsdb"
                    ).fallbackToDestructiveMigration().build()
                    INSTANCE = instance
                }
                return instance
            }
        }
    }
}