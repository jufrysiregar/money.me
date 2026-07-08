package com.moneyapp.presentation.screen.report

import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.moneyapp.data.local.db.AppDatabase
import com.moneyapp.domain.model.Transaction
import com.moneyapp.domain.model.TransactionType
import com.moneyapp.presentation.util.formatRupiah
import com.moneyapp.presentation.screen.transaction.TransactionViewModel
import com.moneyapp.presentation.screen.transaction.showDatePicker
import com.moneyapp.presentation.util.PdfExporter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlin.math.abs

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReportScreen(
    navController: NavController,
    db: AppDatabase, // Inject AppDatabase directly to trigger ZIP exports
    viewModel: TransactionViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val transactions by viewModel.transactions.collectAsState()

    var activePeriodTab by remember { mutableIntStateOf(0) } // Default: Hari
    var selectedDate by remember { mutableStateOf(LocalDate.now()) }
    var horizontalDragAmount by remember { mutableFloatStateOf(0f) }
    val dateFormatter = remember { DateTimeFormatter.ofPattern("MMMM yyyy", Locale("id", "ID")) }

    // Dialog state for PDF printing progress
    var showPdfProgress by remember { mutableStateOf(false) }
    var pdfProgress by remember { mutableFloatStateOf(0f) }
    var pendingPdfTransactions by remember { mutableStateOf<List<Transaction>>(emptyList()) }
    var pendingPdfLabel by remember { mutableStateOf("") }

    fun currentPeriodLabel(): String {
        return when (activePeriodTab) {
            0 -> selectedDate.format(DateTimeFormatter.ofPattern("dd_MMMM_yyyy", Locale("id", "ID")))
            1 -> "Minggu_ke_${selectedDate.dayOfMonth / 7 + 1}_${selectedDate.format(DateTimeFormatter.ofPattern("MMMM_yyyy", Locale("id", "ID")))}"
            2 -> selectedDate.format(DateTimeFormatter.ofPattern("MMMM_yyyy", Locale("id", "ID")))
            3 -> "${selectedDate.year}"
            else -> "Laporan"
        }
    }

    val pdfDocumentLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/pdf")
    ) { uri ->
        if (uri == null) {
            Toast.makeText(context, "Penyimpanan PDF dibatalkan", Toast.LENGTH_SHORT).show()
            return@rememberLauncherForActivityResult
        }

        coroutineScope.launch {
            showPdfProgress = true
            pdfProgress = 0.2f
            try {
                withContext(Dispatchers.IO) {
                    context.contentResolver.openOutputStream(uri)?.use { output ->
                        PdfExporter.exportToPdf(output, pendingPdfTransactions, pendingPdfLabel)
                    } ?: error("Tidak bisa membuka lokasi file")
                }
                pdfProgress = 1.0f
                delay(200)
                Toast.makeText(context, "Laporan PDF berhasil disimpan", Toast.LENGTH_LONG).show()
            } catch (e: Exception) {
                Toast.makeText(context, "Gagal menyimpan PDF: ${e.message}", Toast.LENGTH_LONG).show()
            } finally {
                showPdfProgress = false
            }
        }
    }

    // Group items for the active period (Day, Week, Month, Year)
    val filteredTransactions = remember(transactions, activePeriodTab, selectedDate) {
        transactions.filter { tx ->
            when (activePeriodTab) {
                0 -> tx.date == selectedDate // Hari
                1 -> {
                    // Minggu (within 7 days)
                    val startOfWeek = selectedDate.minusDays(selectedDate.dayOfWeek.value.toLong() - 1)
                    val endOfWeek = startOfWeek.plusDays(6)
                    !tx.date.isBefore(startOfWeek) && !tx.date.isAfter(endOfWeek)
                }
                2 -> tx.date.month == selectedDate.month && tx.date.year == selectedDate.year // Bulan
                3 -> tx.date.year == selectedDate.year // Tahun
                else -> true
            }
        }
    }

    // Grouping by exact date for visual list
    val groupedByDate = remember(filteredTransactions) {
        filteredTransactions.groupBy { it.date }
    }

    // Mathematical values for chart (Mockup 3 & 4)
    val totalIncome = remember(filteredTransactions) {
        filteredTransactions.filter { it.type == TransactionType.INCOME }.sumOf { it.amount }
    }
    val totalExpense = remember(filteredTransactions) {
        filteredTransactions.filter { it.type == TransactionType.EXPENSE }.sumOf { it.amount }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("📊 Laporan Keuangan", fontWeight = FontWeight.Bold) },
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
                .pointerInput(activePeriodTab) {
                    detectHorizontalDragGestures(
                        onDragStart = { horizontalDragAmount = 0f },
                        onHorizontalDrag = { _, dragAmount ->
                            horizontalDragAmount += dragAmount
                        },
                        onDragEnd = {
                            val swipeThreshold = 80f
                            if (abs(horizontalDragAmount) > swipeThreshold) {
                                activePeriodTab = if (horizontalDragAmount < 0) {
                                    (activePeriodTab + 1).coerceAtMost(3)
                                } else {
                                    (activePeriodTab - 1).coerceAtLeast(0)
                                }
                            }
                            horizontalDragAmount = 0f
                        },
                        onDragCancel = { horizontalDragAmount = 0f }
                    )
                }
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            // Period Selector Tabs (Mockup 3)
            TabRow(
                selectedTabIndex = activePeriodTab,
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = MaterialTheme.colorScheme.primary,
                indicator = { tabPositions ->
                    TabRowDefaults.SecondaryIndicator(
                        modifier = Modifier.tabIndicatorOffset(tabPositions[activePeriodTab]),
                        color = MaterialTheme.colorScheme.primary
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
            ) {
                listOf("Hari", "Minggu", "Bulan", "Tahun").forEachIndexed { index, label ->
                    Tab(
                        selected = activePeriodTab == index,
                        onClick = { activePeriodTab = index },
                        text = { Text(label, fontWeight = FontWeight.Bold) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Date / Period Display Selector (Mockup 3)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .border(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f), RoundedCornerShape(16.dp))
                    .background(MaterialTheme.colorScheme.surface)
                    .clickable { showDatePicker(context, selectedDate) { selectedDate = it } }
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                val label = when (activePeriodTab) {
                    0 -> selectedDate.format(DateTimeFormatter.ofPattern("dd MMMM yyyy", Locale("id", "ID")))
                    1 -> "Minggu ke- ${selectedDate.dayOfMonth / 7 + 1}, ${selectedDate.format(dateFormatter)}"
                    2 -> selectedDate.format(dateFormatter)
                    3 -> "${selectedDate.year}"
                    else -> ""
                }
                Text(
                    text = label,
                    style = MaterialTheme.typography.bodyLarge.copy(
                        color = MaterialTheme.colorScheme.onSurface,
                        fontWeight = FontWeight.Bold
                    )
                )
                Icon(imageVector = Icons.Filled.CalendarMonth, contentDescription = "Pilih Periode", tint = MaterialTheme.colorScheme.primary)
            }

            Spacer(modifier = Modifier.height(20.dp))

            // 📈 GRAFIK (Comparative Bar Chart Canvas) (Mockup 4)
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text(
                        text = "📈 PERBANDINGAN ALIRAN DANA",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        letterSpacing = 0.5.sp
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    // Draw custom bars inside Canvas
                    val ratio = remember(totalIncome, totalExpense) {
                        val sum = totalIncome + totalExpense
                        if (sum > 0) (totalIncome / sum).toFloat() else 0.5f
                    }

                    Canvas(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(60.dp)
                    ) {
                        val width = size.width
                        val height = size.height

                        // Calculate split coordinate
                        val incomeWidth = width * ratio
                        val expenseWidth = width - incomeWidth

                        // Draw Green Income Bar
                        if (incomeWidth > 0) {
                            drawRoundRect(
                                color = Color(0xFF52B788),
                                topLeft = Offset(0f, 10f),
                                size = Size(incomeWidth, height - 20f),
                                cornerRadius = CornerRadius(12f, 12f)
                            )
                        }

                        // Draw Orange Expense Bar
                        if (expenseWidth > 0) {
                            drawRoundRect(
                                color = Color(0xFFF4A261),
                                topLeft = Offset(incomeWidth + 4f, 10f),
                                size = Size(expenseWidth - 4f, height - 20f),
                                cornerRadius = CornerRadius(12f, 12f)
                            )
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(modifier = Modifier.size(10.dp).clip(RoundedCornerShape(2.dp)).background(Color(0xFF52B788)))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Pemasukan: ${formatRupiah(totalIncome)}", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(modifier = Modifier.size(10.dp).clip(RoundedCornerShape(2.dp)).background(Color(0xFFF4A261)))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Pengeluaran: ${formatRupiah(totalExpense)}", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = "📋 DAFTAR TRANSAKSI",
                style = MaterialTheme.typography.titleSmall.copy(
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    letterSpacing = 0.5.sp
                )
            )

            Spacer(modifier = Modifier.height(8.dp))

            if (groupedByDate.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Text("Tidak ada transaksi untuk periode ini", color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f))
                }
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    groupedByDate.forEach { (date, list) ->
                        item {
                            Card(
                                shape = RoundedCornerShape(16.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(14.dp)) {
                                    val headerDate = date.format(DateTimeFormatter.ofPattern("d MMMM yyyy", Locale("id", "ID")))
                                    Text(headerDate, fontWeight = FontWeight.ExtraBold, color = MaterialTheme.colorScheme.primary, fontSize = 13.sp)
                                    Spacer(modifier = Modifier.height(8.dp))

                                    list.forEach { tx ->
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(vertical = 4.dp),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            val sign = if (tx.type == TransactionType.INCOME) "+" else "-"
                                            val color = if (tx.type == TransactionType.INCOME) Color(0xFF52B788) else Color(0xFFF4A261)
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Box(modifier = Modifier.size(8.dp).clip(RoundedCornerShape(4.dp)).background(color))
                                                Spacer(modifier = Modifier.width(8.dp))
                                                Text(tx.category, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                                if (tx.note.isNotBlank()) {
                                                    Text(" - ${tx.note}", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
                                                }
                                            }
                                            Text("$sign ${formatRupiah(tx.amount)}", fontWeight = FontWeight.ExtraBold, color = color, fontSize = 13.sp)
                                        }
                                    }
                                }
                            }
                        }
                    }
                    item {
                        Spacer(modifier = Modifier.height(30.dp))
                    }
                }
            }

            // Bottom Button (Cetak PDF)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp)
            ) {
                Button(
                    onClick = {
                        val label = currentPeriodLabel()
                        pendingPdfLabel = label
                        pendingPdfTransactions = filteredTransactions
                        pdfDocumentLauncher.launch("laporan_$label.pdf")
                    },
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary, contentColor = Color.White),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                ) {
                    Icon(imageVector = Icons.Filled.PictureAsPdf, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Cetak PDF", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                }
            }
        }
    }

    // ── PDF GENERATING PROGRESS DIALOG ───────────────────────────────────────
    if (showPdfProgress) {
        AlertDialog(
            onDismissRequest = { /* Prevent dismiss during action */ },
            title = { Text("⚡ Membuat PDF...", fontWeight = FontWeight.Black) },
            text = {
                Column(modifier = Modifier.fillMaxWidth()) {
                    LinearProgressIndicator(
                        progress = { pdfProgress },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(10.dp)
                            .clip(RoundedCornerShape(6.dp))
                    )
                    Spacer(modifier = Modifier.height(14.dp))
                    Text("💾 Simpan ke File Manager", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    Text("/Documents/Money.Me/laporan_${selectedDate.format(dateFormatter).replace(" ", "")}.pdf", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
                }
            },
            confirmButton = {}
        )
    }
}
