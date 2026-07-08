package com.moneyapp.data.local.db.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Room entity representing an investment record.
 *
 * This entity stores all investment data including:
 * - Basic info: name, purchase date, notes, photo
 * - Financial details: average price, current price, total amount
 * - Status: active or sold (with sale details)
 *
 * Version History:
 * - Initial: amount (modal awal), currentValue (nilai sekarang), date
 * - Current: added averagePrice, currentPrice, totalAmount, isSold, soldDate, soldPrice
 *
 * All new columns are optional (nullable) to maintain backward compatibility.
 * 
 * @property id Unique identifier
 * @property name Nama saham/investasi
 * @property amount Modal awal investasi (UANG YANG DIKELUARKAN) - untuk backward compatibility
 * @property currentValue Nilai sekarang investasi (HARGA SAHAM SAAT INI) - untuk backward compatibility
 * @property date Tanggal beli (ISO-8601: "yyyy-MM-dd")
 * @property notes Catatan tambahan (opsional)
 * @property photoPath Path foto bukti (opsional)
 * @property averagePrice Harga rata-rata beli per lembar (AVG) - opsional
 * @property currentPrice Harga saham saat ini di pasar - opsional
 * @property totalAmount Total uang yang diinvestasikan di saham ini - opsional
 * @property isSold Status apakah sudah dijual (default: false)
 * @property soldDate Tanggal penjualan
 * @property soldPrice Harga jual per lembar
 * @property createdAt Timestamp dibuat
 * @property updatedAt Timestamp terakhir diupdate
 */
