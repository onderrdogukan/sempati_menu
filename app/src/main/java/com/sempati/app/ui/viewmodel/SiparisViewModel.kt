package com.sempati.app.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.sempati.app.domain.model.HesapKalemi
import com.sempati.app.domain.model.MasaDurumu
import com.sempati.app.domain.model.SatisGecmisi
import com.sempati.app.domain.model.Urun
import com.sempati.app.domain.repository.HesapKalemiRepository
import com.sempati.app.domain.repository.MasaRepository
import com.sempati.app.domain.repository.SatisGecmisiRepository
import com.sempati.app.domain.repository.UrunRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class SiparisViewModel(
    private val masaId: Int,
    private val urunRepository: UrunRepository,
    private val hesapKalemiRepository: HesapKalemiRepository,
    private val masaRepository: MasaRepository,
    private val satisGecmisiRepository: SatisGecmisiRepository
) : ViewModel() {

    private val _masaAdi = MutableStateFlow("Yükleniyor...")
    val masaAdi: StateFlow<String> = _masaAdi.asStateFlow()

    init {
        viewModelScope.launch {
            // Repository'den masayı ID ile bul ve adını ata
            val masa = masaRepository.getMasaById(masaId)
            _masaAdi.value = masa?.ad ?: "Masa $masaId"
        }
    }

    // Son silineni hafızada tutan değişken
    private var sonSilinenKalem: HesapKalemi? = null

    val aktifUrunler: StateFlow<List<Urun>> = urunRepository.getAktifUrunler()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val masaninHesabi: StateFlow<List<HesapKalemi>> = hesapKalemiRepository.getHesapByMasaId(masaId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val toplamTutar: StateFlow<Double> = masaninHesabi
        .map { liste -> liste.sumOf { it.birimFiyatSnapshot * it.adet } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    fun hesabiKapat(alinanTutar: Double, onHesapKapandi: () -> Unit) {
        viewModelScope.launch {
            val yeniSatisKaydi = SatisGecmisi(
                id = 0,
                masaId = masaId,
                hesaplananTutar = toplamTutar.value,
                alinanTutar = alinanTutar,
                tarihSaat = System.currentTimeMillis()
            )
            satisGecmisiRepository.satisKaydet(yeniSatisKaydi)
            hesapKalemiRepository.masaninHesabiniKapat(masaId)
            masaRepository.durumGuncelle(masaId, MasaDurumu.Bos)
            onHesapKapandi()
        }
    }

    fun adisyonaEkle(urun: Urun) {
        viewModelScope.launch {
            val varOlanKalem = masaninHesabi.value.find { it.urunAdiSnapshot == urun.ad }
            if (varOlanKalem != null) {
                hesapKalemiRepository.kalemGuncelle(varOlanKalem.copy(adet = varOlanKalem.adet + 1))
            } else {
                val yeniKalem = HesapKalemi(0, masaId, urun.id, urun.ad, urun.fiyat, 1)
                hesapKalemiRepository.hesapKalemiEkle(yeniKalem)
            }
            masaRepository.durumGuncelle(masaId, MasaDurumu.Dolu)
        }
    }

    fun adisyondanSil(kalem: HesapKalemi) {
        sonSilinenKalem = kalem // Silmeden önce hafızaya al
        viewModelScope.launch {
            if (kalem.adet > 1) {
                hesapKalemiRepository.kalemGuncelle(kalem.copy(adet = kalem.adet - 1))
            } else {
                hesapKalemiRepository.hesapKalemiSil(kalem.id)
                if (masaninHesabi.value.size <= 1) {
                    masaRepository.durumGuncelle(masaId, MasaDurumu.Bos)
                }
            }
        }
    }

    fun geriAl() {
        sonSilinenKalem?.let { kalem ->
            viewModelScope.launch {
                hesapKalemiRepository.hesapKalemiEkle(kalem)
                masaRepository.durumGuncelle(masaId, MasaDurumu.Dolu)
            }
        }
    }
}

class SiparisViewModelFactory(
    private val masaId: Int,
    private val urunRepository: UrunRepository,
    private val hesapKalemiRepository: HesapKalemiRepository,
    private val masaRepository: MasaRepository,
    private val satisGecmisiRepository: SatisGecmisiRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(SiparisViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return SiparisViewModel(masaId, urunRepository, hesapKalemiRepository, masaRepository, satisGecmisiRepository) as T
        }
        throw IllegalArgumentException("Bilinmeyen ViewModel sınıfı")
    }
}