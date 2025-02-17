package id.hash.tirayin.dao

import androidx.room.*
import id.hash.tirayin.model.Objects
import id.hash.tirayin.model.Types

@Dao
interface ObjectDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(item: Objects)

    @Update
    suspend fun update(item: Objects)

    @Delete
    suspend fun delete(item: Objects)

    @Query("SELECT * FROM object")
    suspend fun getAllItems(): List<Objects>

}