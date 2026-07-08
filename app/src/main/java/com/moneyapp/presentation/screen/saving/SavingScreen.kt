package com.moneyapp.presentation.screen.saving

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.isSystemInDarkTheme
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
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Payments
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
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.moneyapp.domain.model.Saving
import com.moneyapp.presentation.util.formatRupiah
import com.moneyapp.presentation.screen.transaction.showDatePicker
import com.moneyapp.presentation.util.formatRupiahInput
import com.moneyapp.presentation.util.parseRupiahInput
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

private val DeleteRed = Color(0xFFD00000)
private val ListBorderTealLight = Color(0xFF008080)
private val ListBorderCoralLight = Color(0xFFFF6B6B)
private val ListBorderTealDark = Color(0xFF20B2AA)
private val ListBorderCoralDark = Color(0xFFFF8F8F)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SavingScreen(
    navController: NavController,
    viewModel: SavingViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val savings by viewModel.savings.collectAsState()
    val isDarkTheme = isSystemInDarkTheme()
    val firstBorderColor = if (isDarkTheme) ListBorderTealDark else ListBorderTealLight
    val secondBorderColor = if (isDarkTheme) ListBorderCoralDark else ListBorderCoralLight

    var showAddDialog by remember { mutableStateOf(false) }
    var showTopUpDialog by remember { mutableStateOf(false) }
    var selectedSavingForTopUp by remember { mutableStateOf<Saving?>(null) }
    var selectedSavingForDelete by remember { mutableStateOf<Saving?>(null) }

    // Dialog state for Adding Saving Goal
    var name by remember { mutableStateOf("") }
    var targetAmount by remember { mutableStateOf("") }
    var currentAmount by remember { mutableStateOf("") }
    var date by remember { mutableStateOf(LocalDate.now().plusMonths(6)) }
    val dateFormatter = remember { DateTimeFormatter.ofPattern("dd MMMM yyyy", Locale("id", "ID")) }

    // Dialog state for Top-Up
    var topUpInput by remember { mutableStateOf("") }
    var targetUpdateInput by remember { mutableStateOf("") }

    // Listen to events
    LaunchedEffect(Unit) {
        viewModel.uiEvent.collect { event ->
            when (event) {
                is SavingUiEvent.Success -> {
                    showAddDialog = false
                    showTopUpDialog = false
                    selectedSavingForDelete = null
                    // Reset fields
                    name = ""
                    targetAmount = ""
                    currentAmount = ""
                    topUpInput = ""
                    targetUpdateInput = ""
                    date = LocalDate.now().plusMonths(6)
                }
                is SavingUiEvent.Error -> {
                    Toast.makeText(context, event.message, Toast.LENGTH_LONG).show()
                }
            }
        }
    }

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
                text = "SEMUA TABUNGAN",
                style = MaterialTheme.typography.titleSmall.copy(
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    letterSpacing = 0.5.sp
                )
            )

            Spacer(modifier = Modifier.height(8.dp))

            if (savings.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Silakan tambah target tabungan",
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    itemsIndexed(savings, key = { _, sav -> sav.id }) { index, sav ->
                        SavingItem(
                            saving = sav,
                            borderColor = if (index % 2 == 0) firstBorderColor else secondBorderColor,
                            onTopUpClick = {
                                selectedSavingForTopUp = sav
                                topUpInput = ""
                                targetUpdateInput = formatRupiahInput(sav.targetAmount.toLong().toString())
                                showTopUpDialog = true
                            },
                            onDeleteClick = { selectedSavingForDelete = sav }
                        )
                    }
                    item {
                        Spacer(modifier = Modifier.height(30.dp))
                    }
                }
            }
        }
    }

    // Add savings goal dialog
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
                        value = TextFieldValue(targetAmount, selection = TextRange(targetAmount.length)),
                        onValueChange = { targetAmount = formatRupiahInput(it.text) },
                        label = { Text("Nominal Target (Rp)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = TextFieldValue(currentAmount, selection = TextRange(currentAmount.length)),
                        onValueChange = { currentAmount = formatRupiahInput(it.text) },
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
                        val targetVal = parseRupiahInput(targetAmount) ?: 0.0
                        val currentVal = parseRupiahInput(currentAmount) ?: 0.0
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

    // Top up savings goal dialog
    if (showTopUpDialog && selectedSavingForTopUp != null) {
        AlertDialog(
            onDismissRequest = { showTopUpDialog = false },
            title = { Text("Top Up Tabungan", fontWeight = FontWeight.Black) },
            text = {
                Column {
                    Text("Target: ${selectedSavingForTopUp!!.targetName}", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    Text("Terkumpul saat ini: ${formatRupiah(selectedSavingForTopUp!!.currentAmount)}", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                    Spacer(modifier = Modifier.height(14.dp))
                    OutlinedTextField(
                        value = TextFieldValue(topUpInput, selection = TextRange(topUpInput.length)),
                        onValueChange = { topUpInput = formatRupiahInput(it.text) },
                        label = { Text("Jumlah Top Up (Rp)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = TextFieldValue(targetUpdateInput, selection = TextRange(targetUpdateInput.length)),
                        onValueChange = { targetUpdateInput = formatRupiahInput(it.text) },
                        label = { Text("Ubah Nilai Target (Rp)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val topUpVal = parseRupiahInput(topUpInput) ?: 0.0
                        val targetVal = parseRupiahInput(targetUpdateInput)
                        viewModel.updateSavingGoal(
                            selectedSavingForTopUp!!.id,
                            topUpVal,
                            targetVal
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

    if (selectedSavingForDelete != null) {
        AlertDialog(
            onDismissRequest = { selectedSavingForDelete = null },
            title = { Text("Hapus Tabungan?", fontWeight = FontWeight.Bold) },
            text = {
                Text("Yakin ingin menghapus ${selectedSavingForDelete!!.targetName}? Data ini tidak bisa dikembalikan.")
            },
            confirmButton = {
                Button(
                    onClick = { viewModel.deleteSaving(selectedSavingForDelete!!.id) },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = DeleteRed,
                        contentColor = Color.White
                    )
                ) {
                    Text("Hapus")
                }
            },
            dismissButton = {
                Button(
                    onClick = { selectedSavingForDelete = null },
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
    borderColor: Color,
    onTopUpClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    val progress = if (saving.targetAmount > 0) (saving.currentAmount / saving.targetAmount).toFloat() else 0f
    val progressPercent = (progress * 100).toInt().coerceIn(0, 100)

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        modifier = Modifier
            .fillMaxWidth()
            .border(1.5.dp, borderColor, RoundedCornerShape(16.dp))
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp)
        ) {
            IconButton(
                onClick = onDeleteClick,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .size(32.dp)
            ) {
                Icon(
                    imageVector = Icons.Filled.Delete,
                    contentDescription = "Hapus",
                    tint = DeleteRed,
                    modifier = Modifier.size(18.dp)
                )
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(end = 42.dp)
            ) {
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

                Spacer(modifier = Modifier.height(8.dp))

                IconButton(
                    onClick = onTopUpClick,
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.Payments,
                        contentDescription = "Top Up",
                        tint = Color(0xFF1E6091)
                    )
                }
            }
        }
    }
}
