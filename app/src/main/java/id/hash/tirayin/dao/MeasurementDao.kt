package id.hash.tirayin.dao

import androidx.room.*
import id.hash.tirayin.model.Measurements
import id.hash.tirayin.model.Types
import id.hash.tirayin.model.Variants

@Dao
interface MeasurementDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(item: Measurements)

    @Update
    suspend fun update(item: Measurements)

    @Delete
    suspend fun delete(item: Measurements)

    @Query("SELECT * FROM measurement")
    suspend fun getAllItems(): List<Measurements>

}