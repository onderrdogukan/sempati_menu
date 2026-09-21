package com.sempati.app.data.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import androidx.room.Index

@Entity(
    tableName = "hesap_kalemleri",
    foreignKeys = [
        ForeignKey(
            entity = MasaEntity::class,
            parentColumns = ["id"],
            childColumns = ["masaId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = UrunEntity::class,
            parentColumns = ["id"],
            childColumns = ["urunId"],
            onDelete = ForeignKey.SET_NULL
        )
    ],
    indices = [
        Index(value = ["masaId"]),
        Index(value = ["urunId"])
    ]
)
data class HesapKalemiEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val masaId: Int,
    val urunId: Int?, // SET_NULL çalışabilmesi için buranın null kabul edilebilir (Int?) olması şart
    val urunAdiSnapshot: String,
    val birimFiyatSnapshot: Double,
    val adet: Int
)