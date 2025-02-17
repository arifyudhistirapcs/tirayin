package id.hash.tirayin.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey

@Entity(tableName = "type")
data class Types(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val usageName : String = "",
    val objectName : String = ""

)