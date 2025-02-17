package id.hash.tirayin.repository

import id.hash.tirayin.dao.ObjectDao
import id.hash.tirayin.model.Objects
import id.hash.tirayin.model.Types
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class ObjectRepository(private val dao: ObjectDao) {
    suspend fun insert(item: Objects) = withContext(Dispatchers.IO) {
        dao.insert(item)
    }

    suspend fun update(item: Objects) = withContext(Dispatchers.IO) {
        dao.update(item)
    }

    suspend fun delete(item: Objects) = withContext(Dispatchers.IO) {
        dao.delete(item)
    }

    suspend fun getAllItems(): List<Objects> = withContext(Dispatchers.IO) {
        dao.getAllItems()
    }

}