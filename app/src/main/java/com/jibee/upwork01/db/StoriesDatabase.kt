package com.jibee.upwork01.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.jibee.upwork01.models.Stories.Stories_All
import com.jibee.upwork01.models.Stories.UserStory

@Database(
    entities = [UserStory::class, Stories_All::class],
    version = 1
)
@TypeConverters(Converters::class)
abstract class StoriesDatabase : RoomDatabase() {

    abstract fun getStoriesDao(): StoriesDao

    companion object {
        @Volatile
        private var instance: StoriesDatabase? = null
        private val LOCK = Any()

        operator fun invoke(context: Context) = instance ?: synchronized(LOCK) {
            instance ?: createDatabase(context).also { instance = it }
        }

        private fun createDatabase(context: Context) =
            Room.databaseBuilder(
                context.applicationContext,
                StoriesDatabase::class.java,
                "stories_db.db"
            ).build()

    }
}