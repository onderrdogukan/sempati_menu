package com.sempati.app.data

import androidx.room.Database
import androidx.room.RoomDatabase
import com.sempati.app.data.dao.HesapKalemiDao
import com.sempati.app.data.dao.MasaDao
import com.sempati.app.data.dao.SatisGecmisiDao
import com.sempati.app.data.dao.UrunDao
import com.sempati.app.data.entity.HesapKalemiEntity
import com.sempati.app.data.entity.MasaEntity
import com.sempati.app.data.entity.SatisGecmisiEntity
import com.sempati.app.data.entity.UrunEntity

@Database(
    entities = [
        MasaEntity::class,
        UrunEntity::class,
        HesapKalemiEntity::class,
        SatisGecmisiEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    // Veritabanı motoru bu fonksiyonların içini kendisi dolduracak (KAPT sayesinde)
    abstract fun urunDao(): UrunDao
    abstract fun masaDao(): MasaDao
    abstract fun hesapKalemiDao(): HesapKalemiDao
    abstract fun satisGecmisiDao(): SatisGecmisiDao
}