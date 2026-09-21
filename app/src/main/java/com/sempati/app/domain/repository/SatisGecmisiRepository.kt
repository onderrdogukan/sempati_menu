package com.sempati.app.domain.repository

import com.sempati.app.domain.model.SatisGecmisi
import kotlinx.coroutines.flow.Flow

interface SatisGecmisiRepository {
    suspend fun satisKaydet(satis: SatisGecmisi)
    fun getTumSatislar(): Flow<List<SatisGecmisi>>
}