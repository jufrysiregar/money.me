package com.moneyapp.presentation.screen.saving

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
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.moneyapp.domain.model.Saving
import com.moneyapp.presentation.screen.dashboard.formatRupiah
import com.moneyapp.presentation.screen.transaction.showDatePicker
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SavingScreen(
    navController: NavController,
    viewModel: SavingViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val savings by viewModel.savings.collectAsState()

    var showAddDialog by remember { mutableStateOf(false) }
    var showTopUpDialog by remember { mutableStateOf(false) }
    var selectedSavingForTopUp by remember { mutableStateOf<Saving?>(null) }

    // Dialog state for Adding Saving Goal
    var name by remember { mutableStateOf("") }
    var targetAmount by remember { mutableStateOf("") }
    var currentAmount by remember { mutableStateOf("") }
    var date by remember { mutableStateOf(LocalDate.now().plusMonths(6)) }
    val dateFormatter = remember { DateTimeFormatter.ofPattern("dd MMMM yyyy", Locale("id", "ID")) }

    // Dialog state for Top-Up
    var topUpInput by remember { mutableStateOf("") }

    // Listen to events
    LaunchedEffect(Unit) {
        viewModel.uiEvent.collect { event ->
            when (event) {
                is SavingUiEvent.Success -> {
                    showAddDialog = false
                    showTopUpDialog = false
                    // Reset fields
                    name = ""
                    targetAmount = ""
                    currentAmount = ""
                    topUpInput = ""
                    date = LocalDate.now().plusMonths(6)
                }
                is SavingUiEvent.Error -> {
                    Toast.makeText(context, event.message, Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    // Pick first item as primary target goal (Mockup 4)
    val primarySaving = savings.firstOrNull()
    val otherSavings = if (savings.size > 1) savings.subList(1, savings.size) else emptyList()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("🏦 Tabungan", fontWeight = FontWeight.Bold) },
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

            // ── 1. TARGET UTAMA SECTION ─────────────────────────────────────
            if (primarySaving != null) {
                val progress = if (primarySaving.targetAmount > 0) (primarySaving.currentAmount / primarySaving.targetAmount).toFloat() else 0f
                val progressPercent = (progress * 100).toInt().coerceIn(0, 100)

                Text(
                    text = "🎯 TARGET UTAMA",
                    style = MaterialTheme.typography.titleSmall.copy(
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        letterSpacing = 0.5.sp
                    )
                )
                Spacer(modifier = Modifier.height(8.dp))

                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            selectedSavingForTopUp = primarySaving
                            topUpInput = ""
                            showTopUpDialog = true
                        }
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Text(
                            text = primarySaving.targetName,
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.ExtraBold)
                        )
                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Target: ${formatRupiah(primarySaving.targetAmount)}",
                                fontSize = 13.sp,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                            )
                            Text(
                                text = "Terkumpul: ${formatRupiah(primarySaving.currentAmount)}",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Custom Progress Bar matching ASCII mockups (██████░░░░ 60%)
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            LinearProgressIndicator(
                                progress = { progress.coerceIn(0f, 1f) },
                                color = Color(0xFF1E6091),
                                trackColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f),
                                modifier = Modifier
                                    .weight(1f)
                                    .height(10.dp)
                                    .clip(RoundedCornerShape(6.dp))
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = "$progressPercent%",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        val deadlineStr = try {
                            primarySaving.targetDate.format(DateTimeFormatter.ofPattern("MMM yyyy", Locale("id", "ID")))
                        } catch (e: Exception) {
                            primarySaving.targetDate.toString()
                        }

                        Text(
                            text = "Target: $deadlineStr",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                        )
                    }
                }
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp)
                        .clip(RoundedCornerShape(20.dp))
                        .background(MaterialTheme.colorScheme.surface)
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text("Belum ada target tabungan utama", color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f))
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Add saving goal trigger button
            Button(
                onClick = { showAddDialog = true },
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E6091), contentColor = Color.White),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
            ) {
                Icon(imageVector = Icons.Filled.Add, contentDescription = "Tambah")
                Spacer(modifier = Modifier.width(6.dp))
                Text("Tambah Target Tabungan", fontWeight = FontWeight.Bold)
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "📋 SEMUA TABUNGAN",
                style = MaterialTheme.typography.titleSmall.copy(
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    letterSpacing = 0.5.sp
                )
            )

            Spacer(modifier = Modifier.height(8.dp))

            if (otherSavings.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (primarySaving == null) "Silakan tambah target tabungan" else "Tidak ada tabungan lain",
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(otherSavings, key = { it.id }) { sav ->
                        SavingItem(
                            saving = sav,
                            onTopUpClick = {
                                selectedSavingForTopUp = sav
                                topUpInput = ""
                                showTopUpDialog = true
                            },
                            onDeleteClick = { viewModel.deleteSaving(sav.id) }
                        )
                    }
                    item {
                        Spacer(modifier = Modifier.height(30.dp))
                    }
                }
            }
        }
    }

    // ── 1. ADD SAVINGS GOAL DIALOG ───────────────────────────────────────────
    if (showAddDialog) {
        AlertDialog(
            onDismissRequest = { showAddDialog = false },
            title = { Text("Tambah Target Tabungan", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text("Nama Target") },
                        placeholder = { Text("cth: Dana Darurat") },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = targetAmount,
                        onValueChange = { targetAmount = it },
                        label = { Text("Nominal Target (Rp)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = currentAmount,
                        onValueChange = { currentAmount = it },
                        label = { Text("Terkumpul Awal (Rp)") },
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
                        Icon(imageVector = Icons.Filled.CalendarMonth, contentDescription = "Target Tanggal", tint = MaterialTheme.colorScheme.primary)
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val targetVal = targetAmount.toDoubleOrNull() ?: 0.0
                        val currentVal = currentAmount.toDoubleOrNull() ?: 0.0
                        viewModel.saveSaving(name, targetVal, currentVal, date)
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E6091))
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

    // ── 2. TOP UP SAVINGS GOAL DIALOG ────────────────────────────────────────
    if (showTopUpDialog && selectedSavingForTopUp != null) {
        AlertDialog(
            onDismissRequest = { showTopUpDialog = false },
            title = { Text("💰 TOP UP TABUNGAN", fontWeight = FontWeight.Black) },
            text = {
                Column {
                    Text("Target: ${selectedSavingForTopUp!!.targetName}", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    Text("Terkumpul saat ini: ${formatRupiah(selectedSavingForTopUp!!.currentAmount)}", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                    Spacer(modifier = Modifier.height(14.dp))
                    OutlinedTextField(
                        value = topUpInput,
                        onValueChange = { topUpInput = it },
                        label = { Text("Jumlah Top Up (Rp)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val topUpVal = topUpInput.toDoubleOrNull() ?: 0.0
                        viewModel.topUpSaving(
                            selectedSavingForTopUp!!.id,
                            selectedSavingForTopUp!!.currentAmount,
                            topUpVal
                        )
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E6091))
                ) {
                    Text("Tambah")
                }
            },
            dismissButton = {
                Button(
                    onClick = { showTopUpDialog = false },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent, contentColor = MaterialTheme.colorScheme.onSurface)
                ) {
                    Text("Batal")
                }
            }
        )
    }
}

/**
 * Item element inside Savings Goal List.
 */
@Composable
private fun SavingItem(
    saving: Saving,
    onTopUpClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    val progress = if (saving.targetAmount > 0) (saving.currentAmount / saving.targetAmount).toFloat() else 0f
    val progressPercent = (progress * 100).toInt().coerceIn(0, 100)

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = saving.targetName,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "${formatRupiah(saving.currentAmount)} / ${formatRupiah(saving.targetAmount)} ($progressPercent%)",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
                Spacer(modifier = Modifier.height(8.dp))
                
                // Progress bar
                LinearProgressIndicator(
                    progress = { progress.coerceIn(0f, 1f) },
                    color = Color(0xFF1E6091),
                    trackColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f),
                    modifier = Modifier
                        .fillMaxWidth(0.9f)
                        .height(6.dp)
                        .clip(RoundedCornerShape(3.dp))
                )
            }

            // Actions (Top-Up / Delete) (Mockup 4)
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onTopUpClick) {
                    Icon(imageVector = Icons.Filled.Payments, contentDescription = "Top Up", tint = Color(0xFF1E6091))
                }
                IconButton(onClick = onDeleteClick) {
                    Icon(imageVector = Icons.Filled.Delete, contentDescription = "Hapus", tint = MaterialTheme.colorScheme.error.copy(alpha = 0.6f))
                }
            }
        }
    }
}
