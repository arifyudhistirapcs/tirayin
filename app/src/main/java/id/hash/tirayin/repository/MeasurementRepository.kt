package id.hash.tirayin.repository

import id.hash.tirayin.dao.MeasurementDao
import id.hash.tirayin.model.Measurements
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class MeasurementRepository(private val dao: MeasurementDao) {
    suspend fun insert(item: Measurements) = withContext(Dispatchers.IO) {
        dao.insert(item)
    }

    suspend fun update(item: Measurements) = withContext(Dispatchers.IO) {
        dao.update(item)
    }

    suspend fun delete(item: Measurements) = withContext(Dispatchers.IO) {
        dao.delete(item)
    }

    suspend fun getAllItems(): List<Measurements> = withContext(Dispatchers.IO) {
        dao.getAllItems()
    }
}