@Entity(tableName = "investments")
data class InvestmentEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    // === KOLOM LAMA (Backward Compatibility) ===
    
    @ColumnInfo(name = "name")
    val name: String,

    @ColumnInfo(name = "amount")
    val amount: Double,                        // Modal awal (backward compatibility)
    
    @ColumnInfo(name = "current_value")
    val currentValue: Double,                  // Nilai sekarang (backward compatibility)
    
    @ColumnInfo(name = "date")
    val date: String,                          // ISO-8601: "yyyy-MM-dd"
    
    // === KOLOM BARU (OPSIONAL) ===
    
    @ColumnInfo(name = "notes")
    val notes: String? = null,                 // Catatan tambahan
    
    @ColumnInfo(name = "photo_path")
    val photoPath: String? = null,             // Path foto bukti
    
    @ColumnInfo(name = "average_price")
    val averagePrice: Double? = null,          // Harga rata-rata beli (AVG)
    
    @ColumnInfo(name = "current_price")
    val currentPrice: Double? = null,          // Harga saham saat ini di pasar
    
    @ColumnInfo(name = "total_amount")
    val totalAmount: Double? = null,           // Total uang investasi
    
    @ColumnInfo(name = "is_sold")
    val isSold: Boolean = false,               // Sudah dijual?
    
    @ColumnInfo(name = "sold_date")
    val soldDate: String? = null,              // Tanggal jual
    
    @ColumnInfo(name = "sold_price")
    val soldPrice: Double? = null,             // Harga jual per lembar
    
    @ColumnInfo(name = "created_at")
    val createdAt: String = "",                // Timestamp dibuat
    
    @ColumnInfo(name = "updated_at")
    val updatedAt: String = ""                 // Timestamp terakhir diupdate
) {
    
    // ============================================================
    //  FUNGSI HELPER UNTUK PERHITUNGAN INVESTASI
    // ============================================================
    
    /**
     * Mengecek apakah investasi masih aktif (belum dijual)
     */
    fun isActive(): Boolean = !isSold
    
    /**
     * Mengecek apakah investasi sudah dijual
     */
    fun isSoldInvestment(): Boolean = isSold
    
    /**
     * Mendapatkan total nilai investasi saat ini (Market Value)
     * Untuk investasi yang masih aktif:
     *   - Jika ada averagePrice, currentPrice, totalAmount → hitung dari data baru
     *   - Jika tidak, fallback ke currentValue (kolom lama)
     * 
     * Untuk investasi yang sudah dijual: return null
     */
    fun getCurrentValue(): Double? {
        if (isSold) return null
        
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
     * Gunakan totalAmount (baru) jika ada, fallback ke amount (lama)
     */
    fun getTotalInvestment(): Double {
        return totalAmount ?: amount
    }
    
    /**
     * Mendapatkan average price
     * Gunakan averagePrice (baru) jika ada, fallback ke amount / jumlah unit (tidak bisa dihitung)
     */
    fun getAveragePrice(): Double? {
        return averagePrice
    }
    
    /**
     * Mendapatkan harga saham saat ini
     * Gunakan currentPrice (baru) jika ada, fallback ke currentValue (lama)
     */
    fun getCurrentPrice(): Double? {
        return currentPrice ?: currentValue
    }
    
    /**
     * Menghitung Profit/Loss (Unrealized) untuk investasi yang masih aktif
     * 
     * Rumus: (Total Nilai Saat Ini) - (Total Modal)
     * 
     * Return: 
     *   - Positive = Profit (untung)
     *   - Negative = Loss (rugi)
     *   - Null = tidak bisa dihitung
     */
    fun getProfitLoss(): Double? {
        if (isSold) return null
        
        val currentValue = getCurrentValue() ?: return null
        val totalInvestment = getTotalInvestment()
        
        return currentValue - totalInvestment
    }
    
    /**
     * Menghitung persentase Profit/Loss
     * 
     * Rumus: (ProfitLoss / Total Modal) × 100
     */
    fun getProfitLossPercentage(): Double? {
        val profitLoss = getProfitLoss() ?: return null
        val totalInvestment = getTotalInvestment()
        
        if (totalInvestment == 0.0) return 0.0
        
        return (profitLoss / totalInvestment) * 100
    }
    
    /**
     * Mendapatkan status investasi
     * 
     * @return "PROFIT", "LOSS", atau "IMBAS"
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
            profitLoss == null -> "#8D99AE"   // Abu-abu
            profitLoss > 0 -> "#2D6A4F"       // Hijau
            profitLoss < 0 -> "#F4A261"       // Kuning
            else -> "#8D99AE"                 // Abu-abu
        }
    }
    
    /**
     * Mendapatkan total nilai jual (jika sudah dijual)
     * 
     * Rumus: (Total Modal / Average Price) × Harga Jual
     */
    fun getTotalSoldValue(): Double? {
        if (!isSold) return null
        if (soldPrice == null || averagePrice == null || totalAmount == null) return null
        if (averagePrice == 0.0) return null
        
        return (totalAmount / averagePrice) * soldPrice
    }
    
    /**
     * Menghitung Profit/Loss Realisasi (sudah dijual)
     * 
     * Rumus: Total Nilai Jual - Total Modal
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
    
    /**
     * Mendapatkan deskripsi singkat investasi untuk ditampilkan di card
     */
    fun getSummary(): String {
        return buildString {
            append("📊 $name")
            if (isSold) {
                append(" (SUDAH DIJUAL)")
            }
            append("\n")
            
            // Tampilkan data yang tersedia
            averagePrice?.let {
                append("AVG: Rp ${formatNumber(it)}/lembar\n")
            }
            currentPrice?.let {
                append("Harga: Rp ${formatNumber(it)}/lembar\n")
            }
            totalAmount?.let {
                append("Total: Rp ${formatNumber(it)}\n")
            }
            
            // Tampilkan profit/loss
            if (isSold) {
                getRealizedProfitLoss()?.let { profitLoss ->
                    val isProfit = profitLoss > 0
                    append("${if (isProfit) "🟢" else "⚠️"} ")
                    append("${if (isProfit) "PROFIT" else "LOSS"}: Rp ${formatNumber(profitLoss)}")
                    getRealizedProfitLossPercentage()?.let { percentage ->
                        append(" (${String.format("%.2f", percentage)}%)")
                    }
                }
            } else {
                getProfitLoss()?.let { profitLoss ->
                    val isProfit = profitLoss > 0
                    append("${if (isProfit) "🟢" else "⚠️"} ")
                    append("${if (isProfit) "PROFIT" else "LOSS"}: Rp ${formatNumber(profitLoss)}")
                    getProfitLossPercentage()?.let { percentage ->
                        append(" (${String.format("%.2f", percentage)}%)")
                    }
                }
            }
        }
    }
    
    // ============================================================
    //  FUNGSI FORMAT ANGKA
    // ============================================================
    
    private fun formatNumber(amount: Double): String {
        return if (amount == 0.0) "0" else {
            val formatter = java.text.DecimalFormat("#,###,###.##")
            formatter.format(amount)
        }
    }
}

// ============================================================
//  FUNGSI EXTENSION UNTUK KOLEKSI INVESTASI
// ============================================================

/**
 * Menghitung total nilai investasi dari list investasi aktif
 */
fun List<InvestmentEntity>.getTotalActiveValue(): Double {
    return this.filter { !it.isSold }
        .mapNotNull { it.getCurrentValue() }
        .sum()
}

/**
 * Menghitung total profit/loss dari list investasi aktif
 */
fun List<InvestmentEntity>.getTotalActiveProfitLoss(): Double {
    return this.filter { !it.isSold }
        .mapNotNull { it.getProfitLoss() }
        .sum()
}

/**
 * Menghitung total modal investasi dari list investasi
 */
fun List<InvestmentEntity>.getTotalInvestmentAmount(): Double {
    return this.sumOf { it.getTotalInvestment() }
}

/**
 * Mendapatkan investasi yang masih aktif
 */
fun List<InvestmentEntity>.getActiveInvestments(): List<InvestmentEntity> {
    return this.filter { !it.isSold }
}

/**
 * Mendapatkan investasi yang sudah dijual
 */
fun List<InvestmentEntity>.getSoldInvestments(): List<InvestmentEntity> {
    return this.filter { it.isSold }
}