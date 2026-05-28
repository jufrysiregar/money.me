package com.moneyapp.presentation.screen.investment

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.TrendingDown
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.moneyapp.domain.model.Investment
import com.moneyapp.presentation.screen.dashboard.formatRupiah
import com.moneyapp.presentation.screen.transaction.showDatePicker
import com.moneyapp.presentation.util.formatRupiahInput
import com.moneyapp.presentation.util.parseRupiahInput
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InvestmentScreen(
    navController: NavController,
    viewModel: InvestmentViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val investments by viewModel.investments.collectAsState()

    var showAddDialog by remember { mutableStateOf(false) }
    var showEditDialog by remember { mutableStateOf(false) }
    var selectedInvestmentForEdit by remember { mutableStateOf<Investment?>(null) }
    var selectedInvestmentForDelete by remember { mutableStateOf<Investment?>(null) }

    // Dialog form states
    var name by remember { mutableStateOf("") }
    var amount by remember { mutableStateOf("") }
    var currentValue by remember { mutableStateOf("") }
    var date by remember { mutableStateOf(LocalDate.now()) }
    val dateFormatter = remember { DateTimeFormatter.ofPattern("dd MMMM yyyy", Locale("id", "ID")) }

    // Edit valuation dialog state
    var editAdditionalAmountInput by remember { mutableStateOf("") }
    var editCurrentValueInput by remember { mutableStateOf("") }

    // Listen to ViewModel actions events
    LaunchedEffect(Unit) {
        viewModel.uiEvent.collect { event ->
            when (event) {
                is InvestmentUiEvent.Success -> {
                    showAddDialog = false
                    showEditDialog = false
                    selectedInvestmentForDelete = null
                    // Reset forms
                    name = ""
                    amount = ""
                    currentValue = ""
                    date = LocalDate.now()
                }
                is InvestmentUiEvent.Error -> {
                    Toast.makeText(context, event.message, Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    // Calculations (Mockup 5)
    val totalInvested = investments.sumOf { it.amount }
    val totalCurrentValue = investments.sumOf { it.currentValue }
    val profitLoss = totalCurrentValue - totalInvested
    val profitLossSign = if (profitLoss >= 0) "+" else ""
    val profitLossColor = if (profitLoss >= 0) Color(0xFF2D6A4F) else Color(0xFFE63946)

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("📈 Investasi", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(imageVector = Icons.Filled.ArrowBack, contentDescription = "Kembali")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(innerPadding)
                .padding(horizontal = 16.dp)
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            // Summary Header Card (Mockup 5)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Card(
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF2D6A4F)),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("TOTAL INVESTASI", fontSize = 10.sp, color = Color.White.copy(alpha = 0.8f), fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(formatRupiah(totalCurrentValue), fontSize = 16.sp, color = Color.White, fontWeight = FontWeight.ExtraBold)
                    }
                }

                Card(
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("PROFIT / LOSS", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f), fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("$profitLossSign${formatRupiah(profitLoss)}", fontSize = 16.sp, color = profitLossColor, fontWeight = FontWeight.ExtraBold)
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Add investment trigger button
            Button(
                onClick = { showAddDialog = true },
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2D6A4F), contentColor = Color.White),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
            ) {
                Icon(imageVector = Icons.Filled.Add, contentDescription = "Tambah")
                Spacer(modifier = Modifier.width(6.dp))
                Text("Tambah Investasi", fontWeight = FontWeight.Bold)
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "📋 DAFTAR INVESTASI",
                style = MaterialTheme.typography.titleSmall.copy(
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    letterSpacing = 0.5.sp
                )
            )

            Spacer(modifier = Modifier.height(8.dp))

            if (investments.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Text("Belum ada aset investasi", color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f))
                }
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(investments, key = { it.id }) { inv ->
                        InvestmentItem(
                            investment = inv,
                            onUpdateValuationClick = {
                                selectedInvestmentForEdit = inv
                                editAdditionalAmountInput = ""
                                editCurrentValueInput = formatRupiahInput(inv.currentValue.toLong().toString())
                                showEditDialog = true
                            },
                            onDeleteClick = { selectedInvestmentForDelete = inv }
                        )
                    }
                    item {
                        Spacer(modifier = Modifier.height(30.dp))
                    }
                }
            }
        }
    }

    // ── 1. ADD INVESTMENT DIALOG ─────────────────────────────────────────────
    if (showAddDialog) {
        AlertDialog(
            onDismissRequest = { showAddDialog = false },
            title = { Text("Tambah Investasi Baru", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text("Nama Investasi") },
                        placeholder = { Text("cth: Reksadana A") },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = amount,
                        onValueChange = { amount = formatRupiahInput(it) },
                        label = { Text("Modal Awal (Rp)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = currentValue,
                        onValueChange = { currentValue = formatRupiahInput(it) },
                        label = { Text("Nilai Sekarang (Rp)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    )

                    // Date row
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .border(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f), RoundedCornerShape(12.dp))
                            .clickable { showDatePicker(context, date) { date = it } }
                            .padding(14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(date.format(dateFormatter), fontWeight = FontWeight.Bold)
                        Icon(imageVector = Icons.Filled.CalendarMonth, contentDescription = "Tanggal", tint = MaterialTheme.colorScheme.primary)
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val amountVal = parseRupiahInput(amount) ?: 0.0
                        val currentVal = parseRupiahInput(currentValue) ?: amountVal
                        viewModel.saveInvestment(name, amountVal, currentVal, date)
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2D6A4F))
                ) {
                    Text("Simpan")
                }
            },
            dismissButton = {
                Button(
                    onClick = { showAddDialog = false },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent, contentColor = MaterialTheme.colorScheme.onSurface)
                ) {
                    Text("Batal")
                }
            }
        )
    }

    // ── 2. EDIT CURRENT VALUATION DIALOG ──────────────────────────────────────
    if (showEditDialog && selectedInvestmentForEdit != null) {
        AlertDialog(
            onDismissRequest = { showEditDialog = false },
            title = { Text("Update Investasi", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("Aset: ${selectedInvestmentForEdit!!.name}", fontSize = 14.sp)
                    OutlinedTextField(
                        value = editAdditionalAmountInput,
                        onValueChange = { editAdditionalAmountInput = formatRupiahInput(it) },
                        label = { Text("Tambah Investasi (Rp)") },
                        placeholder = { Text("Kosongkan jika tidak tambah modal") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = editCurrentValueInput,
                        onValueChange = { editCurrentValueInput = formatRupiahInput(it) },
                        label = { Text("Harga Sekarang (Rp)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val additionalAmount = parseRupiahInput(editAdditionalAmountInput) ?: 0.0
                        val currentValue = parseRupiahInput(editCurrentValueInput)
                            ?: selectedInvestmentForEdit!!.currentValue
                        viewModel.updateInvestment(
                            selectedInvestmentForEdit!!.id,
                            additionalAmount,
                            currentValue
                        )
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2D6A4F))
                ) {
                    Text("Update")
                }
            },
            dismissButton = {
                Button(
                    onClick = { showEditDialog = false },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent, contentColor = MaterialTheme.colorScheme.onSurface)
                ) {
                    Text("Batal")
                }
            }
        )
    }

    if (selectedInvestmentForDelete != null) {
        AlertDialog(
            onDismissRequest = { selectedInvestmentForDelete = null },
            title = { Text("Hapus Investasi?", fontWeight = FontWeight.Bold) },
            text = {
                Text("Yakin ingin menghapus ${selectedInvestmentForDelete!!.name}? Data ini tidak bisa dikembalikan.")
            },
            confirmButton = {
                Button(
                    onClick = { viewModel.deleteInvestment(selectedInvestmentForDelete!!.id) },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Hapus")
                }
            },
            dismissButton = {
                Button(
                    onClick = { selectedInvestmentForDelete = null },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent, contentColor = MaterialTheme.colorScheme.onSurface)
                ) {
                    Text("Batal")
                }
            }
        )
    }
}

