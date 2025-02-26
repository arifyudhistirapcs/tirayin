package id.hash.tirayin.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "log")
data class Logs(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val variantName : String,
    val variantCode: String,
    val date : String,
    val qty1Before : Int,
    val qty1After : Int,
    val qty2Before : Int,
    val qty2After : Int,
    val from : String,
    val trxCode : String? = ""

)