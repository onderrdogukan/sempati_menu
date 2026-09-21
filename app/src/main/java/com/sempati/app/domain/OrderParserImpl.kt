package com.sempati.app.domain

import com.sempati.app.domain.model.Urun
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.Locale
import kotlin.math.abs
import kotlin.math.max

class OrderParserImpl : OrderParser {

    // KUSURSUZ MİMARİ: Tüm işlem arka planda (Dispatchers.Default) çalışacak!
    override suspend fun parse(rawText: String, activeProducts: List<Urun>): List<ParsedItem> = withContext(Dispatchers.Default) {
        val result = mutableListOf<ParsedItem>()
        val trLocale = Locale("tr", "TR")

        var islenmisMetin = rawText.lowercase(trLocale)

        val rakamSozlugu = mapOf(
            "bir" to "1", "iki" to "2", "üç" to "3", "dört" to "4", "beş" to "5",
            "altı" to "6", "yedi" to "7", "sekiz" to "8", "dokuz" to "9", "on" to "10",
            "on bir" to "11", "on iki" to "12", "yirmi" to "20", "otuz" to "30",
            "kırk" to "40", "elli" to "50"
        )
        rakamSozlugu.forEach { (kelime, rakam) ->
            islenmisMetin = islenmisMetin.replace(Regex("\\b$kelime\\b"), rakam)
        }

        val kelimeler = islenmisMetin.split("\\s+".toRegex())

        for (urun in activeProducts) {
            val urunAdi = urun.ad.lowercase(trLocale)
            var eslesmeIndeksi = -1
            var urunBulundu = false

            if (islenmisMetin.contains(urunAdi)) {
                urunBulundu = true
                eslesmeIndeksi = kelimeler.indexOfFirst { it.contains(urunAdi.split(" ")[0]) }
            } else {
                for (i in kelimeler.indices) {
                    val tekli = kelimeler[i]
                    val ikili = if (i < kelimeler.size - 1) kelimeler[i] + kelimeler[i + 1] else ""

                    if (benzerlikOrani(urunAdi, tekli) > 0.70 || (ikili.isNotEmpty() && benzerlikOrani(urunAdi, ikili) > 0.75)) {
                        urunBulundu = true
                        eslesmeIndeksi = i
                        break
                    }
                }
            }

            if (urunBulundu) {
                var adet = 1
                val sayilar = mutableListOf<Pair<Int, Int>>()
                for (i in kelimeler.indices) {
                    val rakam = kelimeler[i].toIntOrNull()
                    if (rakam != null) {
                        sayilar.add(Pair(i, rakam))
                    }
                }

                if (sayilar.isNotEmpty() && eslesmeIndeksi != -1) {
                    val enYakinSayi = sayilar.minByOrNull { abs(it.first - eslesmeIndeksi) }
                    if (enYakinSayi != null && abs(enYakinSayi.first - eslesmeIndeksi) <= 4) {
                        adet = enYakinSayi.second
                    }
                }

                result.add(ParsedItem(urun, adet))
            }
        }

        // return işlemini @withContext etiketine gönderiyoruz
        return@withContext result
    }

    private fun benzerlikOrani(s1: String, s2: String): Double {
        if (s1 == s2) return 1.0
        val maxLen = max(s1.length, s2.length)
        if (maxLen == 0) return 1.0

        val costs = IntArray(s2.length + 1) { it }
        for (i in s1.indices) {
            var cost = i
            var previousCost = i
            for (j in s2.indices) {
                val currentCost = cost + 1
                cost = costs[j + 1]
                val deletionCost = cost + 1
                val substitutionCost = previousCost + if (s1[i] == s2[j]) 0 else 1
                costs[j + 1] = minOf(currentCost, deletionCost, substitutionCost)
                previousCost = currentCost
            }
        }
        val distance = costs[s2.length]
        return (maxLen - distance).toDouble() / maxLen.toDouble()
    }
}