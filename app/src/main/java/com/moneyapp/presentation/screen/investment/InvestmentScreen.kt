package com.moneyapp.presentation.screen.investment

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
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
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.moneyapp.domain.model.Investment
import com.moneyapp.presentation.screen.transaction.showDatePicker
import com.moneyapp.presentation.util.formatIndonesianNumber
import com.moneyapp.presentation.util.formatPercentage
import com.moneyapp.presentation.util.formatRupiah
import com.moneyapp.presentation.util.formatRupiahInput
import com.moneyapp.presentation.util.parseIndonesianNumber
import com.moneyapp.presentation.util.parseRupiahInput
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

// ============================================================
//  WARNA
// ============================================================
private val GreenDark = Color(0xFF2D6A4F)
private val GreenLight = Color(0xFF52B788)
private val RedAccent = Color(0xFFE63946)
private val DeleteRed = Color(0xFFD00000)
private val ListBorderTealLight = Color(0xFF008080)
private val ListBorderCoralLight = Color(0xFFFF6B6B)
private val ListBorderTealDark = Color(0xFF20B2AA)
private val ListBorderCoralDark = Color(0xFFFF8F8F)
private val SellGold = Color(0xFFF59E0B)

// ============================================================
//  SCREEN UTAMA
// ============================================================

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InvestmentScreen(
    navController: NavController,
    viewModel: InvestmentViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val activeInvestments by viewModel.activeInvestments.collectAsState()
    val isDarkTheme = isSystemInDarkTheme()
    val firstBorderColor = if (isDarkTheme) ListBorderTealDark else ListBorderTealLight
    val secondBorderColor = if (isDarkTheme) ListBorderCoralDark else ListBorderCoralLight
    val dateFormatter = remember { DateTimeFormatter.ofPattern("dd MMMM yyyy", Locale("id", "ID")) }

    // --- Dialog visibility states ---
    var showAddDialog by remember { mutableStateOf(false) }
    var showEditDialog by remember { mutableStateOf(false) }
    var showSellDialog by remember { mutableStateOf(false) }
    var selectedInvestmentForEdit by remember { mutableStateOf<Investment?>(null) }
    var selectedInvestmentForDelete by remember { mutableStateOf<Investment?>(null) }
    var selectedInvestmentForSell by remember { mutableStateOf<Investment?>(null) }

    // --- Add form states ---
    var addName by remember { mutableStateOf("") }
    var addAvgPrice by remember { mutableStateOf("") }
    var addCurrentPrice by remember { mutableStateOf("") }
    var addTotalAmount by remember { mutableStateOf("") }
    var addDate by remember { mutableStateOf(LocalDate.now()) }

    // --- Edit form states ---
    var editName by remember { mutableStateOf("") }
    var editAvgPrice by remember { mutableStateOf("") }
    var editCurrentPrice by remember { mutableStateOf("") }
    var editTotalAmount by remember { mutableStateOf("") }
    var editDate by remember { mutableStateOf(LocalDate.now()) }

    // Listen to ViewModel UI events
    LaunchedEffect(Unit) {
        viewModel.uiEvent.collect { event ->
            when (event) {
                is InvestmentUiEvent.Success -> {
                    showAddDialog = false
                    showEditDialog = false
                    showSellDialog = false
                    selectedInvestmentForDelete = null
                    selectedInvestmentForEdit = null
                    selectedInvestmentForSell = null
                    // Reset add form
                    addName = ""
                    addAvgPrice = ""
                    addCurrentPrice = ""
                    addTotalAmount = ""
                    addDate = LocalDate.now()
                }
                is InvestmentUiEvent.Error -> {
                    Toast.makeText(context, event.message, Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    // --- Summary calculations (hanya dari investasi aktif) ---
    val totalInvested = activeInvestments.sumOf { it.totalAmount ?: it.amount }
    val totalCurrentValue = activeInvestments.sumOf { inv ->
        val avg = inv.averagePrice
        val cur = inv.currentPrice
        val total = inv.totalAmount ?: inv.amount
        if (avg != null && avg > 0.0 && cur != null) {
            (total / avg) * cur
        } else {
            total
        }
    }
    val totalProfitLoss = totalCurrentValue - totalInvested
    val isProfitPositive = totalProfitLoss >= 0
    val profitLossColor = if (isProfitPositive) GreenDark else RedAccent
    val profitLossSign = if (isProfitPositive) "+" else ""

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("📈 Investasi Saham", fontWeight = FontWeight.Bold) },
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

            // ── RINGKASAN INVESTASI ──────────────────────────────
            Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = GreenDark),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(horizontal = 20.dp, vertical = 16.dp)) {
                    Text(
                        "RINGKASAN INVESTASI",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White.copy(alpha = 0.75f),
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(
                                "Total Investasi",
                                fontSize = 11.sp,
                                color = Color.White.copy(alpha = 0.7f)
                            )
                            Text(
                                formatRupiah(totalInvested),
                                fontSize = 18.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color.White
                            )
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                "Total Profit/Loss",
                                fontSize = 11.sp,
                                color = Color.White.copy(alpha = 0.7f)
                            )
                            Text(
                                "$profitLossSign${formatRupiah(totalProfitLoss)}",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = if (isProfitPositive) Color(0xFFB7E4C7) else Color(0xFFFFB3B3)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // ── TOMBOL TAMBAH ────────────────────────────────────
            Button(
                onClick = { showAddDialog = true },
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = GreenDark, contentColor = Color.White),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
            ) {
                Icon(imageVector = Icons.Filled.Add, contentDescription = "Tambah")
                Spacer(modifier = Modifier.width(6.dp))
                Text("Tambah Investasi", fontWeight = FontWeight.Bold)
            }

            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = "DAFTAR INVESTASI AKTIF",
                style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    letterSpacing = 1.sp
                )
            )

            Spacer(modifier = Modifier.height(8.dp))

            if (activeInvestments.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("📊", fontSize = 40.sp)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            "Belum ada investasi aktif",
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),
                            fontSize = 14.sp
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    itemsIndexed(activeInvestments, key = { _, inv -> inv.id }) { index, inv ->
                        InvestmentItem(
                            investment = inv,
                            borderColor = if (index % 2 == 0) firstBorderColor else secondBorderColor,
                            onEditClick = {
                                selectedInvestmentForEdit = inv
                                editName = inv.name
                                editAvgPrice = inv.averagePrice?.let { formatIndonesianNumber(it) } ?: ""
                                editCurrentPrice = inv.currentPrice?.let { formatIndonesianNumber(it) } ?: ""
                                editTotalAmount = (inv.totalAmount ?: inv.amount).let { formatRupiahInput(it.toLong().toString()) }
                                editDate = inv.date
                                showEditDialog = true
                            },
                            onDeleteClick = { selectedInvestmentForDelete = inv },
                            onSellClick = {
                                selectedInvestmentForSell = inv
                                showSellDialog = true
                            }
                        )
                    }
                    item { Spacer(modifier = Modifier.height(30.dp)) }
                }
            }
        }
    }

    // ── DIALOG: TAMBAH INVESTASI ─────────────────────────────────
    if (showAddDialog) {
        AlertDialog(
            onDismissRequest = { showAddDialog = false },
            title = {
                Text("Tambah Investasi Saham", fontWeight = FontWeight.Bold)
            },
            text = {
                Column(
                    modifier = Modifier.verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Nama Saham (wajib)
                    OutlinedTextField(
                        value = addName,
                        onValueChange = { addName = it.uppercase() },
                        label = { Text("Nama Saham *") },
                        placeholder = { Text("cth: BBCA, GOTO, TLKM") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(
                            capitalization = KeyboardCapitalization.Characters
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    )

                    // Average Saham (opsional)
                    OutlinedTextField(
                        value = addAvgPrice,
                        onValueChange = { if (it.isEmpty() || it.matches(Regex("^[0-9.,]*$"))) addAvgPrice = it },
                        label = { Text("Average Saham") },
                        placeholder = { Text("Opsional, cth: 10.500,5") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    )

                    // Harga Saat Ini (opsional)
                    OutlinedTextField(
                        value = addCurrentPrice,
                        onValueChange = { if (it.isEmpty() || it.matches(Regex("^[0-9.,]*$"))) addCurrentPrice = it },
                        label = { Text("Harga Saham Saat Ini") },
                        placeholder = { Text("Opsional, cth: 12.000") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    )

                    // Total Uang Investasi (wajib)
                    OutlinedTextField(
                        value = TextFieldValue(addTotalAmount, selection = TextRange(addTotalAmount.length)),
                        onValueChange = { addTotalAmount = formatRupiahInput(it.text) },
                        label = { Text("Total Uang Investasi *") },
                        placeholder = { Text("cth: 1.000.000") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    )

                    // Tanggal (opsional)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .border(
                                1.dp,
                                MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f),
                                RoundedCornerShape(12.dp)
                            )
                            .clickable { showDatePicker(context, addDate) { addDate = it } }
                            .padding(horizontal = 16.dp, vertical = 14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                "Tanggal",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                            )
                            Text(addDate.format(dateFormatter), fontWeight = FontWeight.Medium)
                        }
                        Icon(
                            imageVector = Icons.Filled.CalendarMonth,
                            contentDescription = "Pilih Tanggal",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }

                    Text(
                        "* Wajib diisi",
                        fontSize = 11.sp,
                        fontStyle = FontStyle.Italic,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val totalVal = parseRupiahInput(addTotalAmount) ?: 0.0
                        val avgVal = parseIndonesianNumber(addAvgPrice)
                        val curVal = parseIndonesianNumber(addCurrentPrice)
                        viewModel.saveInvestmentNew(
                            name = addName,
                            averagePrice = avgVal,
                            currentPrice = curVal,
                            totalAmount = totalVal,
                            date = addDate,
                            notes = null
                        )
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = GreenDark)
                ) {
                    Text("Simpan", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddDialog = false }) {
                    Text("Batal")
                }
            }
        )
    }

    // ── DIALOG: EDIT INVESTASI ────────────────────────────────────
    if (showEditDialog && selectedInvestmentForEdit != null) {
        val inv = selectedInvestmentForEdit!!
        AlertDialog(
            onDismissRequest = { showEditDialog = false },
            title = {
                Text("Edit Investasi: ${inv.name}", fontWeight = FontWeight.Bold)
            },
            text = {
                Column(
                    modifier = Modifier.verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Nama Saham
                    OutlinedTextField(
                        value = editName,
                        onValueChange = { editName = it.uppercase() },
                        label = { Text("Nama Saham") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(
                            capitalization = KeyboardCapitalization.Characters
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    )

                    // Average Saham
                    OutlinedTextField(
                        value = editAvgPrice,
                        onValueChange = { if (it.isEmpty() || it.matches(Regex("^[0-9.,]*$"))) editAvgPrice = it },
                        label = { Text("Average Saham") },
                        placeholder = { Text("Opsional, cth: 10.500,5") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    )

                    // Harga Saat Ini
                    OutlinedTextField(
                        value = editCurrentPrice,
                        onValueChange = { if (it.isEmpty() || it.matches(Regex("^[0-9.,]*$"))) editCurrentPrice = it },
                        label = { Text("Harga Saham Saat Ini") },
                        placeholder = { Text("Opsional, cth: 12.000") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    )

                    // Total Uang Investasi
                    OutlinedTextField(
                        value = TextFieldValue(editTotalAmount, selection = TextRange(editTotalAmount.length)),
                        onValueChange = { editTotalAmount = formatRupiahInput(it.text) },
                        label = { Text("Total Uang Investasi") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    )

                    // Tanggal
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .border(
                                1.dp,
                                MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f),
                                RoundedCornerShape(12.dp)
                            )
                            .clickable { showDatePicker(context, editDate) { editDate = it } }
                            .padding(horizontal = 16.dp, vertical = 14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                "Tanggal",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                            )
                            Text(editDate.format(dateFormatter), fontWeight = FontWeight.Medium)
                        }
                        Icon(
                            imageVector = Icons.Filled.CalendarMonth,
                            contentDescription = "Pilih Tanggal",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }

                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val totalVal = parseRupiahInput(editTotalAmount)
                        val avgVal = parseIndonesianNumber(editAvgPrice)
                        val curVal = parseIndonesianNumber(editCurrentPrice)
                        viewModel.updateInvestmentFull(
                            investment = inv,
                            name = editName.ifBlank { null },
                            averagePrice = avgVal,
                            currentPrice = curVal,
                            totalAmount = totalVal,
                            date = editDate,
                            notes = null
                        )
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = GreenDark)
                ) {
                    Text("Simpan", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showEditDialog = false }) {
                    Text("Batal")
                }
            }
        )
    }

    // ── DIALOG: KONFIRMASI JUAL ───────────────────────────────────
    if (showSellDialog && selectedInvestmentForSell != null) {
        val inv = selectedInvestmentForSell!!
        val totalInv = inv.totalAmount ?: inv.amount
        val avg = inv.averagePrice
        val cur = inv.currentPrice
        val lembar = if (avg != null && avg > 0.0) (totalInv / avg) else null
        val nilaiSekarang = if (lembar != null && cur != null) lembar * cur else totalInv
        val profitLoss = nilaiSekarang - totalInv
        val profitPct = if (totalInv > 0) (profitLoss / totalInv) * 100 else 0.0
        val isProfit = profitLoss >= 0

        AlertDialog(
            onDismissRequest = { showSellDialog = false },
            title = {
                Text("Konfirmasi Penjualan", fontWeight = FontWeight.Bold)
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    // Nama saham
                    Text(
                        inv.name,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.primary
                    )

                    HorizontalDivider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f))

                    // Detail
                    SellDialogRow("Total Investasi", formatRupiah(totalInv))
                    if (avg != null) SellDialogRow("Average Beli", "Rp ${formatIndonesianNumber(avg)}/lembar")
                    if (cur != null) SellDialogRow("Harga Saat Ini", "Rp ${formatIndonesianNumber(cur)}/lembar")
                    if (lembar != null) SellDialogRow("Jumlah Lembar", "${lembar.toLong()} lembar")
                    SellDialogRow("Nilai Jual", formatRupiah(nilaiSekarang))

                    HorizontalDivider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f))

                    // Profit/Loss
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                if (isProfit) GreenDark.copy(alpha = 0.12f) else RedAccent.copy(alpha = 0.12f),
                                RoundedCornerShape(10.dp)
                            )
                            .padding(horizontal = 12.dp, vertical = 10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = if (isProfit) Icons.Filled.TrendingUp else Icons.Filled.TrendingDown,
                                contentDescription = null,
                                tint = if (isProfit) GreenDark else RedAccent,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                if (isProfit) "Profit" else "Loss",
                                fontWeight = FontWeight.Bold,
                                color = if (isProfit) GreenDark else RedAccent
                            )
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                "${if (isProfit) "+" else ""}${formatRupiah(profitLoss)}",
                                fontWeight = FontWeight.ExtraBold,
                                color = if (isProfit) GreenDark else RedAccent,
                                fontSize = 16.sp
                            )
                            Text(
                                "(${formatPercentage(profitPct)})",
                                fontSize = 12.sp,
                                color = if (isProfit) GreenDark else RedAccent
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        "Apakah Anda yakin ingin menjual semua saham ${inv.name}?\nTransaksi pemasukan akan dibuat otomatis.",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.65f),
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = { viewModel.sellInvestment(inv) },
                    colors = ButtonDefaults.buttonColors(containerColor = SellGold, contentColor = Color.White)
                ) {
                    Text("Ya, Jual!", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showSellDialog = false }) {
                    Text("Batal")
                }
            }
        )
    }

    // ── DIALOG: HAPUS INVESTASI ───────────────────────────────────
    if (selectedInvestmentForDelete != null) {
        AlertDialog(
            onDismissRequest = { selectedInvestmentForDelete = null },
            title = { Text("Hapus Investasi?", fontWeight = FontWeight.Bold) },
            text = {
                Text("Yakin ingin menghapus saham ${selectedInvestmentForDelete!!.name}? Data ini tidak bisa dikembalikan.")
            },
            confirmButton = {
                Button(
                    onClick = { viewModel.deleteInvestment(selectedInvestmentForDelete!!.id) },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = DeleteRed,
                        contentColor = Color.White
                    )
                ) { Text("Hapus") }
            },
            dismissButton = {
                TextButton(onClick = { selectedInvestmentForDelete = null }) {
                    Text("Batal")
                }
            }
        )
    }
}

