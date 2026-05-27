package com.moneyapp.presentation.component

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.moneyapp.presentation.theme.Danger
import com.moneyapp.presentation.theme.Primary
import com.moneyapp.presentation.theme.Success
import com.moneyapp.presentation.util.formatRupiah

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
 *
 * Warna nominal disesuaikan dengan tipe:
 * - INCOME    → Success (hijau)
 * - EXPENSE   → Danger (oranye)
 * - INVESTMENT, SAVING → Primary (biru)
 *
 * @param label Judul kartu, misalnya "Pemasukan", "Pengeluaran"
 * @param amount Nilai nominal dalam Double
 * @param type Tipe kartu yang menentukan warna nominal
 * @param modifier Modifier opsional
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
