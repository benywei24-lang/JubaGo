package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "favorite_services")
data class FavoriteEntity(
    @PrimaryKey
    val serviceId: String,
    val addedAt: Long = System.currentTimeMillis()
)
