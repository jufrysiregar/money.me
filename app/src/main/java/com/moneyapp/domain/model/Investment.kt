package com.moneyapp.domain.model

import java.time.LocalDate

/**
 * Domain model for Investment.
 * 
 * @property id Unique identifier
 * @property name Investment name (e.g., "BBCA", "Reksadana A")
 * @property amount Initial investment amount (backward compatibility)
 * @property currentValue Current market value (backward compatibility)
 * @property date Purchase date
 * @property notes Additional notes (optional)
 * @property photoPath Path to photo evidence (optional)
 * @property averagePrice Average purchase price per unit (AVG) - optional
 * @property currentPrice Current market price per unit - optional
 * @property totalAmount Total money invested in this asset - optional
 * @property isSold Whether this investment has been sold
 * @property soldDate Date when investment was sold
 * @property soldPrice Price per unit when sold
 * @property createdAt Creation timestamp
 * @property updatedAt Last update timestamp
 */
data class Investment(
    val id: Long = 0,
    val name: String,
    val amount: Double,                    // Modal awal (backward compatibility)
    val currentValue: Double,              // Nilai sekarang (backward compatibility)
    val date: LocalDate,
    val notes: String? = null,             // NEW: Catatan
    val photoPath: String? = null,         // NEW: Path foto
    val averagePrice: Double? = null,      // NEW: Harga rata-rata beli (AVG)
    val currentPrice: Double? = null,      // NEW: Harga saham saat ini di pasar
    val totalAmount: Double? = null,       // NEW: Total uang investasi
    val isSold: Boolean = false,           // NEW: Sudah dijual?
    val soldDate: LocalDate? = null,       // NEW: Tanggal jual
    val soldPrice: Double? = null,         // NEW: Harga jual per lembar
    val createdAt: String = "",            // NEW: Timestamp dibuat
    val updatedAt: String = ""             // NEW: Timestamp terakhir diupdate
) {
    
    // ============================================================
    //  FUNGSI HELPER
    // ============================================================
    
    /**
     * Mengecek apakah investasi masih aktif (belum dijual)
     */
    fun isActive(): Boolean = !isSold
    
    /**
     * Mendapatkan total nilai investasi saat ini (Market Value)
     */
    fun getCurrentValueOrFallback(): Double {
        // Gunakan data baru jika tersedia
        if (averagePrice != null && currentPrice != null && totalAmount != null) {
            if (averagePrice != 0.0 && totalAmount != 0.0) {
                return (totalAmount / averagePrice) * currentPrice
            }
        }
        // Fallback ke kolom lama
        return currentValue
    }
    
    /**
     * Mendapatkan total modal investasi
     */
    fun getTotalInvestment(): Double {
        return totalAmount ?: amount
    }
    
    /**
     * Mendapatkan average price
     */
    fun getAveragePriceOrFallback(): Double? {
        return averagePrice
    }
    
    /**
     * Mendapatkan harga saham saat ini
     */
    fun getCurrentPriceOrFallback(): Double? {
        return currentPrice ?: currentValue
    }
    
    /**
     * Menghitung Profit/Loss (Unrealized) untuk investasi yang masih aktif
     */
    fun getProfitLoss(): Double? {
        if (isSold) return null
        val currentValue = getCurrentValueOrFallback()
        val totalInvestment = getTotalInvestment()
        return currentValue - totalInvestment
    }
    
    /**
     * Menghitung persentase Profit/Loss
     */
    fun getProfitLossPercentage(): Double? {
        val profitLoss = getProfitLoss() ?: return null
        val totalInvestment = getTotalInvestment()
        if (totalInvestment == 0.0) return 0.0
        return (profitLoss / totalInvestment) * 100
    }
    
    /**
     * Mendapatkan status investasi
     */
    fun getStatus(): String {
        if (isSold) return "SUDAH DIJUAL"
        val profitLoss = getProfitLoss()
        return when {
            profitLoss == null -> "BELUM DIHITUNG"
            profitLoss > 0 -> "PROFIT ✅"
            profitLoss < 0 -> "LOSS ⚠️"
            else -> "IMBAS ➖"
        }
    }
    
    /**
     * Mendapatkan warna status
     */
    fun getStatusColor(): String {
        if (isSold) return "#6C757D"  // Abu-abu
        val profitLoss = getProfitLoss()
        return when {
            profitLoss == null -> "#8D99AE"
            profitLoss > 0 -> "#2D6A4F"  // Hijau
            profitLoss < 0 -> "#F4A261"  // Kuning
            else -> "#8D99AE"
        }
    }
    
    /**
     * Mendapatkan total nilai jual (jika sudah dijual)
     */
    fun getTotalSoldValue(): Double? {
        if (!isSold) return null
        if (soldPrice == null || averagePrice == null || totalAmount == null) return null
        if (averagePrice == 0.0) return null
        return (totalAmount / averagePrice) * soldPrice
    }
    
    /**
     * Menghitung Profit/Loss Realisasi (sudah dijual)
     */
    fun getRealizedProfitLoss(): Double? {
        if (!isSold) return null
        val totalSold = getTotalSoldValue() ?: return null
        val totalInvestment = getTotalInvestment()
        return totalSold - totalInvestment
    }
    
    /**
     * Menghitung persentase Profit/Loss Realisasi
     */
    fun getRealizedProfitLossPercentage(): Double? {
        val profitLoss = getRealizedProfitLoss() ?: return null
        val totalInvestment = getTotalInvestment()
        if (totalInvestment == 0.0) return 0.0
        return (profitLoss / totalInvestment) * 100
    }
}