package com.example.data.local

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "search_history",
    indices = [Index(value = ["query"], unique = true)]
)
data class SearchQueryEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val query: String,
    val categoryContext: String? = null,
    val timestamp: Long = System.currentTimeMillis(),
    val searchCount: Int = 1
)
