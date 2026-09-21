package com.sempati.app.domain

import com.google.ai.client.generativeai.GenerativeModel
import com.sempati.app.domain.model.Urun
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import android.util.Log

class GeminiFallbackService {

    private val generativeModel = GenerativeModel(
        modelName = "gemini-2.5-flash",
        apiKey = AppConfig.GEMINI_API_KEY
    )

    suspend fun parseWithGemini(rawText: String, activeProducts: List<Urun>): List<ParsedItem> = withContext(Dispatchers.IO) {
        val result = mutableListOf<ParsedItem>()

        // Ürün listesini yapay zekanın anlayacağı bir rehbere dönüştürüyoruz
        val urunRehberi = activeProducts.joinToString("\n") { "ID: ${it.id} - Adı: ${it.ad}" }

        // KELEPÇELİ PROMPT: Yapay zekanın dışına çıkamayacağı kurallar zinciri
        val prompt = """
            Sen bir restoran POS sistemi sesli komut ayrıştırıcısısın. 
            Sana aşağıda sistemde kayıtlı aktif ürünlerin listesini (ID ve Ad olarak) ve garsonun söylediği ham metni vereceğim.
            
            Görevin, metinden hangi üründen kaç adet istendiğini bulup sadece ID'sini döndürmektir.
            
            KURALLAR:
            1. Asla açıklama yapma. Çıktı formatı kesinlikle şu şekilde olmalıdır: [GEÇERLİ_URUN_ID]:[ADET]
            2. Eğer metinde "pardon", "vazgeçtim", "yok dur" gibi ifadeler varsa, önceki siparişi tamamen iptal et ve son kararı uygula.
            3. DİKKAT: Sadece ve sadece sana verdiğim "AKTİF ÜRÜNLER LİSTESİ" içinde gerçekten var olan ID'leri kullan. Asla hayali bir ID uydurma! Eğer ürün listede yoksa eşleştirme yapma.
            4. Eğer sipariş listedeki hiçbir ürünle eşleşmiyorsa sadece "BOS" yaz.

            AKTİF ÜRÜNLER LİSTESİ:
            $urunRehberi

            GARSONUN SESLİ KOMUT METNİ:
            "$rawText"
        """.trimIndent()

        try {
            Log.d("GeminiMotoru", "🚨 DİKKAT: Cümle kaotik bulundu. Gemini API'ye istek atılıyor...")
            Log.d("GeminiMotoru", "Giden Metin: $rawText")

            val response = generativeModel.generateContent(prompt)
            val cevapMetni = response.text?.trim() ?: return@withContext emptyList()

            Log.d("GeminiMotoru", "✅ Gemini'den Cevap Geldi:\n$cevapMetni")

            if (cevapMetni == "BOS" || cevapMetni.isBlank()) {
                Log.w("GeminiMotoru", "⚠️ Gemini bu cümleden hiçbir sipariş çıkaramadı!")
                return@withContext emptyList()
            }

            // GÜVENLİK DUVARI: Markdown veya fazlalık metinleri kazı
            val temizCevap = cevapMetni.replace("```", "").replace("text", "").replace("json", "").trim()
            val satirlar = temizCevap.split("\n")

            // REGEX SİLAHI: Arada boşluk olsa bile "Sayı:Sayı" formatını yakalar
            val regex = Regex("""(\d+)\s*:\s*(\d+)""")

            for (satir in satirlar) {
                val match = regex.find(satir)
                if (match != null) {
                    val urunId = match.groupValues[1].toInt()
                    val adet = match.groupValues[2].toInt()

                    val bulunanUrun = activeProducts.find { it.id == urunId }
                    if (bulunanUrun != null) {
                        result.add(ParsedItem(bulunanUrun, adet))
                    } else {
                        Log.w("GeminiMotoru", "⚠️ Hata: AI $urunId ID'sini buldu ama veritabanında böyle bir ürün yok!")
                    }
                } else {
                    Log.w("GeminiMotoru", "⚠️ Satır formata uymuyor ve atlandı: $satir")
                }
            }

            Log.d("GeminiMotoru", "🎯 Ayrıştırma Bitti. Eşleşen ve ViewModel'a giden ürün sayısı: ${result.size}")

        } catch (e: Exception) {
            Log.e("GeminiMotoru", "❌ KRİTİK HATA: Gemini'ye ulaşılamadı!", e)
        }
        return@withContext result
    }
}