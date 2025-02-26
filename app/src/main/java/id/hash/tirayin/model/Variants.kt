package id.hash.tirayin.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "variant")
data class Variants(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    var qty1: Int,
    val msr1: String,
    val xqty2: Int,
    val qty2 : Int,
    val msr2 : String,
    val usageName : String,
    val objectName : String,
    val typeName : String,
    val qrCode : String,
    val uses : String? = "",
    val condition : String? = ""
)