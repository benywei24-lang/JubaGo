package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [OrderEntity::class, FavoriteEntity::class], version = 2, exportSchema = false)
abstract class JubaGoDatabase : RoomDatabase() {

    abstract fun orderDao(): OrderDao
    abstract fun favoriteDao(): FavoriteDao

    companion object {
        @Volatile
        private var INSTANCE: JubaGoDatabase? = null

        fun getDatabase(context: Context): JubaGoDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    JubaGoDatabase::class.java,
                    "jubago_offline_database"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
