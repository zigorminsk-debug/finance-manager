package com.byfinancemanager.data.local

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "transactions",
    foreignKeys = [
        ForeignKey(
            entity = Source::class,
            parentColumns = ["id"],
            childColumns = ["sourceId"],
            onDelete = ForeignKey.SET_NULL
        )
    ],
    indices = [Index("sourceId"), Index("date")]
)
data class Transaction(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val type: TransactionType,
    val amountByn: Double, // Сумма в BYN
    val amountUsd: Double, // Сумма в USD по курсу НБРБ на день оплаты
    val exchangeRate: Double, // Курс НБРБ USD/BYN на день операции
    val date: Long, // timestamp millis
    val sourceId: Long?,
    val note: String = "",
    val createdAt: Long = System.currentTimeMillis()
)
