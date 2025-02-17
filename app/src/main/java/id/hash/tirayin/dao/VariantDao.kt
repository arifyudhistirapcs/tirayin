package id.hash.tirayin.dao

import androidx.room.*
import id.hash.tirayin.model.Types
import id.hash.tirayin.model.Variants

@Dao
interface VariantDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(item: Variants)

    @Update
    suspend fun update(item: Variants)

    @Delete
    suspend fun delete(item: Variants)

    @Query("SELECT * FROM variant")
    suspend fun getAllItems(): List<Variants>

    @Query("SELECT * FROM variant WHERE typeName = :typeName")
    suspend fun getByTypeName(typeName: String): List<Variants>

    @Query("SELECT * FROM variant WHERE objectName = :objectName")
    suspend fun getByObjectName(objectName: String): List<Variants>

    @Query("SELECT * FROM variant WHERE usageName = :usageName")
    suspend fun getByUsageName(usageName: String): List<Variants>

}