package id.hash.tirayin.db

import id.hash.tirayin.dao.VariantDao
import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverter
import androidx.room.TypeConverters
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import id.hash.tirayin.converter.Converters
import id.hash.tirayin.dao.ObjectDao
import id.hash.tirayin.dao.TransactionDao
import id.hash.tirayin.dao.TypeDao
import id.hash.tirayin.dao.UsageDao
import id.hash.tirayin.model.Objects
import id.hash.tirayin.model.Transactions
import id.hash.tirayin.model.Types
import id.hash.tirayin.model.Usages
import id.hash.tirayin.model.Variants

@Database(entities = [Variants::class,Types::class,Objects::class,Usages::class,Transactions::class], version = 1, exportSchema = false)
@TypeConverters(Converters::class)
abstract class MyDatabase : RoomDatabase() {
    abstract fun variantDao(): VariantDao
    abstract fun typeDao(): TypeDao
    abstract fun objectDao(): ObjectDao
    abstract fun usageDao(): UsageDao
    abstract fun transactionDao(): TransactionDao

    companion object {
        @Volatile
        private var INSTANCE: MyDatabase? = null

        fun getDatabase(context: Context): MyDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    MyDatabase::class.java,
                    "inventory.db"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}



