package id.hash.tirayin.dao

import androidx.room.*
import id.hash.tirayin.model.Types
import id.hash.tirayin.model.Usages

@Dao
interface UsageDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(item: Usages)

    @Update
    suspend fun update(item: Usages)

    @Delete
    suspend fun delete(item: Usages)

    @Query("SELECT * FROM usage")
    suspend fun getAllItems(): List<Usages>

}