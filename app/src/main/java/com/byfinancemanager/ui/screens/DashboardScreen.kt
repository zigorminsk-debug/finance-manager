@file:OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)

package com.byfinancemanager.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.byfinancemanager.data.local.TransactionType
import com.byfinancemanager.ui.viewmodel.FinanceViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    viewModel: FinanceViewModel,
    onAddTransaction: () -> Unit,
    onTransactionClick: (Long) -> Unit = {}
) {
    val balanceByn by viewModel.balanceByn.collectAsState()
    val balanceUsd by viewModel.balanceUsd.collectAsState()
    val incomeByn by viewModel.totalIncomeByn.collectAsState()
    val expenseByn by viewModel.totalExpenseByn.collectAsState()
    val incomeUsd by viewModel.totalIncomeUsd.collectAsState()
    val expenseUsd by viewModel.totalExpenseUsd.collectAsState()
    val currentRate by viewModel.currentRate.collectAsState()
    val recentTransactions by viewModel.allTransactions.collectAsState()
    val isLoadingRate by viewModel.isLoadingRate.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("BY Финансы", fontWeight = FontWeight.Bold) },
                actions = {
                    if (isLoadingRate) {
                        CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                        Spacer(modifier = Modifier.width(8.dp))
                    } else {
                        AssistChip(
                            onClick = { viewModel.refreshCurrentRate() },
                            label = { Text("1\$ = ${String.format("%.4f", currentRate)} BYN", fontSize = 12.sp) }
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onAddTransaction) {
                Icon(Icons.Default.Add, contentDescription = "Добавить")
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Баланс карточка
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (balanceByn >= 0) Color(0xFF2E7D32) else Color(0xFFC62828)
                    ),
                    elevation = CardDefaults.cardElevation(8.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text("Общий баланс", color = Color.White.copy(alpha = 0.8f), fontSize = 14.sp)
                        Text(
                            "${String.format("%.2f", balanceByn)} BYN",
                            color = Color.White,
                            fontSize = 28.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            "${String.format("%.2f", balanceUsd)} USD",
                            color = Color.White.copy(alpha = 0.9f),
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            "по курсу НБРБ • ${String.format("%.4f", currentRate)} BYN за 1 USD",
                            color = Color.White.copy(alpha = 0.6f),
                            fontSize = 11.sp
                        )
                    }
                }
            }

            // Доходы / Расходы
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Card(
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFE8F5E9))
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.ArrowUpward, contentDescription = null, tint = Color(0xFF2E7D32), modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Доходы", color = Color(0xFF2E7D32), fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            }
                            Text("${String.format("%.2f", incomeByn)} BYN", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                            Text("${String.format("%.2f", incomeUsd)} USD", fontSize = 12.sp, color = Color.Gray)
                        }
                    }
                    Card(
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFFFEBEE))
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.ArrowDownward, contentDescription = null, tint = Color(0xFFC62828), modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Расходы", color = Color(0xFFC62828), fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            }
                            Text("${String.format("%.2f", expenseByn)} BYN", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                            Text("${String.format("%.2f", expenseUsd)} USD", fontSize = 12.sp, color = Color.Gray)
                        }
                    }
                }
            }

            // Заголовок последние операции
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Последние операции", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                    Text("${recentTransactions.size} всего", fontSize = 12.sp, color = Color.Gray)
                }
            }

            if (recentTransactions.isEmpty()) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(32.dp).fillMaxWidth(),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text("💸", fontSize = 48.sp)
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("Нет операций", fontWeight = FontWeight.Medium)
                            Text("Нажмите + чтобы добавить первую", fontSize = 12.sp, color = Color.Gray)
                        }
                    }
                }
            } else {
                items(recentTransactions.take(15)) { tx ->
                    TransactionItem(
                        amountByn = tx.amountByn,
                        amountUsd = tx.amountUsd,
                        rate = tx.exchangeRate,
                        type = tx.type,
                        date = tx.date,
                        sourceName = viewModel.getSourceName(tx.sourceId),
                        sourceIcon = viewModel.getSourceIcon(tx.sourceId),
                        note = tx.note
                    )
                }
            }
        }
    }
}

@Composable
fun TransactionItem(
    amountByn: Double,
    amountUsd: Double,
    rate: Double,
    type: TransactionType,
    date: Long,
    sourceName: String,
    sourceIcon: String,
    note: String
) {
    val dateFormat = remember { SimpleDateFormat("dd MMM yyyy, HH:mm", Locale("ru")) }
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Row(
            modifier = Modifier.padding(12.dp).fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = if (type == TransactionType.INCOME) Color(0xFFE8F5E9) else Color(0xFFFFEBEE),
                modifier = Modifier.size(44.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(sourceIcon, fontSize = 20.sp)
                }
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(sourceName, fontWeight = FontWeight.Medium, fontSize = 14.sp, maxLines = 1)
                if (note.isNotBlank()) {
                    Text(note, fontSize = 12.sp, color = Color.Gray, maxLines = 1)
                }
                Text(dateFormat.format(Date(date)), fontSize = 11.sp, color = Color.Gray)
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    "${if (type == TransactionType.INCOME) "+" else "-"}${String.format("%.2f", amountByn)} BYN",
                    fontWeight = FontWeight.Bold,
                    color = if (type == TransactionType.INCOME) Color(0xFF2E7D32) else Color(0xFFC62828),
                    fontSize = 14.sp
                )
                Text(
                    "${String.format("%.2f", amountUsd)} USD",
                    fontSize = 11.sp,
                    color = Color.Gray
                )
                Text(
                    "курс ${String.format("%.4f", rate)}",
                    fontSize = 9.sp,
                    color = Color.Gray.copy(alpha = 0.7f)
                )
            }
        }
    }
}
