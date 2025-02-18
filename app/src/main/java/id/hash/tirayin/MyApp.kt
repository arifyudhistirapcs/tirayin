package id.hash.tirayin

import id.hash.tirayin.db.MyDatabase
import id.hash.tirayin.repository.VariantRepository
import android.app.Application
import id.hash.tirayin.repository.ObjectRepository
import id.hash.tirayin.repository.TransactionRepository
import id.hash.tirayin.repository.TypeRepository
import id.hash.tirayin.repository.UsageRepository

class MyApp : Application() {

    lateinit var database: MyDatabase
        private set

    lateinit var variantRepository: VariantRepository
        private set
    lateinit var typeRepository: TypeRepository
        private set
    lateinit var objectRepository: ObjectRepository
        private set
    lateinit var usageRepository: UsageRepository
        private set
    lateinit var transactionRepository: TransactionRepository
        private set

    override fun onCreate() {
        super.onCreate()
        database = MyDatabase.getDatabase(this)
        variantRepository = VariantRepository(database.variantDao())
        typeRepository = TypeRepository(database.typeDao())
        objectRepository = ObjectRepository(database.objectDao())
        usageRepository = UsageRepository(database.usageDao())
        transactionRepository = TransactionRepository(database.transactionDao())
    }
}