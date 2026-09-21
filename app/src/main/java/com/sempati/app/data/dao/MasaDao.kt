package com.sempati.app.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import com.sempati.app.data.entity.MasaEntity
import com.sempati.app.domain.model.MasaDurumu
import kotlinx.coroutines.flow.Flow

@Dao
interface MasaDao {
    @Query("SELECT * FROM masalar")
    fun tumMasalariGetir(): Flow<List<MasaEntity>>

    @Query("UPDATE masalar SET durum = :yeniDurum WHERE id = :masaId")
    suspend fun durumGuncelle(masaId: Int, yeniDurum: String)

    @Insert
    suspend fun masaEkle(masa: MasaEntity)

    @Delete
    suspend fun masaSil(masa: MasaEntity)

    @Query("SELECT * FROM masalar WHERE id = :id")
    suspend fun getMasaById(id: Int): MasaEntity?}