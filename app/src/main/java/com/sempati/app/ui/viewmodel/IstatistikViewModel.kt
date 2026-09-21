package com.sempati.app.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sempati.app.domain.repository.SatisGecmisiRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

class IstatistikViewModel(satisGecmisiRepository: SatisGecmisiRepository) : ViewModel() {

    // İşletme Günü Kaydırması: 4 saat (Milisaniye cinsinden)
    // Gece 04:00'a kadar olan tüm işlemler bir önceki günün cirosu sayılır.
    private val geceVardiyasiFarki = 4L * 60 * 60 * 1000

    val gunlukCiro: StateFlow<Map<String, Double>> = satisGecmisiRepository.getTumSatislar()
        .map { list ->
            list.groupBy {
                // MİMARİ ZEKAYI BURAYA GÖMÜYORUZ:
                // İşlem zamanından 4 saat çıkararak gruplama yapıyoruz.
                val islemZamani = it.tarihSaat - geceVardiyasiFarki
                java.text.SimpleDateFormat("dd/MM", java.util.Locale.getDefault()).format(islemZamani)
            }.mapValues { entry -> entry.value.sumOf { it.alinanTutar } }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyMap())
}