package com.sempati.app.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.sempati.app.domain.repository.SatisGecmisiRepository

class IstatistikViewModelFactory(
    private val satisGecmisiRepository: SatisGecmisiRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(IstatistikViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return IstatistikViewModel(satisGecmisiRepository) as T
        }
        throw IllegalArgumentException("Bilinmeyen ViewModel sınıfı")
    }
}