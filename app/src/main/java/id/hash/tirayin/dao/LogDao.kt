package id.hash.tirayin.dao

import androidx.room.*
import id.hash.tirayin.model.Logs
import id.hash.tirayin.model.Transactions
import id.hash.tirayin.model.Types
import id.hash.tirayin.model.Variants

@Dao
interface LogDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(item: Logs)

    @Update
    suspend fun update(item: Logs)

    @Delete
    suspend fun delete(item: Logs)

    @Query("SELECT * FROM `log`")
    suspend fun getAllItems(): List<Logs>

}