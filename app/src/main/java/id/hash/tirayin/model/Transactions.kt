package id.hash.tirayin.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "transaction")
data class Transactions(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val status: String,
    val name: String,
    val date: Long,
    val variants: List<Variants>?
)