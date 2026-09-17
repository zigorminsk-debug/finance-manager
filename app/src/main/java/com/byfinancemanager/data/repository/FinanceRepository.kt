package com.byfinancemanager.data.repository

import android.content.Context
import android.util.Log
import com.byfinancemanager.data.local.*
import com.byfinancemanager.data.remote.NbrbApi
import com.byfinancemanager.data.remote.NbrbRate
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import java.text.SimpleDateFormat
import java.util.*
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import java.util.concurrent.TimeUnit

class FinanceRepository(
    private val transactionDao: TransactionDao,
    private val sourceDao: SourceDao,
    private val context: Context
) {
    private val nbrbApi: NbrbApi by lazy {
        val logging = HttpLoggingInterceptor().apply { level = HttpLoggingInterceptor.Level.BASIC }
        val client = OkHttpClient.Builder()
            .addInterceptor(logging)
            .connectTimeout(10, TimeUnit.SECONDS)
            .readTimeout(10, TimeUnit.SECONDS)
            .build()
        Retrofit.Builder()
            .baseUrl("https://api.nbrb.by/")
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(NbrbApi::class.java)
    }

    // Transactions
    fun getAllTransactions(): Flow<List<Transaction>> = transactionDao.getAllTransactions()
    fun getTransactionsByType(type: TransactionType) = transactionDao.getTransactionsByType(type)
    fun getTransactionsByDateRange(start: Long, end: Long) = transactionDao.getTransactionsByDateRange(start, end)
    fun getTotalBynByType(type: TransactionType) = transactionDao.getTotalBynByType(type)
    fun getTotalUsdByType(type: TransactionType) = transactionDao.getTotalUsdByType(type)

    suspend fun insertTransaction(transaction: Transaction): Long = transactionDao.insert(transaction)
    suspend fun updateTransaction(transaction: Transaction) = transactionDao.update(transaction)
    suspend fun deleteTransaction(transaction: Transaction) = transactionDao.delete(transaction)
    suspend fun deleteTransactionById(id: Long) = transactionDao.deleteById(id)
    suspend fun getTransactionById(id: Long) = transactionDao.getById(id)

    // Sources
    fun getAllSources(): Flow<List<Source>> = sourceDao.getAllSources()
    fun getSourcesByType(type: TransactionType) = sourceDao.getSourcesByType(type)
    suspend fun getSourceById(id: Long) = sourceDao.getById(id)
    suspend fun insertSource(source: Source): Long = sourceDao.insert(source)
    suspend fun updateSource(source: Source) = sourceDao.update(source)
    suspend fun deleteSource(source: Source) = sourceDao.delete(source)
    suspend fun deleteSourceById(id: Long) = sourceDao.deleteById(id)

    // NBRB
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.US)
    private val prefs by lazy { context.getSharedPreferences("nbrb_cache", Context.MODE_PRIVATE) }

    suspend fun fetchUsdRateForDate(timestamp: Long): Double {
        val dateStr = dateFormat.format(Date(timestamp))
        // Try cache first
        val cachedKey = "rate_$dateStr"
        val cached = prefs.getFloat(cachedKey, -1f)
        if (cached != -1f) {
            return cached.toDouble()
        }

        return try {
            // Пробуем несколько вариантов API
            val rateResponse: NbrbRate = try {
                nbrbApi.getUsdRateByDate(dateStr)
            } catch (e: Exception) {
                Log.w("FinanceRepo", "USD param attempt failed: ${e.message}, trying ID 431")
                nbrbApi.getUsdRateByIdAndDate(dateStr)
            }
            val rate = rateResponse.officialRate ?: throw Exception("Rate null")
            val scale = rateResponse.scale
            val normalizedRate = rate / scale // BYN per 1 USD, если scale=1 то rate, если больше - делим
            // Actually officialRate is for scale, so per 1 USD = rate/scale
            // But NBRB returns rate for scale, so 1 USD = officialRate / scale? No, officialRate is for scale units. For USD scale=1, so it's direct.
            // To be safe: rate per 1 USD = officialRate / scale
            val perOne = rate / scale
            // Cache
            prefs.edit().putFloat(cachedKey, perOne.toFloat()).apply()
            // Also cache as last known
            prefs.edit().putFloat("last_rate", perOne.toFloat()).apply()
            perOne
        } catch (e: Exception) {
            Log.e("FinanceRepo", "Failed to fetch NBRB rate for $dateStr: ${e.message}")
            // Try last known rate or fallback
            val last = prefs.getFloat("last_rate", -1f)
            if (last != -1f) last.toDouble() else 3.2 // fallback примерный курс
        }
    }

    suspend fun fetchCurrentUsdRate(): Double {
        return try {
            val resp = nbrbApi.getCurrentUsdRate()
            val rate = resp.officialRate ?: 3.2
            rate / resp.scale
        } catch (e: Exception) {
            Log.e("FinanceRepo", "Failed current rate: ${e.message}")
            prefs.getFloat("last_rate", 3.2f).toDouble()
        }
    }

    fun calculateUsdAmount(bynAmount: Double, rate: Double): Double {
        if (rate == 0.0) return 0.0
        return bynAmount / rate
    }

    suspend fun getBalance(): Pair<Double, Double> {
        val incomeByn = transactionDao.getTotalBynByType(TransactionType.INCOME).first() ?: 0.0
        val expenseByn = transactionDao.getTotalBynByType(TransactionType.EXPENSE).first() ?: 0.0
        val incomeUsd = transactionDao.getTotalUsdByType(TransactionType.INCOME).first() ?: 0.0
        val expenseUsd = transactionDao.getTotalUsdByType(TransactionType.EXPENSE).first() ?: 0.0
        return Pair(incomeByn - expenseByn, incomeUsd - expenseUsd)
    }
}
