package com.sempati.app

import android.app.Application
import androidx.room.Room
import com.sempati.app.data.AppDatabase
import com.sempati.app.data.repository.UrunRepositoryImpl
import com.sempati.app.domain.repository.UrunRepository
import com.sempati.app.data.repository.MasaRepositoryImpl
import com.sempati.app.domain.repository.MasaRepository
import com.sempati.app.domain.repository.HesapKalemiRepository
import com.sempati.app.data.repository.HesapKalemiRepositoryImpl
import com.sempati.app.data.repository.SatisGecmisiRepositoryImpl
import com.sempati.app.domain.model.SatisGecmisi
import com.sempati.app.domain.repository.SatisGecmisiRepository


class SempatiApplication : Application() {
    val database: AppDatabase by lazy {
        Room.databaseBuilder(
            this,
            AppDatabase::class.java,
            "sempati.db"
        ).build()
    }

    val masaRepository: MasaRepository by lazy {
        MasaRepositoryImpl(database.masaDao())
    }

    val urunRepository: UrunRepository by lazy {
        UrunRepositoryImpl(database.urunDao())
    }

    val hesapKalemiRepository: HesapKalemiRepository by lazy {
        HesapKalemiRepositoryImpl(database.hesapKalemiDao())
    }

    val satisGecmisiRepository: SatisGecmisiRepository by lazy {
        SatisGecmisiRepositoryImpl(database.satisGecmisiDao())
    }
}