package com.sempati.app.domain.model

enum class MasaDurumu {
    Bos,
    Dolu
}

data class Masa(
    val id: Int,
    val ad: String,
    val durum: MasaDurumu // String değil, bu Enum tipinde olmalı
)