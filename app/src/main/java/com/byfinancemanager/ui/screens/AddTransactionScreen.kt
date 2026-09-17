package com.byfinancemanager.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.byfinancemanager.data.local.Source
import com.byfinancemanager.data.local.TransactionType
import com.byfinancemanager.ui.viewmodel.FinanceViewModel
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun AddTransactionScreen(
    viewModel: FinanceViewModel,
    onBack: () -> Unit,
    onSaved: () -> Unit
) {
    val incomeSources by viewModel.incomeSources.collectAsState()
    val expenseSources by viewModel.expenseSources.collectAsState()
    val currentRate by viewModel.currentRate.collectAsState()
    val isLoadingRate by viewModel.isLoadingRate.collectAsState()

    var selectedType by remember { mutableStateOf(TransactionType.EXPENSE) }
    var amountBynText by remember { mutableStateOf("") }
    var selectedDate by remember { mutableStateOf(System.currentTimeMillis()) }
    var selectedSource by remember { mutableStateOf<Source?>(null) }
    var note by remember { mutableStateOf("") }
    var fetchedRate by remember { mutableStateOf(currentRate) }
    var amountUsd by remember { mutableStateOf(0.0) }
    var showDatePicker by remember { mutableStateOf(false) }
    var rateManuallyEdited by remember { mutableStateOf(false) }
    var rateText by remember { mutableStateOf(String.format("%.4f", currentRate)) }

    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    val dateFormat = remember { SimpleDateFormat("dd MMMM yyyy", Locale("ru")) }
    val dateFormatApi = remember { SimpleDateFormat("yyyy-MM-dd", Locale.US) }

    // При изменении типа - сбросить источник
    LaunchedEffect(selectedType) {
        selectedSource = null
    }

    // При изменении даты - подгрузить курс НБРБ
    LaunchedEffect(selectedDate) {
        rateManuallyEdited = false
        try {
            val rate = viewModel.fetchRateForDate(selectedDate)
            fetchedRate = rate
            rateText = String.format("%.4f", rate)
        } catch (e: Exception) {
            fetchedRate = currentRate
            rateText = String.format("%.4f", currentRate)
        }
    }

    // Пересчет USD при изменении BYN или курса
    LaunchedEffect(amountBynText, fetchedRate, rateText) {
        val byn = amountBynText.toDoubleOrNull() ?: 0.0
        val rate = if (rateManuallyEdited) rateText.toDoubleOrNull() ?: fetchedRate else fetchedRate
        amountUsd = if (rate != 0.0) byn / rate else 0.0
    }

    // Если курс не редактировался вручную - синхронизировать с fetchedRate
    LaunchedEffect(fetchedRate) {
        if (!rateManuallyEdited) {
            rateText = String.format("%.4f", fetchedRate)
        }
    }

    val sources = if (selectedType == TransactionType.INCOME) incomeSources else expenseSources
    val amountByn = amountBynText.toDoubleOrNull() ?: 0.0
    val finalRate = if (rateManuallyEdited) rateText.toDoubleOrNull() ?: fetchedRate else fetchedRate
    val isValid = amountByn > 0 && finalRate > 0

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (selectedType == TransactionType.INCOME) "Новый доход" else "Новый расход") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Назад")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Тип операции
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Card(
                    modifier = Modifier.weight(1f).selectable(
                        selected = selectedType == TransactionType.INCOME,
                        onClick = { selectedType = TransactionType.INCOME }
                    ),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (selectedType == TransactionType.INCOME) Color(0xFF4CAF50) else Color(0xFFE0E0E0)
                    )
                ) {
                    Box(modifier = Modifier.padding(16.dp).fillMaxWidth(), contentAlignment = Alignment.Center) {
                        Text("💰 Доход", color = if (selectedType == TransactionType.INCOME) Color.White else Color.Black, fontWeight = FontWeight.Bold)
                    }
                }
                Card(
                    modifier = Modifier.weight(1f).selectable(
                        selected = selectedType == TransactionType.EXPENSE,
                        onClick = { selectedType = TransactionType.EXPENSE }
                    ),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (selectedType == TransactionType.EXPENSE) Color(0xFFF44336) else Color(0xFFE0E0E0)
                    )
                ) {
                    Box(modifier = Modifier.padding(16.dp).fillMaxWidth(), contentAlignment = Alignment.Center) {
                        Text("💸 Расход", color = if (selectedType == TransactionType.EXPENSE) Color.White else Color.Black, fontWeight = FontWeight.Bold)
                    }
                }
            }

            // Сумма BYN
            OutlinedTextField(
                value = amountBynText,
                onValueChange = { newValue ->
                    // Разрешаем только цифры и точку/запятую
                    val filtered = newValue.replace(',', '.').filter { it.isDigit() || it == '.' }
                    // Не более одной точки
                    if (filtered.count { it == '.' } <= 1) {
                        amountBynText = filtered
                    }
                },
                label = { Text("Сумма в BYN (бел. рублях)") },
                placeholder = { Text("0.00") },
                prefix = { Text("BYN ") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            // Дата
            OutlinedCard(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                onClick = { showDatePicker = true }
            ) {
                Row(
                    modifier = Modifier.padding(16.dp).fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("Дата операции", fontSize = 12.sp, color = Color.Gray)
                        Text(dateFormat.format(Date(selectedDate)), fontWeight = FontWeight.Medium)
                        Text("Курс НБРБ на этот день", fontSize = 10.sp, color = Color.Gray)
                    }
                    Icon(Icons.Default.CalendarToday, contentDescription = null)
                }
            }

            // Курс НБРБ и USD
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFF5F5F5))
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Курс НБРБ USD/BYN", fontWeight = FontWeight.Medium, fontSize = 14.sp)
                        if (isLoadingRate) {
                            CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                        }
                    }
                    OutlinedTextField(
                        value = rateText,
                        onValueChange = {
                            rateText = it
                            rateManuallyEdited = true
                        },
                        label = { Text("Курс") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        supportingText = { Text("Можно отредактировать вручную, если нет интернета") }
                    )
                    Divider()
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Сумма в USD:", fontSize = 14.sp)
                        Text(
                            "${String.format("%.2f", amountUsd)} USD",
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = Color(0xFF1976D2)
                        )
                    }
                    Text(
                        "Формула: ${String.format("%.2f", amountByn)} BYN / ${String.format("%.4f", finalRate)} = ${String.format("%.2f", amountUsd)} USD",
                        fontSize = 11.sp,
                        color = Color.Gray
                    )
                    Text(
                        "Источник: api.nbrb.by • Дата: ${dateFormatApi.format(Date(selectedDate))}",
                        fontSize = 9.sp,
                        color = Color.Gray.copy(alpha = 0.7f)
                    )
                }
            }

            // Источник
            Text("Источник / Категория", fontWeight = FontWeight.Bold, fontSize = 16.sp)
            if (sources.isEmpty()) {
                Text("Нет источников. Добавьте в разделе Источники.", color = Color.Gray, fontSize = 12.sp)
            } else {
                // Simple flow of chips
                androidx.compose.foundation.layout.FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    sources.forEach { src ->
                        FilterChip(
                            selected = selectedSource?.id == src.id,
                            onClick = { selectedSource = src },
                            label = { Text("${src.icon} ${src.name}") }
                        )
                    }
                }
            }

            // Заметка
            OutlinedTextField(
                value = note,
                onValueChange = { note = it },
                label = { Text("Заметка (необязательно)") },
                placeholder = { Text("Например: продукты в Евроопте") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 2
            )

            // Кнопка сохранить
            Button(
                onClick = {
                    if (isValid) {
                        viewModel.addTransaction(
                            type = selectedType,
                            amountByn = amountByn,
                            amountUsd = amountUsd,
                            rate = finalRate,
                            date = selectedDate,
                            sourceId = selectedSource?.id,
                            note = note
                        ) {
                            onSaved()
                        }
                    }
                },
                enabled = isValid,
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Сохранить • ${String.format("%.2f", amountByn)} BYN = ${String.format("%.2f", amountUsd)} USD", fontWeight = FontWeight.Bold)
            }

            Spacer(modifier = Modifier.height(80.dp))
        }
    }

    if (showDatePicker) {
        val datePickerState = rememberDatePickerState(initialSelectedDateMillis = selectedDate)
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { selectedDate = it }
                    showDatePicker = false
                }) { Text("OK") }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) { Text("Отмена") }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }
}
