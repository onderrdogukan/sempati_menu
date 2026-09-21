package com.sempati.app.data.repository

import com.sempati.app.data.dao.SatisGecmisiDao
import com.sempati.app.data.entity.SatisGecmisiEntity
import com.sempati.app.domain.model.SatisGecmisi
import com.sempati.app.domain.repository.SatisGecmisiRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class SatisGecmisiRepositoryImpl(
    private val satisGecmisiDao: SatisGecmisiDao
) : SatisGecmisiRepository {

    override suspend fun satisKaydet(satis: SatisGecmisi) {
        val entity = SatisGecmisiEntity(
            id = satis.id,
            masaId = satis.masaId,
            hesaplananTutar = satis.hesaplananTutar,
            alinanTutar = satis.alinanTutar,
            tarihSaat = satis.tarihSaat
        )
        satisGecmisiDao.satisEkle(entity)
    }

    override fun getTumSatislar(): Flow<List<SatisGecmisi>> {
        return satisGecmisiDao.getTumSatislar().map { entityList ->
            entityList.map { entity ->
                SatisGecmisi(
                    id = entity.id,
                    masaId = entity.masaId,
                    hesaplananTutar = entity.hesaplananTutar,
                    alinanTutar = entity.alinanTutar,
                    tarihSaat = entity.tarihSaat
                )
            }
        }
    }
}