package com.sempati.app.domain.repository

import com.sempati.app.domain.model.Masa
import com.sempati.app.domain.model.MasaDurumu
import kotlinx.coroutines.flow.Flow

interface MasaRepository {
    fun tumMasalariGetir(): Flow<List<Masa>>
    suspend fun masaEkle(masa: Masa)
    suspend fun masaSil(masa: Masa)
    suspend fun getMasaById(id: Int): Masa?
    suspend fun durumGuncelle(masaId: Int, durum: MasaDurumu)
}