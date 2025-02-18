package id.hash.tirayin.converter

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import id.hash.tirayin.model.Variants

class Converters {

    private val gson = Gson()

    @TypeConverter
    fun fromVariantsList(variants: List<Variants>?): String {
        return gson.toJson(variants)
    }

    @TypeConverter
    fun toVariantsList(variantsJson: String?): List<Variants>? {
        return variantsJson?.let {
            val type = object : TypeToken<List<Variants>>() {}.type
            gson.fromJson(it, type)
        }
    }
}
