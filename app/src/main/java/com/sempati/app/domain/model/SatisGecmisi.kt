package com.sempati.app.domain.model

data class SatisGecmisi(
    val id: Int,
    val masaId: Int,
    val hesaplananTutar: Double, // Sistemin hesapladığı asıl bakiye
    val alinanTutar: Double,     // Esnafın indirim yapıp kasaya koyduğu nakit
    val tarihSaat: Long          // Z-Raporunda tarih filtresi yapabilmek için zaman damgası
)