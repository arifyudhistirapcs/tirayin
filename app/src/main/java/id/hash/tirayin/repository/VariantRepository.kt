package id.hash.tirayin.repository

import id.hash.tirayin.dao.VariantDao
import id.hash.tirayin.model.Types
import id.hash.tirayin.model.Variants
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class VariantRepository(private val dao: VariantDao) {
    suspend fun insert(item: Variants) = withContext(Dispatchers.IO) {
        dao.insert(item)
    }

    suspend fun update(item: Variants) = withContext(Dispatchers.IO) {
        dao.update(item)
    }

    suspend fun delete(item: Variants) = withContext(Dispatchers.IO) {
        dao.delete(item)
    }

    suspend fun getAllItems(): List<Variants> = withContext(Dispatchers.IO) {
        dao.getAllItems()
    }

    suspend fun getVariantsByTypeName(typeName: String): List<Variants> {
        return dao.getByTypeName(typeName)
    }
}