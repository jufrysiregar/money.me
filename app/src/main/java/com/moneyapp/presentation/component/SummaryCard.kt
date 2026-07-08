package com.moneyapp.presentation.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.moneyapp.domain.model.Investment
import com.moneyapp.presentation.theme.Danger
import com.moneyapp.presentation.theme.Primary
import com.moneyapp.presentation.theme.Success
import com.moneyapp.presentation.util.formatRupiah
import java.time.format.DateTimeFormatter

/**
 * Tipe kartu ringkasan untuk menentukan warna nominal.
 */
enum class SummaryCardType {
    INCOME,      // Hijau (Success)
    EXPENSE,     // Oranye (Danger)
    INVESTMENT,  // Biru (Primary)
    SAVING       // Biru (Primary)
}

/**
 * Kartu ringkasan keuangan yang menampilkan label dan nominal dalam format Rupiah.
 */
@Composable
fun SummaryCard(
    label: String,
    amount: Double,
    type: SummaryCardType,
    modifier: Modifier = Modifier
) {
    val amountColor: Color = when (type) {
        SummaryCardType.INCOME -> Success
        SummaryCardType.EXPENSE -> Danger
        SummaryCardType.INVESTMENT, SummaryCardType.SAVING -> Primary
    }

    val formattedAmount = formatRupiah(amount)
    val cardContentDescription = "$label: $formattedAmount"

    Card(
        modifier = modifier
            .fillMaxWidth()
            .semantics { contentDescription = cardContentDescription },
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = formattedAmount,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = amountColor
            )
        }
    }
}

// ============================================================
//  INVESTMENT CARD (DENGAN TOMBOL JUAL)
// ============================================================

/**
 * Card untuk menampilkan detail investasi dengan tombol aksi:
 * - Edit
 * - Hapus
 * - Jual (hanya untuk investasi aktif)
 */
@Composable
fun InvestmentCard(
    investment: Investment,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onSell: (() -> Unit)? = null,  // NULL untuk investasi yang sudah dijual
    modifier: Modifier = Modifier,
    isDarkMode: Boolean = false
) {
    val borderColor = if (isDarkMode) Color(0xFFC77DFF) else Color(0xFF9B59B6)
    val isActive = !investment.isSold
    val profitLoss = if (isActive) investment.getProfitLoss() else investment.getRealizedProfitLoss()
    val isProfit = profitLoss != null && profitLoss > 0
    val profitLossColor = when {
        profitLoss == null -> Color.Gray
        isProfit -> Success
        else -> Danger
    }
    
    val dateFormatter = DateTimeFormatter.ofPattern("dd MMM yyyy")
    
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp),
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // ============================================================
            //  HEADER: Nama + Status + Tombol Aksi
            // ============================================================
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Nama & Status
                Column {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "📈 ${investment.name}",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        
                        if (!isActive) {
                            Text(
                                text = "🔒",
                                style = MaterialTheme.typography.labelSmall
                            )
                        }
                    }
                    
                    if (!isActive) {
                        Text(
                            text = "Sudah Dijual",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.Gray
                        )
                    }
                }
                
                // Tombol Aksi (hanya untuk investasi aktif)
                if (isActive) {
                    Row {
                        // Edit
                        IconButton(
                            onClick = onEdit,
                            modifier = Modifier.size(36.dp)
                        ) {
                            Icon(
                                Icons.Default.Edit,
                                contentDescription = "Edit Investasi",
                                tint = Primary
                            )
                        }
                        
                        // Jual
                        onSell?.let {
                            IconButton(
                                onClick = it,
                                modifier = Modifier.size(36.dp)
                            ) {
                                Icon(
                                    Icons.Default.AttachMoney,
                                    contentDescription = "Jual Investasi",
                                    tint = Success
                                )
                            }
                        }
                        
                        // Hapus
                        IconButton(
                            onClick = onDelete,
                            modifier = Modifier.size(36.dp)
                        ) {
                            Icon(
                                Icons.Default.Delete,
                                contentDescription = "Hapus Investasi",
                                tint = Danger
                            )
                        }
                    }
                } else {
                    // Investasi sudah dijual: hanya tombol hapus
                    IconButton(
                        onClick = onDelete,
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            Icons.Default.Delete,
                            contentDescription = "Hapus Investasi",
                            tint = Danger
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // ============================================================
            //  BODY: Detail Investasi
            // ============================================================
            
            Column(
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                // Average Price
                investment.getAveragePriceOrFallback()?.let {
                    Text(
                        text = "📊 Average: ${formatRupiah(it)}/lembar",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
                
                // Current Price / Sold Price
                if (isActive) {
                    investment.getCurrentPriceOrFallback()?.let {
                        Text(
                            text = "📈 Harga Saat Ini: ${formatRupiah(it)}/lembar",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                } else {
                    investment.soldPrice?.let {
                        Text(
                            text = "💵 Harga Jual: ${formatRupiah(it)}/lembar",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
                
                // Total Investment
                Text(
                    text = "💰 Total Investasi: ${formatRupiah(investment.getTotalInvestment())}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                
                // Date
                if (isActive) {
                    Text(
                        text = "📅 Beli: ${investment.date.format(dateFormatter)}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                } else {
                    investment.soldDate?.let {
                        Text(
                            text = "📅 Jual: ${it.format(dateFormatter)}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                    }
                }
                
                // Notes
                investment.notes?.let {
                    Text(
                        text = "📝 $it",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // ============================================================
            //  PROFIT / LOSS
            // ============================================================
            
            profitLoss?.let { profitLossValue ->
                val percentage = if (isActive) {
                    investment.getProfitLossPercentage()
                } else {
                    investment.getRealizedProfitLossPercentage()
                }
                
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = profitLossColor.copy(alpha = 0.1f)
                    ),
                    shape = MaterialTheme.shapes.small
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (isProfit) "🟢" else "⚠️",
                            style = MaterialTheme.typography.titleMedium
                        )
                        Spacer(modifier = Modifier.size(4.dp))
                        Text(
                            text = if (isActive) {
                                "${if (isProfit) "PROFIT" else "LOSS"}"
                            } else {
                                "REALISASI ${if (isProfit) "PROFIT" else "LOSS"}"
                            },
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = profitLossColor
                        )
                        Spacer(modifier = Modifier.size(4.dp))
                        Text(
                            text = "${if (isProfit) "+" else "-"}${formatRupiah(profitLossValue)}",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = profitLossColor
                        )
                        percentage?.let {
                            Text(
                                text = " (${String.format("%.2f", if (isProfit) it else -it)}%)",
                                style = MaterialTheme.typography.bodySmall,
                                color = profitLossColor
                            )
                        }
                    }
                }
            }
        }
    }
}