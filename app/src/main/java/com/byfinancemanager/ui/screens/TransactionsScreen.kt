package com.byfinancemanager.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.FilterList
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransactionsScreen(
    viewModel: FinanceViewModel
) {
    val filtered by viewModel.filteredTransactions.collectAsState()
    val selectedPeriod by viewModel.selectedPeriod.collectAsState()
    val selectedType by viewModel.selectedTypeFilter.collectAsState()
    val all by viewModel.allTransactions.collectAsState()

    var showFilterSheet by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Операции", fontWeight = FontWeight.Bold) },
                actions = {
                    IconButton(onClick = { showFilterSheet = true }) {
                        Icon(Icons.Default.FilterList, contentDescription = "Фильтр")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp)
        ) {
            // Chips фильтров
            Row(
                modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FinanceViewModel.Period.values().forEach { period ->
                    FilterChip(
                        selected = selectedPeriod == period,
                        onClick = { viewModel.setPeriod(period) },
                        label = { Text(period.displayName, fontSize = 12.sp) }
                    )
                }
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(
                    selected = selectedType == null,
                    onClick = { viewModel.setTypeFilter(null) },
                    label = { Text("Все", fontSize = 12.sp) }
                )
                FilterChip(
                    selected = selectedType == TransactionType.INCOME,
                    onClick = { viewModel.setTypeFilter(TransactionType.INCOME) },
                    label = { Text("Доходы", fontSize = 12.sp) }
                )
                FilterChip(
                    selected = selectedType == TransactionType.EXPENSE,
                    onClick = { viewModel.setTypeFilter(TransactionType.EXPENSE) },
                    label = { Text("Расходы", fontSize = 12.sp) }
                )
            }

            Spacer(modifier = Modifier.height(8.dp))
            Text("${filtered.size} из ${all.size} операций", fontSize = 12.sp, color = Color.Gray)

            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxSize().padding(top = 8.dp)
            ) {
                items(filtered, key = { it.id }) { tx ->
                    var showDeleteConfirm by remember { mutableStateOf(false) }
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp).fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
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
                            IconButton(onClick = { showDeleteConfirm = true }) {
                                Icon(Icons.Default.Delete, contentDescription = "Удалить", tint = Color.Gray)
                            }
                        }
                    }
                    if (showDeleteConfirm) {
                        AlertDialog(
                            onDismissRequest = { showDeleteConfirm = false },
                            title = { Text("Удалить операцию?") },
                            text = { Text("Вы уверены? Это действие нельзя отменить.") },
                            confirmButton = {
                                TextButton(onClick = {
                                    viewModel.deleteTransaction(tx)
                                    showDeleteConfirm = false
                                }) { Text("Удалить", color = Color.Red) }
                            },
                            dismissButton = {
                                TextButton(onClick = { showDeleteConfirm = false }) { Text("Отмена") }
                            }
                        )
                    }
                }
                item { Spacer(modifier = Modifier.height(80.dp)) }
            }
        }
    }

    if (showFilterSheet) {
        ModalBottomSheet(onDismissRequest = { showFilterSheet = false }) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("Фильтры", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                Text("Период:", fontWeight = FontWeight.Medium)
                FinanceViewModel.Period.values().forEach { period ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = selectedPeriod == period,
                            onClick = { viewModel.setPeriod(period) }
                        )
                        Text(period.displayName)
                    }
                }
                Divider()
                Text("Тип:", fontWeight = FontWeight.Medium)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    RadioButton(selected = selectedType == null, onClick = { viewModel.setTypeFilter(null) })
                    Text("Все")
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    RadioButton(selected = selectedType == TransactionType.INCOME, onClick = { viewModel.setTypeFilter(TransactionType.INCOME) })
                    Text("Только доходы")
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    RadioButton(selected = selectedType == TransactionType.EXPENSE, onClick = { viewModel.setTypeFilter(TransactionType.EXPENSE) })
                    Text("Только расходы")
                }
                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }
}
