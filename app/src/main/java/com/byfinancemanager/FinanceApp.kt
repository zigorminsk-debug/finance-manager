package com.byfinancemanager

import android.app.Application
import com.byfinancemanager.data.local.AppDatabase
import com.byfinancemanager.data.repository.FinanceRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob

class FinanceApp : Application() {
    val applicationScope = CoroutineScope(SupervisorJob())

    val database by lazy {
        AppDatabase.getDatabase(this, applicationScope)
    }

    val repository by lazy {
        FinanceRepository(
            transactionDao = database.transactionDao(),
            sourceDao = database.sourceDao(),
            context = this
        )
    }
}
