package id.hash.tirayin.repository

import id.hash.tirayin.dao.TransactionDao
import id.hash.tirayin.dao.VariantDao
import id.hash.tirayin.model.Transactions
import id.hash.tirayin.model.Types
import id.hash.tirayin.model.Variants
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class TransactionRepository(private val dao: TransactionDao) {
    suspend fun insert(item: Transactions) = withContext(Dispatchers.IO) {
        dao.insert(item)
    }

    suspend fun update(item: Transactions) = withContext(Dispatchers.IO) {
        dao.update(item)
    }

    suspend fun delete(item: Transactions) = withContext(Dispatchers.IO) {
        dao.delete(item)
    }

    suspend fun getAllItems(): List<Transactions> = withContext(Dispatchers.IO) {
        dao.getAllItems()
    }

}