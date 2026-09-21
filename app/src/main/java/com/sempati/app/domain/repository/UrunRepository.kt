package com.sempati.app.domain.repository

import com.sempati.app.domain.model.Urun
import kotlinx.coroutines.flow.Flow

interface UrunRepository {

    fun getAktifUrunler(): kotlinx.coroutines.flow.Flow<List<com.sempati.app.domain.model.Urun>>
    suspend fun urunSil(urunId: Int)

    // Gerekirse yeni ürün eklemek için
    suspend fun urunEkle(urun: Urun)
    //Ürün düzenlemek için
    suspend fun urunGuncelle(urun: Urun)
}