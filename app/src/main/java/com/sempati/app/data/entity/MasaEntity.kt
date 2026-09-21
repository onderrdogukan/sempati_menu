package com.sempati.app.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "masalar")
data class MasaEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val ad: String,
    val durum: String // Veritabanında enum yerine String tutmak her zaman daha güvenli ve esnektir.
)