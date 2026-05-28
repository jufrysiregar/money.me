package com.moneyapp.presentation.util

import java.text.NumberFormat
import java.util.Locale

/**
 * Format angka menjadi string Rupiah dengan pemisah ribuan menggunakan titik.
 *
 * Contoh:
 * - 5000000.0 → "Rp 5.000.000"
 * - 1500.5    → "Rp 1.500"
 * - 0.0       → "Rp 0"
 *
 * Validates: Requirements 16.3, 16.5
 */
fun formatRupiah(amount: Double): String {
    val formatter = NumberFormat.getNumberInstance(Locale("id", "ID"))
    return "Rp ${formatter.format(amount.toLong())}"
}

fun formatRupiahInput(value: String): String {
    val rawDigits = value.filter { it.isDigit() }
    val digits = rawDigits.trimStart('0').ifEmpty {
        if (rawDigits.isNotEmpty()) "0" else ""
    }
    if (digits.isEmpty()) return ""

    val formatter = NumberFormat.getNumberInstance(Locale("id", "ID"))
    return formatter.format(digits.toLongOrNull() ?: return digits)
}

fun parseRupiahInput(value: String): Double? {
    val digits = value.filter { it.isDigit() }
    return digits.toDoubleOrNull()
}
