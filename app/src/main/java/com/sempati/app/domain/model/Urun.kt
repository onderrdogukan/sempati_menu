package com.sempati.app.domain.model

data class Urun(
    val id: Int,
    val ad: String,
    val fiyat: Double,
    val aktif: Boolean // Menüden kalkınca false olacak, silinmeyecek
)