package com.sempati.app.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.sempati.app.data.entity.UrunEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface UrunDao {
    // Sadece aktif ürünleri getiren ve veritabanı değiştikçe anlık tepki veren (Flow) SQL sorgumuz
    @Query("SELECT * FROM urunler WHERE aktif = 1")
    fun getActiveUrunler(): Flow<List<UrunEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun urunEkle(urun: UrunEntity)

    // Menüde sadece aktif olan (silinmemiş) ürünleri listelemek için
    @Query("SELECT * FROM urunler WHERE aktif = 1")
    fun getAktifUrunler(): kotlinx.coroutines.flow.Flow<List<com.sempati.app.data.entity.UrunEntity>>

    // Ürün güncellemek için
    @androidx.room.Update
    suspend fun urunGuncelle(urun: UrunEntity)

    // İstenilen ürünü veritabanından tamamen uçurmak için
    @Query("DELETE FROM urunler WHERE id = :urunId")
    suspend fun urunSil(urunId: Int)

}