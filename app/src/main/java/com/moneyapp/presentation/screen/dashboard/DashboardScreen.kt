package com.moneyapp.presentation.screen.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Savings
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.TextStyle as ComposeTextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.moneyapp.presentation.navigation.Screen
import java.text.NumberFormat
import java.time.LocalDate
import java.time.format.TextStyle
import java.util.Locale

/**
 * Helper function to format double values as Indonesian Rupiah currency.
 */
fun formatRupiah(value: Double): String {
    val formatter = NumberFormat.getCurrencyInstance(Locale("id", "ID"))
    return formatter.format(value).replace(",00", "").replace("Rp", "Rp ")
}

/**
 * DashboardScreen displays user name greeting, top CTA Alert banner, grid cards,
 * and monthly summaries reactively. Satisfies F02, D01, D02, D03, D04.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    navController: NavController,
    viewModel: DashboardViewModel = hiltViewModel()
) {
    val summary by viewModel.dashboardSummary.collectAsState()

    // Determine current month text
    val currentMonthName = LocalDate.now().month.getDisplayName(TextStyle.FULL, Locale("id", "ID"))
    val currentYear = LocalDate.now().year

    // Responsiveness variables
    val configuration = LocalConfiguration.current
    val isTablet = configuration.screenWidthDp >= 600
    val arrowTransition = rememberInfiniteTransition(label = "cta-arrow")
    val arrowOffset = arrowTransition.animateFloat(
        initialValue = 0f,
        targetValue = 8f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 700),
            repeatMode = RepeatMode.Reverse
        ),
        label = "cta-arrow-offset"
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Money.me",
                        fontWeight = FontWeight.Black,
                        fontSize = 30.sp,
                        lineHeight = 34.sp,
                        style = ComposeTextStyle(
                            brush = Brush.linearGradient(
                                colors = listOf(
                                    Color(0xFF2DD4BF),
                                    Color(0xFFEC4899),
                                    Color(0xFF8B5CF6),
                                    Color.White
                                )
                            )
                        ),
                        modifier = Modifier.padding(end = 8.dp)
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(innerPadding)
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(8.dp))

            // Greeting Section (Satisfies F02)
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Halo, ${summary?.userName ?: "Wahyu Pradana"} 👋",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Quick CTA Banner Card (Satisfies D01)
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { navController.navigate(Screen.TransactionForm.createRoute()) }
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            Brush.horizontalGradient(
                                listOf(
                                    Color(0xFF1E6091).copy(alpha = 0.15f),
                                    Color(0xFF52B788).copy(alpha = 0.05f)
                                )
                            )
                        )
                        .padding(20.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "📝 Transaksi apa saja yang sudah dilakukan hari ini?",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.End
                        ) {
                            Text(
                                text = "Mulai",
                                fontWeight = FontWeight.ExtraBold,
                                color = Color(0xFF1E6091),
                                fontSize = 15.sp
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Icon(
                                imageVector = Icons.Filled.ArrowForward,
                                contentDescription = "Add Transaction",
                                tint = Color(0xFF1E6091),
                                modifier = Modifier
                                    .offset(x = arrowOffset.value.dp)
                                    .size(16.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // 4 Grid Cards: Investasi, Tabungan, Pengeluaran, Pemasukan (Satisfies D02)
            val gridColumns = if (isTablet) GridCells.Fixed(4) else GridCells.Fixed(2)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(if (isTablet) 120.dp else 220.dp)
            ) {
                LazyVerticalGrid(
                    columns = gridColumns,
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    // Card 1: Investasi (Hijau Tua #2D6A4F)
                    item {
                        DashboardAssetCard(
                            title = "INVESTASI",
                            value = summary?.totalInvestment ?: 0.0,
                            icon = "📈",
                            backgroundColor = Color(0xFF2D6A4F),
                            onClick = { navController.navigate(Screen.Investment.route) }
                        )
                    }
                    // Card 2: Tabungan (Biru #1E6091)
                    item {
                        DashboardAssetCard(
                            title = "TABUNGAN",
                            value = summary?.totalSaving ?: 0.0,
                            icon = "",
                            backgroundColor = Color(0xFF1E6091),
                            onClick = { navController.navigate(Screen.Saving.route) }
                        )
                    }
                    // Card 3: Pengeluaran (Amber #F4A261 with warning triangle icon)
                    item {
                        DashboardAssetCard(
                            title = "PENGELUARAN",
                            value = summary?.totalExpense ?: 0.0,
                            icon = "🧾",
                            backgroundColor = Color(0xFFF4A261),
                            onClick = { navController.navigate(Screen.Transaction.route) }
                        )
                    }
                    // Card 4: Pemasukan (Hijau Muda #52B788)
                    item {
                        DashboardAssetCard(
                            title = "PEMASUKAN",
                            value = summary?.totalIncome ?: 0.0,
                            icon = "💰",
                            backgroundColor = Color(0xFF52B788),
                            onClick = { navController.navigate(Screen.Transaction.route) }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Monthly Summary Card (Satisfies D03 & D04)
            Card(
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 24.dp)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "📋 RINGKASAN $currentMonthName $currentYear".uppercase(),
                            style = MaterialTheme.typography.titleSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                                letterSpacing = 1.sp
                            )
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Total Transaksi",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                        )
                        Text(
                            text = "${summary?.transactionCount ?: 0}x",
                            style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Total Pengeluaran",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                        )
                        Text(
                            text = formatRupiah(summary?.totalExpense ?: 0.0),
                            style = MaterialTheme.typography.bodyLarge.copy(
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFF4A261)
                            )
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(1.dp)
                            .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f))
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Total Keseluruhan",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        val netBalance = summary?.netBalance ?: 0.0
                        val netBalanceColor = if (netBalance >= 0) Color(0xFF2D6A4F) else Color(0xFFE63946)
                        Text(
                            text = formatRupiah(netBalance),
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.ExtraBold,
                                color = netBalanceColor
                            )
                        )
                    }
                }
            }
        }
    }
}

/**
 * Gorgeous customized card layout for asset counters inside Dashboard.
 */
@Composable
private fun DashboardAssetCard(
    title: String,
    value: Double,
    icon: String,
    backgroundColor: Color,
    onClick: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = backgroundColor),
        modifier = Modifier
            .fillMaxWidth()
            .height(100.dp)
            .clickable { onClick() }
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White.copy(alpha = 0.8f)
                )
                if (title == "TABUNGAN") {
                    Icon(
                        imageVector = Icons.Filled.Savings,
                        contentDescription = title,
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                } else {
                    Text(text = icon, fontSize = 16.sp)
                }
            }
            Column {
                Text(
                    text = formatRupiah(value),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color.White
                )
            }
        }
    }
}
