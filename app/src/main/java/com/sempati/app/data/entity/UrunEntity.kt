package com.sempati.app.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "urunler")
data class UrunEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val ad: String,
    val fiyat: Double,
    val aktif: Boolean
)