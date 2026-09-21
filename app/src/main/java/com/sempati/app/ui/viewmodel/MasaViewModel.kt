package com.sempati.app.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.sempati.app.domain.OrderParserImpl
import com.sempati.app.domain.GeminiFallbackService
import com.sempati.app.domain.ParsedItem
import com.sempati.app.domain.model.HesapKalemi
import com.sempati.app.domain.model.Masa
import com.sempati.app.domain.model.MasaDurumu
import com.sempati.app.domain.repository.HesapKalemiRepository
import com.sempati.app.domain.repository.MasaRepository
import com.sempati.app.domain.repository.UrunRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.Locale
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow

data class PendingOrder(
    val masaId: Int,
    val masaAdi: String,
    val items: List<ParsedItem>
)

// 1. YENİ SİLAH: Niyet (Intent) Tipleri
enum class KomutTipi { SIPARIS, HESAP_KAPAT, ADISYON_YAZDIR }

class MasaViewModel(
    private val masaRepository: MasaRepository,
    private val urunRepository: UrunRepository,
    private val hesapKalemiRepository: HesapKalemiRepository
) : ViewModel() {

    val tumMasalar: StateFlow<List<Masa>> = masaRepository.tumMasalariGetir()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // UI'ın dinleyeceği sipariş durumu
    private val _pendingOrder = MutableStateFlow<PendingOrder?>(null)
    val pendingOrder: StateFlow<PendingOrder?> = _pendingOrder.asStateFlow()

    // 2. YENİ GÜVENLİK DUVARI: Aksiyon (Donanım/Sistem) durumu
    private val _masaAksiyonu = MutableStateFlow<Pair<KomutTipi, Masa>?>(null)
    val masaAksiyonu: StateFlow<Pair<KomutTipi, Masa>?> = _masaAksiyonu.asStateFlow()

    // 3. NİYET OKUYUCU MOTOR
    private fun niyetOku(metin: String): KomutTipi {
        val m = metin.lowercase(Locale("tr", "TR"))
        return when {
            m.contains("hesap kapat") || m.contains("hesabı kapat") || m.contains("hesabı al") -> KomutTipi.HESAP_KAPAT
            m.contains("yazdır") || m.contains("adisyon çıkar") || m.contains("fiş") -> KomutTipi.ADISYON_YAZDIR
            else -> KomutTipi.SIPARIS
        }
    }

    private val _sesliKomutHatasi = MutableSharedFlow<String>()
    val sesliKomutHatasi = _sesliKomutHatasi.asSharedFlow()

    // TRAFİK YÖNLENDİRİCİ (GÜNCELLENDİ)
    fun sesliSiparisiAyristir(masaId: Int, masaAdi: String, hamMetin: String) {
        // ÖNCE NİYET OKUNUR: Bu bir sipariş mi yoksa sistem komutu mu?
        val niyet = niyetOku(hamMetin)

        if (niyet != KomutTipi.SIPARIS) {
            // Eğer komutsa, sipariş motorlarına HİÇ SOKMADAN UI'daki aksiyon ekranına fırlat
            val masa = tumMasalar.value.find { it.id == masaId }
            if (masa != null) {
                _masaAksiyonu.value = Pair(niyet, masa)
            }
            return // Kodu burada keser, yapay zekayı boş yere yormaz
        }

        // Eğer komut değil, normal siparişse bildiğimiz yoldan (Hibrit Motor) devam eder
        viewModelScope.launch {
            try {
                // 1. Veritabanındaki güncel menüyü al
                val aktifUrunler = urunRepository.getAktifUrunler().first()
                var siparisler: List<ParsedItem> = emptyList()

                // YÖNLENDİRİCİ MANTIĞI: Cümle kaotik mi?
                val kaotikKelimeler = listOf("vazgeçtim", "pardon", "iptal", "yok dur", "değiştir", "yerine", "değil")
                val isKaotik = kaotikKelimeler.any { hamMetin.contains(it) } || hamMetin.split(" ").size > 6

                // 2. PARSER MOTORLARI ÇALIŞIR
                if (isKaotik) {
                    val geminiService = GeminiFallbackService()
                    siparisler = geminiService.parseWithGemini(hamMetin, aktifUrunler)
                } else {
                    val yerelParser = OrderParserImpl()
                    siparisler = yerelParser.parse(hamMetin, aktifUrunler)

                    if (siparisler.isEmpty()) {
                        val geminiService = GeminiFallbackService()
                        siparisler = geminiService.parseWithGemini(hamMetin, aktifUrunler)
                    }
                }

                // 3. İŞTE GERÇEK KONTROL NOKTASI BURASI!
                // Ayrıştırma işlemleri bitti. Elimizde bir sonuç var mı?
                if (siparisler.isEmpty()) {
                    // Sepet boş! Ürün anlaşılamadı veya menüde yok. Feryadı bas!
                    _sesliKomutHatasi.emit("'$hamMetin' anlaşılamadı veya menüde bulunamadı!")
                } else {
                    // Sepet dolu, onaya gönder!
                    _pendingOrder.value = PendingOrder(masaId, masaAdi, siparisler)
                }

            } catch (e: Exception) {
                e.printStackTrace()
                _sesliKomutHatasi.emit("Sistemde beklenmeyen bir hata oluştu.")
            }
        }
    }

    // 4. AKSİYON OPERASYONLARI (Hesap Kapatma / Yazdırma)
    fun aksiyonuTamamla() {
        _masaAksiyonu.value = null
    }

    fun aksiyonuIptalEt() {
        _masaAksiyonu.value = null
    }

    // MEVCUT SİPARİŞ İŞLEMLERİ
    fun onaylananSiparisiKaydet() {
        val mevcutSiparis = _pendingOrder.value ?: return
        viewModelScope.launch {
            try {
                val mevcutHesap = hesapKalemiRepository.getHesapByMasaId(mevcutSiparis.masaId).first()

                mevcutSiparis.items.forEach { item ->
                    val varOlanKalem = mevcutHesap.find { it.urunId == item.urun.id }

                    if (varOlanKalem != null) {
                        val guncelKalem = varOlanKalem.copy(adet = varOlanKalem.adet + item.adet)
                        hesapKalemiRepository.kalemGuncelle(guncelKalem)
                    } else {
                        val yeniKalem = HesapKalemi(
                            id = 0,
                            masaId = mevcutSiparis.masaId,
                            urunId = item.urun.id,
                            urunAdiSnapshot = item.urun.ad,
                            birimFiyatSnapshot = item.urun.fiyat,
                            adet = item.adet
                        )
                        hesapKalemiRepository.hesapKalemiEkle(yeniKalem)
                    }
                }

                masaRepository.durumGuncelle(mevcutSiparis.masaId, MasaDurumu.Dolu)
                _pendingOrder.value = null

            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun siparisiIptalEt() {
        _pendingOrder.value = null
    }

    fun masaEkle(ad: String) {
        viewModelScope.launch {
            masaRepository.masaEkle(Masa(id = 0, ad = ad, durum = MasaDurumu.Bos))
        }
    }

    fun masaSil(masa: Masa) {
        viewModelScope.launch {
            masaRepository.masaSil(masa)
        }
    }
}

class MasaViewModelFactory(
    private val masaRepository: MasaRepository,
    private val urunRepository: UrunRepository,
    private val hesapKalemiRepository: HesapKalemiRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MasaViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return MasaViewModel(masaRepository, urunRepository, hesapKalemiRepository) as T
        }
        throw IllegalArgumentException("Bilinmeyen ViewModel Sınıfı")
    }
}