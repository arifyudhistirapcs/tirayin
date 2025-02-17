package id.hash.tirayin.dao

import androidx.room.*
import id.hash.tirayin.model.Types

@Dao
interface TypeDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(item: Types)

    @Update
    suspend fun update(item: Types)

    @Delete
    suspend fun delete(item: Types)

    @Query("SELECT * FROM type")
    suspend fun getAllItems(): List<Types>

}