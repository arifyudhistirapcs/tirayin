package id.hash.tirayin.repository

import id.hash.tirayin.dao.TypeDao
import id.hash.tirayin.model.Types
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class TypeRepository(private val dao: TypeDao) {
    suspend fun insert(item: Types) = withContext(Dispatchers.IO) {
        dao.insert(item)
    }

    suspend fun update(item: Types) = withContext(Dispatchers.IO) {
        dao.update(item)
    }

    suspend fun delete(item: Types) = withContext(Dispatchers.IO) {
        dao.delete(item)
    }

    suspend fun getAllItems(): List<Types> = withContext(Dispatchers.IO) {
        dao.getAllItems()
    }

}