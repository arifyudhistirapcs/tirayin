package id.hash.tirayin.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "object")
data class Objects(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val usageName : String
)