package com.moneyapp.presentation.util

import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.text.NumberFormat
import java.util.Locale

/**
 * Universal number formatter for Money.me.
 *
 * All number, currency, and percentage display in the app should use these functions.
 * Format: pemisah ribuan = titik (.), pemisah desimal = koma (,)
 *
 * Examples:
 *   formatIndonesianNumber(1045200.5)  → "1.045.200,5"
 *   formatRupiah(1045200.5)            → "Rp 1.045.200,5"
 *   parseIndonesianNumber("1.045.200,5") → 1045200.5
 *   formatPercentage(-19.48)           → "-19,48%"
 */
object NumberFormatter {

    private val idLocale = Locale("id", "ID")

    /** Symbols: titik = thousands, koma = decimal */
    private val idSymbols = DecimalFormatSymbols(idLocale)

    // ── Display formatters ────────────────────────────────────────────

    /**
     * Format angka ke format Indonesia dengan maksimal 2 desimal.
     * Contoh: 1045200.5 → "1.045.200,5" | 1000000.0 → "1.000.000"
     */
    fun formatIndonesianNumber(value: Double): String {
        val formatter = DecimalFormat("#,###.##", idSymbols)
        return formatter.format(value)
    }

    /**
     * Format angka ke Rupiah Indonesia dengan maksimal 2 desimal.
     * Contoh: 1045200.5 → "Rp 1.045.200,5" | 50.09 → "Rp 50,09"
     */
    fun formatRupiah(value: Double): String {
        return "Rp ${formatIndonesianNumber(value)}"
    }

    /**
     * Format persentase dengan selalu 2 angka di belakang koma (koma sebagai desimal).
     * Contoh: -19.48 → "-19,48%" | 5.0 → "5,00%"
     */
    fun formatPercentage(value: Double): String {
        val formatter = DecimalFormat("0.00", idSymbols)
        return "${formatter.format(value)}%"
    }

    /**
     * Format persentase dengan tanda + untuk nilai positif.
     * Contoh: 5.0 → "+5,00%" | -19.48 → "-19,48%"
     */
    fun formatPercentageSigned(value: Double): String {
        val formatted = formatPercentage(value)
        return if (value > 0) "+$formatted" else formatted
    }

    // ── Input formatters (for TextField) ─────────────────────────────

    /**
     * Format string digit mentah ke tampilan ribuan Indonesia (hanya integer).
     * Digunakan untuk input transaksi dan tabungan (tanpa desimal).
     * Contoh: "1000000" → "1.000.000"
     */
    fun formatRupiahInput(value: String): String {
        val rawDigits = value.filter { it.isDigit() }
        val digits = rawDigits.trimStart('0').ifEmpty {
            if (rawDigits.isNotEmpty()) "0" else ""
        }
        if (digits.isEmpty()) return ""
        val formatter = NumberFormat.getNumberInstance(idLocale)
        return formatter.format(digits.toLongOrNull() ?: return digits)
    }

    // ── Parsers ───────────────────────────────────────────────────────

    /**
     * Parse string format Indonesia ke Double.
     * Titik = pemisah ribuan, koma = desimal.
     * Contoh: "1.045.200,5" → 1045200.5 | "50,09" → 50.09
     */
    fun parseIndonesianNumber(input: String): Double? {
        if (input.isBlank()) return null
        return try {
            val clean = input.trim().replace(".", "").replace(",", ".")
            clean.toDoubleOrNull()
        } catch (_: Exception) {
            null
        }
    }

    /**
     * Parse string berisi hanya digit ke Double (untuk input integer).
     * Contoh: "1.000.000" → 1000000.0
     */
    fun parseRupiahInput(value: String): Double? {
        val digits = value.filter { it.isDigit() }
        return digits.toDoubleOrNull()
    }
}

// ── Top-level convenience aliases ────────────────────────────────────
// Allows callers to use `formatRupiah(x)` without `NumberFormatter.` prefix.

fun formatRupiah(value: Double): String = NumberFormatter.formatRupiah(value)
fun formatIndonesianNumber(value: Double): String = NumberFormatter.formatIndonesianNumber(value)
fun formatPercentage(value: Double): String = NumberFormatter.formatPercentage(value)
fun formatPercentageSigned(value: Double): String = NumberFormatter.formatPercentageSigned(value)
fun parseIndonesianNumber(input: String): Double? = NumberFormatter.parseIndonesianNumber(input)
fun formatRupiahInput(value: String): String = NumberFormatter.formatRupiahInput(value)
fun parseRupiahInput(value: String): Double? = NumberFormatter.parseRupiahInput(value)
