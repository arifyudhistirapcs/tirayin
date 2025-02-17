package id.hash.tirayin.repository

import id.hash.tirayin.dao.UsageDao
import id.hash.tirayin.model.Types
import id.hash.tirayin.model.Usages
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class UsageRepository(private val dao: UsageDao) {
    suspend fun insert(item: Usages) = withContext(Dispatchers.IO) {
        dao.insert(item)
    }

    suspend fun update(item: Usages) = withContext(Dispatchers.IO) {
        dao.update(item)
    }

    suspend fun delete(item: Usages) = withContext(Dispatchers.IO) {
        dao.delete(item)
    }

    suspend fun getAllItems(): List<Usages> = withContext(Dispatchers.IO) {
        dao.getAllItems()
    }
}