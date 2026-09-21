package com.sempati.app.data.repository

import com.sempati.app.data.dao.UrunDao
import com.sempati.app.data.entity.UrunEntity
import com.sempati.app.domain.model.Urun
import com.sempati.app.domain.repository.UrunRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class UrunRepositoryImpl(
    private val urunDao: UrunDao
) : UrunRepository {

    override fun getAktifUrunler(): Flow<List<Urun>> {
        return urunDao.getAktifUrunler().map { entityList ->
            entityList.map { entity ->
                Urun(
                    id = entity.id,
                    ad = entity.ad,
                    fiyat = entity.fiyat,
                    aktif = entity.aktif
                )
            }
        }
    }

    override suspend fun urunEkle(urun: Urun) {
        val entity = UrunEntity(
            id = urun.id,
            ad = urun.ad,
            fiyat = urun.fiyat,
            aktif = urun.aktif
        )
        urunDao.urunEkle(entity)
    }

    override suspend fun urunGuncelle(urun: Urun) {
        // Domain modelinden (Urun), veritabanı modeline (UrunEntity) dönüştürüyoruz
        val entity = UrunEntity(
            id = urun.id,     // ID sabit kalır, böylece Room veritabanı hangi satırın üzerine yazacağını bilir
            ad = urun.ad,     // Yeni isim
            fiyat = urun.fiyat, // Yeni fiyat
            aktif = urun.aktif
        )
        // Dao'daki @Update fonksiyonunu tetikliyoruz
        urunDao.urunGuncelle(entity)
    }

    override suspend fun urunSil(urunId: Int) {
        urunDao.urunSil(urunId)
    }
}