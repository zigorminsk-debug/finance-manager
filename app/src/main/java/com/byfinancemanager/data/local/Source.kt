package com.byfinancemanager.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "sources")
data class Source(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val type: TransactionType,
    val color: Long = 0xFF4CAF50, // default color as Long
    val icon: String = "💰",
    val isDefault: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)
