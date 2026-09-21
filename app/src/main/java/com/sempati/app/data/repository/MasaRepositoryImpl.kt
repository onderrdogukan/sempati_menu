package com.sempati.app.data.repository

import com.sempati.app.data.dao.MasaDao
import com.sempati.app.data.entity.MasaEntity
import com.sempati.app.domain.model.Masa
import com.sempati.app.domain.model.MasaDurumu
import com.sempati.app.domain.repository.MasaRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class MasaRepositoryImpl(
    private val masaDao: MasaDao
) : MasaRepository {

    override fun tumMasalariGetir(): Flow<List<Masa>> {
        return masaDao.tumMasalariGetir().map { entityList: List<MasaEntity> ->
            entityList.map { entity: MasaEntity ->
                Masa(
                    id = entity.id,
                    ad = entity.ad,
                    durum = MasaDurumu.valueOf(entity.durum)
                )
            }
        }
    }

    override suspend fun masaEkle(masa: Masa) {
        val entity = MasaEntity(
            id = masa.id,
            ad = masa.ad,
            durum = masa.durum.name
        )
        masaDao.masaEkle(entity)
    }

    override suspend fun masaSil(masa: Masa) {
        val entity = MasaEntity(
            id = masa.id,
            ad = masa.ad,
            durum = masa.durum.name
        )
        masaDao.masaSil(entity)
    }

    override suspend fun durumGuncelle(masaId: Int, durum: MasaDurumu) {
        masaDao.durumGuncelle(masaId, durum.name)
    }

    override suspend fun getMasaById(id: Int): Masa? {
        val entity = masaDao.getMasaById(id) ?: return null
        return Masa(
            id = entity.id,
            ad = entity.ad,
            durum = MasaDurumu.valueOf(entity.durum)
        )
    }
}