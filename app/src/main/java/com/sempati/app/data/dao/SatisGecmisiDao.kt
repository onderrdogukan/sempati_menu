package com.sempati.app.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.sempati.app.data.entity.SatisGecmisiEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface SatisGecmisiDao {
    @Insert
    suspend fun satisEkle(satis: SatisGecmisiEntity)

    @Query("SELECT * FROM satis_gecmisi ORDER BY tarihSaat DESC")
    fun getTumSatislar(): Flow<List<SatisGecmisiEntity>>
}