package id.hash.tirayin.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "usage")
data class Usages(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
)