package com.sempati.app.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sempati.app.domain.model.SatisGecmisi
import com.sempati.app.domain.repository.SatisGecmisiRepository
import kotlinx.coroutines.flow.*

class KasaViewModel(private val repository: SatisGecmisiRepository) : ViewModel() {

    // İşletme Gecikmesi (Gece 04:00'a kadar dünden sayılır)
    private val geceVardiyasiFarki = 4L * 60 * 60 * 1000
    private val birGunMilisaniye = 24L * 60 * 60 * 1000

    // 0 = Bugün, 1 = Dün, 2 = Evvelsi gün...
    private val _seciliGunFarki = MutableStateFlow(0)

    // Ekranda gösterilecek tarih (Örn: "23 Haz 2026")
    val gorunenTarih: StateFlow<String> = _seciliGunFarki.map { fark ->
        val hedefZaman = System.currentTimeMillis() - (fark * birGunMilisaniye) - geceVardiyasiFarki
        java.text.SimpleDateFormat("dd MMM yyyy", java.util.Locale.getDefault()).format(hedefZaman)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "")

    // SADECE seçili güne ait kayıtları filtreleyen zeki akış
    val filtrelenmisIslemler: StateFlow<List<SatisGecmisi>> = repository.getTumSatislar()
        .combine(_seciliGunFarki) { tumListe, fark ->
            val hedefZaman = System.currentTimeMillis() - (fark * birGunMilisaniye) - geceVardiyasiFarki
            val hedefGunFormatli = java.text.SimpleDateFormat("dd/MM/yyyy", java.util.Locale.getDefault()).format(hedefZaman)

            tumListe.filter { satis ->
                val satisZamaniFormatli = java.text.SimpleDateFormat("dd/MM/yyyy", java.util.Locale.getDefault()).format(satis.tarihSaat - geceVardiyasiFarki)
                satisZamaniFormatli == hedefGunFormatli
            }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Ekrana basılacak özetler (Artık tüm zamanların değil, o günün toplamı!)
    val gunlukCiro: StateFlow<Double> = filtrelenmisIslemler
        .map { liste -> liste.sumOf { it.alinanTutar } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    val gunlukIndirim: StateFlow<Double> = filtrelenmisIslemler
        .map { liste -> liste.sumOf { it.hesaplananTutar - it.alinanTutar } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    // Şalterler
    fun oncekiGun() { _seciliGunFarki.value += 1 }
    fun sonrakiGun() { if (_seciliGunFarki.value > 0) _seciliGunFarki.value -= 1 }
}

class KasaViewModelFactory(
    private val repository: com.sempati.app.domain.repository.SatisGecmisiRepository
) : androidx.lifecycle.ViewModelProvider.Factory {
    override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(KasaViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return KasaViewModel(repository) as T
        }
        throw IllegalArgumentException("Bilinmeyen ViewModel sınıfı")
    }
}