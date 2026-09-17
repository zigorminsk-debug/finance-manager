package com.byfinancemanager.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.byfinancemanager.data.local.*
import com.byfinancemanager.data.repository.FinanceRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.Calendar

class FinanceViewModel(
    application: Application,
    private val repository: FinanceRepository
) : AndroidViewModel(application) {

    // Sources
    val allSources: StateFlow<List<Source>> = repository.getAllSources()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val incomeSources: StateFlow<List<Source>> = repository.getSourcesByType(TransactionType.INCOME)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val expenseSources: StateFlow<List<Source>> = repository.getSourcesByType(TransactionType.EXPENSE)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Transactions
    val allTransactions: StateFlow<List<Transaction>> = repository.getAllTransactions()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Totals
    val totalIncomeByn: StateFlow<Double> = repository.getTotalBynByType(TransactionType.INCOME)
        .map { it ?: 0.0 }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    val totalExpenseByn: StateFlow<Double> = repository.getTotalBynByType(TransactionType.EXPENSE)
        .map { it ?: 0.0 }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    val totalIncomeUsd: StateFlow<Double> = repository.getTotalUsdByType(TransactionType.INCOME)
        .map { it ?: 0.0 }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    val totalExpenseUsd: StateFlow<Double> = repository.getTotalUsdByType(TransactionType.EXPENSE)
        .map { it ?: 0.0 }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    val balanceByn: StateFlow<Double> = combine(totalIncomeByn, totalExpenseByn) { inc, exp -> inc - exp }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    val balanceUsd: StateFlow<Double> = combine(totalIncomeUsd, totalExpenseUsd) { inc, exp -> inc - exp }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    // Current USD rate
    private val _currentRate = MutableStateFlow(3.2)
    val currentRate: StateFlow<Double> = _currentRate.asStateFlow()

    private val _isLoadingRate = MutableStateFlow(false)
    val isLoadingRate: StateFlow<Boolean> = _isLoadingRate.asStateFlow()

    // Filter
    private val _selectedPeriod = MutableStateFlow(Period.ALL_TIME)
    val selectedPeriod: StateFlow<Period> = _selectedPeriod.asStateFlow()

    private val _selectedTypeFilter = MutableStateFlow<TransactionType?>(null)
    val selectedTypeFilter: StateFlow<TransactionType?> = _selectedTypeFilter.asStateFlow()

    val filteredTransactions: StateFlow<List<Transaction>> = combine(
        allTransactions, selectedPeriod, selectedTypeFilter
    ) { transactions, period, typeFilter ->
        var filtered = transactions
        // Type filter
        if (typeFilter != null) {
            filtered = filtered.filter { it.type == typeFilter }
        }
        // Period filter
        filtered = when (period) {
            Period.TODAY -> {
                val start = Calendar.getInstance().apply {
                    set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0); set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0)
                }.timeInMillis
                filtered.filter { it.date >= start }
            }
            Period.WEEK -> {
                val cal = Calendar.getInstance()
                cal.add(Calendar.DAY_OF_YEAR, -7)
                filtered.filter { it.date >= cal.timeInMillis }
            }
            Period.MONTH -> {
                val cal = Calendar.getInstance()
                cal.add(Calendar.MONTH, -1)
                filtered.filter { it.date >= cal.timeInMillis }
            }
            Period.YEAR -> {
                val cal = Calendar.getInstance()
                cal.add(Calendar.YEAR, -1)
                filtered.filter { it.date >= cal.timeInMillis }
            }
            Period.ALL_TIME -> filtered
        }
        filtered
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    init {
        refreshCurrentRate()
    }

    fun refreshCurrentRate() {
        viewModelScope.launch {
            _isLoadingRate.value = true
            try {
                _currentRate.value = repository.fetchCurrentUsdRate()
            } finally {
                _isLoadingRate.value = false
            }
        }
    }

    suspend fun fetchRateForDate(dateMillis: Long): Double {
        _isLoadingRate.value = true
        return try {
            repository.fetchUsdRateForDate(dateMillis)
        } finally {
            _isLoadingRate.value = false
        }
    }

    fun addTransaction(
        type: TransactionType,
        amountByn: Double,
        amountUsd: Double,
        rate: Double,
        date: Long,
        sourceId: Long?,
        note: String,
        onComplete: () -> Unit = {}
    ) {
        viewModelScope.launch {
            val tx = Transaction(
                type = type,
                amountByn = amountByn,
                amountUsd = amountUsd,
                exchangeRate = rate,
                date = date,
                sourceId = sourceId,
                note = note
            )
            repository.insertTransaction(tx)
            onComplete()
        }
    }

    fun deleteTransaction(transaction: Transaction) {
        viewModelScope.launch {
            repository.deleteTransaction(transaction)
        }
    }

    fun deleteTransactionById(id: Long) {
        viewModelScope.launch {
            repository.deleteTransactionById(id)
        }
    }

    fun addSource(name: String, type: TransactionType, icon: String = "💰", color: Long = 0xFF4CAF50) {
        viewModelScope.launch {
            repository.insertSource(Source(name = name, type = type, icon = icon, color = color))
        }
    }

    fun updateSource(source: Source) {
        viewModelScope.launch {
            repository.updateSource(source)
        }
    }

    fun deleteSource(source: Source) {
        viewModelScope.launch {
            repository.deleteSource(source)
        }
    }

    fun setPeriod(period: Period) {
        _selectedPeriod.value = period
    }

    fun setTypeFilter(type: TransactionType?) {
        _selectedTypeFilter.value = type
    }

    fun getSourceName(sourceId: Long?): String {
        if (sourceId == null) return "Без категории"
        return allSources.value.find { it.id == sourceId }?.name ?: "Без категории"
    }

    fun getSourceIcon(sourceId: Long?): String {
        if (sourceId == null) return "💰"
        return allSources.value.find { it.id == sourceId }?.icon ?: "💰"
    }

    enum class Period(val displayName: String) {
        TODAY("Сегодня"),
        WEEK("Неделя"),
        MONTH("Месяц"),
        YEAR("Год"),
        ALL_TIME("Всё время")
    }
}