/**
 * Item element inside Investments List.
 */
@Composable
private fun InvestmentItem(
    investment: Investment,
    onUpdateValuationClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    val growthAmount = investment.currentValue - investment.amount
    val growthPercent = if (investment.amount > 0) (growthAmount / investment.amount) * 100 else 0.0
    val isPositive = growthAmount >= 0

    val growthColor = if (isPositive) Color(0xFF2D6A4F) else Color(0xFFE63946)
    val growthIcon = if (isPositive) Icons.Filled.TrendingUp else Icons.Filled.TrendingDown
    val growthSign = if (isPositive) "+" else ""

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = investment.name,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Investasi: ${formatRupiah(investment.amount)}",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                )
                Spacer(modifier = Modifier.height(2.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "Saat ini: ${formatRupiah(investment.currentValue)}",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    
                    // Growth pill display
                    Row(
                        modifier = Modifier
                            .background(growthColor.copy(alpha = 0.1f), RoundedCornerShape(6.dp))
                            .padding(horizontal = 6.dp, vertical = 2.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(imageVector = growthIcon, contentDescription = null, tint = growthColor, modifier = Modifier.size(10.dp))
                        Spacer(modifier = Modifier.width(2.dp))
                        Text("$growthSign%,.1f%%".format(Locale.US, growthPercent), fontSize = 10.sp, fontWeight = FontWeight.Bold, color = growthColor)
                    }
                }
            }

            // Edit / Delete Actions (Mockup 5)
            Row {
                IconButton(onClick = onUpdateValuationClick) {
                    Icon(imageVector = Icons.Filled.Edit, contentDescription = "Edit Nilai", tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.7f))
                }
                IconButton(onClick = onDeleteClick) {
                    Icon(imageVector = Icons.Filled.Delete, contentDescription = "Hapus", tint = MaterialTheme.colorScheme.error.copy(alpha = 0.6f))
                }
            }
        }
    }
}
