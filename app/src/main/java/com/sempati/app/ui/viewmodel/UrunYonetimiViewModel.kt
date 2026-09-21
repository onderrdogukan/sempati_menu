package com.sempati.app.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.sempati.app.domain.model.Urun
import com.sempati.app.domain.repository.UrunRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class UrunYonetimiViewModel(
    private val urunRepository: UrunRepository
) : ViewModel() {

    // Menüdeki tüm aktif ürünleri canlı olarak dinliyoruz
    val tumUrunler: StateFlow<List<Urun>> = urunRepository.getAktifUrunler()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    // Yeni ürün ekleme fonksiyonu
    fun urunEkle(ad: String, fiyat: Double) {
        if (ad.isBlank() || fiyat <= 0.0) return // Boş veya bedava ürün eklenmesini engelle (Güvenlik!)

        viewModelScope.launch {
            val yeniUrun = Urun(id = 0, ad = ad, fiyat = fiyat, aktif = true)
            urunRepository.urunEkle(yeniUrun)
        }
    }

    // Ürün düzenle fonksiyonu
    fun urunGuncelle(guncelUrun: com.sempati.app.domain.model.Urun) {
        viewModelScope.launch {
            urunRepository.urunGuncelle(guncelUrun)
        }
    }

    // Ürünü menüden tamamen silme fonksiyonu
    fun urunSil(urunId: Int) {
        viewModelScope.launch {
            urunRepository.urunSil(urunId)
        }
    }
}

class UrunYonetimiViewModelFactory(
    private val urunRepository: UrunRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(UrunYonetimiViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return UrunYonetimiViewModel(urunRepository) as T
        }
        throw IllegalArgumentException("Bilinmeyen ViewModel sınıfı")
    }
}