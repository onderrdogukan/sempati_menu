package com.sempati.app.domain.repository

import com.sempati.app.domain.model.HesapKalemi
import kotlinx.coroutines.flow.Flow

interface HesapKalemiRepository {
    fun getHesapByMasaId(masaId: Int): Flow<List<HesapKalemi>>
    suspend fun hesapKalemiEkle(kalem: HesapKalemi)
    suspend fun masaninHesabiniKapat(masaId: Int)
    suspend fun hesapKalemiSil(kalemId: Int)
    suspend fun kalemGuncelle(kalem: com.sempati.app.domain.model.HesapKalemi)
}