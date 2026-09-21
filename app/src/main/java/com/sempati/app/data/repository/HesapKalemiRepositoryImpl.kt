package com.sempati.app.data.repository

import com.sempati.app.data.dao.HesapKalemiDao
import com.sempati.app.data.entity.HesapKalemiEntity
import com.sempati.app.domain.model.HesapKalemi
import com.sempati.app.domain.repository.HesapKalemiRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class HesapKalemiRepositoryImpl(
    private val hesapKalemiDao: HesapKalemiDao
) : HesapKalemiRepository {

    override fun getHesapByMasaId(masaId: Int): Flow<List<HesapKalemi>> {
        return hesapKalemiDao.getHesapByMasaId(masaId).map { entityList ->
            entityList.map { entity ->
                HesapKalemi(
                    id = entity.id,
                    masaId = entity.masaId,
                    urunId = entity.urunId,
                    adet = entity.adet,
                    birimFiyatSnapshot = entity.birimFiyatSnapshot,
                    urunAdiSnapshot = entity.urunAdiSnapshot
                )
            }
        }
    }

    override suspend fun hesapKalemiEkle(kalem: HesapKalemi) {
        val entity = HesapKalemiEntity(
            id = kalem.id,
            masaId = kalem.masaId,
            urunId = kalem.urunId,
            adet = kalem.adet,
            birimFiyatSnapshot = kalem.birimFiyatSnapshot,
            urunAdiSnapshot = kalem.urunAdiSnapshot
        )
        hesapKalemiDao.hesapKalemiEkle(entity)
    }

    override suspend fun masaninHesabiniKapat(masaId: Int) {
        hesapKalemiDao.masaninHesabiniKapat(masaId)
    }

    override suspend fun hesapKalemiSil(kalemId: Int) {
        hesapKalemiDao.kalemSil(kalemId)
    }

    override suspend fun kalemGuncelle(kalem: com.sempati.app.domain.model.HesapKalemi) {
        val entity = com.sempati.app.data.entity.HesapKalemiEntity(
            id = kalem.id,
            masaId = kalem.masaId,
            urunId = kalem.urunId,
            urunAdiSnapshot = kalem.urunAdiSnapshot,
            birimFiyatSnapshot = kalem.birimFiyatSnapshot,
            adet = kalem.adet
        )
        hesapKalemiDao.kalemGuncelle(entity)
    }
}