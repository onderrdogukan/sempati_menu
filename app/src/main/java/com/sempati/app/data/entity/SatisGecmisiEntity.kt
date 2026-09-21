package com.sempati.app.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "satis_gecmisi")
data class SatisGecmisiEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val masaId: Int,
    val hesaplananTutar: Double,
    val alinanTutar: Double,
    val tarihSaat: Long
)