// ============================================================
//  ITEM INVESTASI
// ============================================================

@Composable
private fun InvestmentItem(
    investment: Investment,
    borderColor: Color,
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit,
    onSellClick: () -> Unit
) {
    val totalInv = investment.totalAmount ?: investment.amount
    val avg = investment.averagePrice
    val cur = investment.currentPrice
    val lembar = if (avg != null && avg > 0.0) (totalInv / avg) else null
    val nilaiSekarang = if (lembar != null && cur != null) lembar * cur else totalInv
    val profitLoss = nilaiSekarang - totalInv
    val profitPct = if (totalInv > 0) (profitLoss / totalInv) * 100 else 0.0
    val hasPriceData = cur != null
    val isProfit = profitLoss >= 0
    val profitColor = if (isProfit) GreenDark else RedAccent

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier
            .fillMaxWidth()
            .border(1.5.dp, borderColor, RoundedCornerShape(16.dp))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {

            // ── Header: nama + icon ───────────────────────────
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = investment.name,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.ExtraBold),
                    color = MaterialTheme.colorScheme.onSurface
                )
                if (hasPriceData) {
                    Row(
                        modifier = Modifier
                            .background(profitColor.copy(alpha = 0.12f), RoundedCornerShape(8.dp))
                            .padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = if (isProfit) Icons.Filled.TrendingUp else Icons.Filled.TrendingDown,
                            contentDescription = null,
                            tint = profitColor,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            "${if (isProfit) "+" else ""}${formatPercentage(profitPct)}",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = profitColor
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // ── Data harga ────────────────────────────────────
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Kolom kiri: data beli
                Column(modifier = Modifier.weight(1f)) {
                    if (avg != null) {
                        LabelValueText("Average Saham", "Rp ${formatIndonesianNumber(avg)}")
                    }
                    if (cur != null) {
                        LabelValueText("Harga Saat Ini", "Rp ${formatIndonesianNumber(cur)}")
                    }
                    LabelValueText("Total Investasi", formatRupiah(totalInv))
                }
                // Kolom kanan: nilai pasar + P&L
                Column(
                    modifier = Modifier.weight(1f),
                    horizontalAlignment = Alignment.End
                ) {
                    if (hasPriceData) {
                        LabelValueText("Market Value", formatRupiah(nilaiSekarang), Alignment.End)
                    }
                    val sign = if (isProfit) "+" else ""
                    LabelValueText(
                        if (isProfit) "Potential Profit" else "Potential Loss",
                        "$sign${formatRupiah(profitLoss)}",
                        Alignment.End,
                        valueColor = profitColor
                    )
                    if (hasPriceData) {
                        LabelValueText(
                            "Persentase",
                            "$sign${formatPercentage(profitPct)}",
                            Alignment.End,
                            valueColor = profitColor
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // ── Tombol aksi ───────────────────────────────────
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Edit icon button
                IconButton(
                    onClick = onEditClick,
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.Edit,
                        contentDescription = "Edit",
                        tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.8f),
                        modifier = Modifier.size(18.dp)
                    )
                }
                Spacer(modifier = Modifier.width(4.dp))
                // Delete icon button
                IconButton(
                    onClick = onDeleteClick,
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.Delete,
                        contentDescription = "Hapus",
                        tint = DeleteRed,
                        modifier = Modifier.size(18.dp)
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                // Jual text button
                Button(
                    onClick = onSellClick,
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = SellGold,
                        contentColor = Color.White
                    ),
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(
                        horizontal = 16.dp,
                        vertical = 6.dp
                    ),
                    modifier = Modifier.height(32.dp)
                ) {
                    Text("Jual", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                }
            }
        }
    }
}

// ============================================================
//  HELPER COMPOSABLES
// ============================================================

@Composable
private fun LabelValueText(
    label: String,
    value: String,
    alignment: Alignment.Horizontal = Alignment.Start,
    valueColor: Color = Color.Unspecified
) {
    Column(
        horizontalAlignment = when (alignment) {
            Alignment.End -> Alignment.End
            else -> Alignment.Start
        }
    ) {
        Text(
            label,
            fontSize = 10.sp,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
        )
        Text(
            value,
            fontSize = 13.sp,
            fontWeight = FontWeight.SemiBold,
            color = if (valueColor == Color.Unspecified) MaterialTheme.colorScheme.onSurface else valueColor
        )
    }
}

@Composable
private fun SellDialogRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
        Text(value, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
    }
}

// ============================================================
//  FORMAT HELPER
// ============================================================
// formatAngka dipindah ke NumberFormatter.formatIndonesianNumber()
// Semua pemanggil sudah diupdate ke formatIndonesianNumber()
