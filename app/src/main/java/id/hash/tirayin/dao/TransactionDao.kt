package id.hash.tirayin.dao

import androidx.room.*
import id.hash.tirayin.model.Transactions
import id.hash.tirayin.model.Types
import id.hash.tirayin.model.Variants

@Dao
interface TransactionDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(item: Transactions)

    @Update
    suspend fun update(item: Transactions)

    @Delete
    suspend fun delete(item: Transactions)

    @Query("SELECT * FROM `transaction`")
    suspend fun getAllItems(): List<Transactions>

}