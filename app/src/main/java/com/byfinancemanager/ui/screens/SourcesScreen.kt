@file:OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)

package com.byfinancemanager.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.byfinancemanager.data.local.Source
import com.byfinancemanager.data.local.TransactionType
import com.byfinancemanager.ui.viewmodel.FinanceViewModel

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun SourcesScreen(viewModel: FinanceViewModel) {
    val incomeSources by viewModel.incomeSources.collectAsState()
    val expenseSources by viewModel.expenseSources.collectAsState()

    var selectedTab by remember { mutableStateOf(0) } // 0 = expenses, 1 = income
    var showAddDialog by remember { mutableStateOf(false) }
    var editingSource by remember { mutableStateOf<Source?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Источники", fontWeight = FontWeight.Bold) })
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { showAddDialog = true }) {
                Icon(Icons.Default.Add, contentDescription = "Добавить источник")
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            TabRow(selectedTabIndex = selectedTab) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = { Text("Расходы (${expenseSources.size})") }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = { Text("Доходы (${incomeSources.size})") }
                )
            }

            val currentList = if (selectedTab == 0) expenseSources else incomeSources

            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFE3F2FD))
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(
                                if (selectedTab == 0) "Категории расходов" else "Источники доходов",
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                            Text(
                                if (selectedTab == 0) "На что вы тратите деньги. Например: Продукты, Транспорт, Жильё."
                                else "Откуда приходят деньги. Например: Зарплата, Фриланс, Бизнес.",
                                fontSize = 12.sp,
                                color = Color.Gray
                            )
                        }
                    }
                }

                items(currentList, key = { it.id }) { source ->
                    SourceItem(
                        source = source,
                        onEdit = { editingSource = source },
                        onDelete = { viewModel.deleteSource(source) }
                    )
                }

                item { Spacer(modifier = Modifier.height(80.dp)) }
            }
        }
    }

    if (showAddDialog) {
        AddSourceDialog(
            type = if (selectedTab == 0) TransactionType.EXPENSE else TransactionType.INCOME,
            onDismiss = { showAddDialog = false },
            onSave = { name, icon, color ->
                viewModel.addSource(name, if (selectedTab == 0) TransactionType.EXPENSE else TransactionType.INCOME, icon, color)
                showAddDialog = false
            }
        )
    }

    editingSource?.let { source ->
        EditSourceDialog(
            source = source,
            onDismiss = { editingSource = null },
            onSave = { updated ->
                viewModel.updateSource(updated)
                editingSource = null
            }
        )
    }
}

@Composable
fun SourceItem(
    source: Source,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    var showDeleteConfirm by remember { mutableStateOf(false) }

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
                shape = RoundedCornerShape(8.dp),
                color = Color(source.color).copy(alpha = 0.2f),
                modifier = Modifier.size(40.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(source.icon, fontSize = 20.sp)
                }
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(source.name, fontWeight = FontWeight.Medium)
                Text(
                    if (source.type == TransactionType.INCOME) "Доход" else "Расход",
                    fontSize = 11.sp,
                    color = Color.Gray
                )
            }
            IconButton(onClick = onEdit) {
                Icon(Icons.Default.Edit, contentDescription = "Редактировать", tint = Color.Gray)
            }
            IconButton(onClick = { showDeleteConfirm = true }) {
                Icon(Icons.Default.Delete, contentDescription = "Удалить", tint = Color.Gray)
            }
        }
    }

    if (showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            title = { Text("Удалить источник?") },
            text = { Text("Источник \"${source.name}\" будет удалён. Транзакции останутся, но станут без категории.") },
            confirmButton = {
                TextButton(onClick = {
                    onDelete()
                    showDeleteConfirm = false
                }) { Text("Удалить", color = Color.Red) }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirm = false }) { Text("Отмена") }
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun AddSourceDialog(
    type: TransactionType,
    onDismiss: () -> Unit,
    onSave: (String, String, Long) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var icon by remember { mutableStateOf(if (type == TransactionType.INCOME) "💰" else "💸") }

    val icons = if (type == TransactionType.INCOME) {
        listOf("💰", "💼", "💻", "🏢", "📈", "🎁", "🏦", "💵", "🪙", "💳")
    } else {
        listOf("🛒", "🚗", "🏠", "⚕️", "🎮", "👕", "🍽️", "📱", "📚", "✈️", "⛽", "🎬", "💊", "🏋️", "🎓")
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (type == TransactionType.INCOME) "Новый источник дохода" else "Новая категория расхода") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Название") },
                    placeholder = { Text(if (type == TransactionType.INCOME) "Например: Зарплата" else "Например: Продукты") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Text("Иконка:", fontSize = 12.sp, fontWeight = FontWeight.Medium)
                androidx.compose.foundation.layout.FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    icons.forEach { ic ->
                        FilterChip(
                            selected = icon == ic,
                            onClick = { icon = ic },
                            label = { Text(ic, fontSize = 18.sp) }
                        )
                    }
                }
                OutlinedTextField(
                    value = icon,
                    onValueChange = { icon = it },
                    label = { Text("Или своя иконка (эмодзи)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = { if (name.isNotBlank()) onSave(name.trim(), icon, if (type == TransactionType.INCOME) 0xFF4CAF50 else 0xFFF44336) },
                enabled = name.isNotBlank()
            ) { Text("Добавить") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Отмена") }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditSourceDialog(
    source: Source,
    onDismiss: () -> Unit,
    onSave: (Source) -> Unit
) {
    var name by remember { mutableStateOf(source.name) }
    var icon by remember { mutableStateOf(source.icon) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Редактировать") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Название") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = icon,
                    onValueChange = { icon = it },
                    label = { Text("Иконка") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = { if (name.isNotBlank()) onSave(source.copy(name = name.trim(), icon = icon)) },
                enabled = name.isNotBlank()
            ) { Text("Сохранить") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Отмена") }
        }
    )
}
