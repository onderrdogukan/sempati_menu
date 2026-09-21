package com.sempati.app.domain

import com.sempati.app.domain.model.Urun

// Sesli komuttan çıkacak geçici sonuç nesnesi
data class ParsedItem(
    val urun: Urun,
    val adet: Int
)

interface OrderParser {
    suspend fun parse(rawText: String, activeProducts: List<Urun>): List<ParsedItem>
}