package com.example.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [
        FolioEntity::class,
        BookmarkEntity::class,
        ChapterEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class FolioDatabase : RoomDatabase() {
    abstract fun folioDao(): FolioDao

    companion object {
        @Volatile
        private var INSTANCE: FolioDatabase? = null

        fun getDatabase(context: Context): FolioDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    FolioDatabase::class.java,
                    "folio_3d_reader.db"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
