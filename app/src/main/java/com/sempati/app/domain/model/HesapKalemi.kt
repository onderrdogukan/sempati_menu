package com.sempati.app.domain.model

data class HesapKalemi(
    val id: Int,
    val masaId: Int,
    val urunId: Int?, // Ürün tamamen silinirse patlamamak için null olabilir (SET_NULL mantığı)
    val urunAdiSnapshot: String,
    val birimFiyatSnapshot: Double,
    val adet: Int
)