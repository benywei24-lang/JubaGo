package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface FavoriteDao {

    @Query("SELECT * FROM favorite_services ORDER BY addedAt DESC")
    fun getAllFavorites(): Flow<List<FavoriteEntity>>

    @Query("SELECT EXISTS(SELECT 1 FROM favorite_services WHERE serviceId = :serviceId)")
    fun isFavorite(serviceId: String): Flow<Boolean>

    @Query("SELECT serviceId FROM favorite_services")
    fun getFavoriteServiceIds(): Flow<List<String>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFavorite(favorite: FavoriteEntity)

    @Query("DELETE FROM favorite_services WHERE serviceId = :serviceId")
    suspend fun deleteFavorite(serviceId: String)
}
