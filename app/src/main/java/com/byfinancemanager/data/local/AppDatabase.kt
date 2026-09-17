package com.byfinancemanager.data.local

import android.content.Context
import androidx.room.*
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [Transaction::class, Source::class],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun transactionDao(): TransactionDao
    abstract fun sourceDao(): SourceDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context, scope: CoroutineScope): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "finance_manager_db"
                )
                    .addCallback(DatabaseCallback(context.applicationContext, scope))
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }

    private class DatabaseCallback(
        private val context: Context,
        private val scope: CoroutineScope
    ) : RoomDatabase.Callback() {
        override fun onCreate(db: SupportSQLiteDatabase) {
            super.onCreate(db)
            // Попытка через INSTANCE, если null - через getDatabase
            scope.launch(Dispatchers.IO) {
                try {
                    val database = INSTANCE ?: getDatabase(context, this)
                    populateDatabase(database.sourceDao())
                } catch (e: Exception) {
                    // Fallback: вставка через прямой SQL если DAO недоступен
                    try {
                        // Вставляем дефолтные источники напрямую через SQL
                        val incomeSources = listOf(
                            Triple("Зарплата", "INCOME", "💼"),
                            Triple("Фриланс", "INCOME", "💻"),
                            Triple("Бизнес", "INCOME", "🏢"),
                            Triple("Инвестиции", "INCOME", "📈"),
                            Triple("Подарки", "INCOME", "🎁"),
                            Triple("Прочие доходы", "INCOME", "💰")
                        )
                        val expenseSources = listOf(
                            Triple("Продукты", "EXPENSE", "🛒"),
                            Triple("Транспорт", "EXPENSE", "🚗"),
                            Triple("Жильё / Коммуналка", "EXPENSE", "🏠"),
                            Triple("Здоровье", "EXPENSE", "⚕️"),
                            Triple("Развлечения", "EXPENSE", "🎮"),
                            Triple("Одежда", "EXPENSE", "👕"),
                            Triple("Кафе / Рестораны", "EXPENSE", "🍽️"),
                            Triple("Связь / Интернет", "EXPENSE", "📱"),
                            Triple("Образование", "EXPENSE", "📚"),
                            Triple("Прочие расходы", "EXPENSE", "💸")
                        )
                        (incomeSources + expenseSources).forEach { (name, type, icon) ->
                            try {
                                db.execSQL(
                                    "INSERT INTO sources (name, type, icon, color, isDefault, createdAt) VALUES (?, ?, ?, ?, ?, ?)",
                                    arrayOf(name, type, icon, 0xFF4CAF50L, 1, System.currentTimeMillis())
                                )
                            } catch (_: Exception) {}
                        }
                    } catch (_: Exception) {}
                }
            }
        }

        suspend fun populateDatabase(sourceDao: SourceDao) {
            // Доходы по умолчанию
            val incomeSources = listOf(
                Source(name = "Зарплата", type = TransactionType.INCOME, icon = "💼", color = 0xFF4CAF50, isDefault = true),
                Source(name = "Фриланс", type = TransactionType.INCOME, icon = "💻", color = 0xFF2196F3, isDefault = true),
                Source(name = "Бизнес", type = TransactionType.INCOME, icon = "🏢", color = 0xFF9C27B0, isDefault = true),
                Source(name = "Инвестиции", type = TransactionType.INCOME, icon = "📈", color = 0xFFFF9800, isDefault = true),
                Source(name = "Подарки", type = TransactionType.INCOME, icon = "🎁", color = 0xFFE91E63, isDefault = true),
                Source(name = "Прочие доходы", type = TransactionType.INCOME, icon = "💰", color = 0xFF009688, isDefault = true)
            )
            // Расходы по умолчанию
            val expenseSources = listOf(
                Source(name = "Продукты", type = TransactionType.EXPENSE, icon = "🛒", color = 0xFF4CAF50, isDefault = true),
                Source(name = "Транспорт", type = TransactionType.EXPENSE, icon = "🚗", color = 0xFF2196F3, isDefault = true),
                Source(name = "Жильё / Коммуналка", type = TransactionType.EXPENSE, icon = "🏠", color = 0xFF795548, isDefault = true),
                Source(name = "Здоровье", type = TransactionType.EXPENSE, icon = "⚕️", color = 0xFFF44336, isDefault = true),
                Source(name = "Развлечения", type = TransactionType.EXPENSE, icon = "🎮", color = 0xFF9C27B0, isDefault = true),
                Source(name = "Одежда", type = TransactionType.EXPENSE, icon = "👕", color = 0xFF607D8B, isDefault = true),
                Source(name = "Кафе / Рестораны", type = TransactionType.EXPENSE, icon = "🍽️", color = 0xFFFF5722, isDefault = true),
                Source(name = "Связь / Интернет", type = TransactionType.EXPENSE, icon = "📱", color = 0xFF00BCD4, isDefault = true),
                Source(name = "Образование", type = TransactionType.EXPENSE, icon = "📚", color = 0xFF3F51B5, isDefault = true),
                Source(name = "Прочие расходы", type = TransactionType.EXPENSE, icon = "💸", color = 0xFF9E9E9E, isDefault = true)
            )
            (incomeSources + expenseSources).forEach { sourceDao.insert(it) }
        }
    }
}

@Dao
interface TransactionDao {
    @Query("SELECT * FROM transactions ORDER BY date DESC, id DESC")
    fun getAllTransactions(): kotlinx.coroutines.flow.Flow<List<Transaction>>

    @Query("SELECT * FROM transactions WHERE type = :type ORDER BY date DESC")
    fun getTransactionsByType(type: TransactionType): kotlinx.coroutines.flow.Flow<List<Transaction>>

    @Query("SELECT * FROM transactions WHERE date BETWEEN :start AND :end ORDER BY date DESC")
    fun getTransactionsByDateRange(start: Long, end: Long): kotlinx.coroutines.flow.Flow<List<Transaction>>

    @Query("SELECT SUM(amountByn) FROM transactions WHERE type = :type")
    fun getTotalBynByType(type: TransactionType): kotlinx.coroutines.flow.Flow<Double?>

    @Query("SELECT SUM(amountUsd) FROM transactions WHERE type = :type")
    fun getTotalUsdByType(type: TransactionType): kotlinx.coroutines.flow.Flow<Double?>

    @Query("SELECT * FROM transactions WHERE id = :id")
    suspend fun getById(id: Long): Transaction?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(transaction: Transaction): Long

    @Update
    suspend fun update(transaction: Transaction)

    @Delete
    suspend fun delete(transaction: Transaction)

    @Query("DELETE FROM transactions WHERE id = :id")
    suspend fun deleteById(id: Long)
}

@Dao
interface SourceDao {
    @Query("SELECT * FROM sources ORDER BY name ASC")
    fun getAllSources(): kotlinx.coroutines.flow.Flow<List<Source>>

    @Query("SELECT * FROM sources WHERE type = :type ORDER BY name ASC")
    fun getSourcesByType(type: TransactionType): kotlinx.coroutines.flow.Flow<List<Source>>

    @Query("SELECT * FROM sources WHERE id = :id")
    suspend fun getById(id: Long): Source?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(source: Source): Long

    @Update
    suspend fun update(source: Source)

    @Delete
    suspend fun delete(source: Source)

    @Query("DELETE FROM sources WHERE id = :id")
    suspend fun deleteById(id: Long)
}

class Converters {
    @TypeConverter
    fun fromTransactionType(type: TransactionType): String = type.name

    @TypeConverter
    fun toTransactionType(name: String): TransactionType = TransactionType.valueOf(name)
}
