package com.sempati.app.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.sempati.app.data.entity.HesapKalemiEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface HesapKalemiDao {
    // Sadece içine girdiğimiz masanın siparişlerini getiren akış
    @Query("SELECT * FROM hesap_kalemleri WHERE masaId = :masaId")
    fun getHesapByMasaId(masaId: Int): Flow<List<HesapKalemiEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun hesapKalemiEkle(kalem: HesapKalemiEntity)

    // Opsiyonel: Hesap ödendiğinde masayı temizlemek için
    @Query("DELETE FROM hesap_kalemleri WHERE masaId = :masaId")
    suspend fun masaninHesabiniKapat(masaId: Int)

    @Query("DELETE FROM hesap_kalemleri WHERE id = :kalemId")
    suspend fun kalemSil(kalemId: Int)

    // Mevcut satırın adetini vs. güncellemek için
    @Update
    suspend fun kalemGuncelle(kalem: com.sempati.app.data.entity.HesapKalemiEntity)
}