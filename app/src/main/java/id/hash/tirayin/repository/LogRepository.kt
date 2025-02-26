package id.hash.tirayin.repository

import id.hash.tirayin.dao.LogDao
import id.hash.tirayin.model.Types
import id.hash.tirayin.model.Logs
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class LogRepository(private val dao: LogDao) {
    suspend fun insert(item: Logs) = withContext(Dispatchers.IO) {
        dao.insert(item)
    }

    suspend fun update(item: Logs) = withContext(Dispatchers.IO) {
        dao.update(item)
    }

    suspend fun delete(item: Logs) = withContext(Dispatchers.IO) {
        dao.delete(item)
    }

    suspend fun getAllItems(): List<Logs> = withContext(Dispatchers.IO) {
        dao.getAllItems()
    }

}