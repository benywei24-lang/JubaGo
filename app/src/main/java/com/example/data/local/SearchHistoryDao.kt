package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface SearchHistoryDao {

    @Query("SELECT * FROM search_history ORDER BY timestamp DESC LIMIT :limit")
    fun getRecentSearchesFlow(limit: Int = 15): Flow<List<SearchQueryEntity>>

    @Query("SELECT * FROM search_history ORDER BY timestamp DESC LIMIT :limit")
    suspend fun getRecentSearches(limit: Int = 15): List<SearchQueryEntity>

    @Query("SELECT * FROM search_history WHERE query = :query LIMIT 1")
    suspend fun findSearchByQuery(query: String): SearchQueryEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSearch(search: SearchQueryEntity)

    @Query("DELETE FROM search_history WHERE id = :id")
    suspend fun deleteSearchById(id: Long)

    @Query("DELETE FROM search_history WHERE query = :query")
    suspend fun deleteSearchByQuery(query: String)

    @Query("DELETE FROM search_history")
    suspend fun clearAll()
}
