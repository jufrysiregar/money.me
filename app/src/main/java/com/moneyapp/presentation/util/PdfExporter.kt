package com.moneyapp.presentation.util

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import com.moneyapp.domain.model.Transaction
import com.moneyapp.domain.model.TransactionType
import java.io.File
import java.io.FileOutputStream
import java.time.format.DateTimeFormatter
import java.util.Locale

/**
 * Utility class to export transaction lists to a beautifully styled PDF document.
 * Uses Android's native [PdfDocument] and [Canvas] APIs.
 */
object PdfExporter {

    private val dateFormatter = DateTimeFormatter.ofPattern("dd MMMM yyyy", Locale("id", "ID"))

    /**
     * Exports a list of transactions into a PDF file saved at /Documents/Money.Me/.
     *
     * @param context App context to access directories
     * @param transactions List of transactions to export
     * @param periodName Label for the report period (e.g., "Mei 2026")
     * @return File referencing the generated PDF
     */
    fun exportToPdf(context: Context, transactions: List<Transaction>, periodName: String): File {
        val pdfDocument = PdfDocument()

        // Page info: A4 dimensions roughly 595 x 842 points
        val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create()
        val page = pdfDocument.startPage(pageInfo)
        val canvas: Canvas = page.canvas

        // Paints for drawing elements
        val titlePaint = Paint().apply {
            color = Color.parseColor("#1E6091") // Primary Blue
            textSize = 22f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            isAntiAlias = true
        }

        val subTitlePaint = Paint().apply {
            color = Color.parseColor("#6C757D") // Muted grey
            textSize = 12f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
            isAntiAlias = true
        }

        val headerPaint = Paint().apply {
            color = Color.parseColor("#212529") // Dark Text
            textSize = 11f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            isAntiAlias = true
        }

        val bodyPaint = Paint().apply {
            color = Color.parseColor("#212529")
            textSize = 10f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
            isAntiAlias = true
        }

        val greenPaint = Paint().apply {
            color = Color.parseColor("#2D6A4F") // Success Green
            textSize = 10f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            isAntiAlias = true
        }

        val orangePaint = Paint().apply {
            color = Color.parseColor("#F4A261") // Danger/Warning Amber
            textSize = 10f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            isAntiAlias = true
        }

        val linePaint = Paint().apply {
            color = Color.parseColor("#DEE2E6") // Soft line separator
            strokeWidth = 1f
            isAntiAlias = true
        }

        val fillHeaderPaint = Paint().apply {
            color = Color.parseColor("#F8F9FA") // Light header BG
            style = Paint.Style.FILL
        }

        // ── 1. Header Section ────────────────────────────────────────────────
        canvas.drawText("Laporan Keuangan Money.Me", 40f, 60f, titlePaint)
        canvas.drawText("Periode: $periodName", 40f, 80f, subTitlePaint)
        canvas.drawText("Dibuat otomatis oleh Money.Me Offline Tracker", 40f, 95f, subTitlePaint)

        // Divider
        canvas.drawLine(40f, 115f, 555f, 115f, linePaint)

        // ── 2. Summary Block ─────────────────────────────────────────────────
        val totalIncome = transactions.filter { it.type == TransactionType.INCOME }.sumOf { it.amount }
        val totalExpense = transactions.filter { it.type == TransactionType.EXPENSE }.sumOf { it.amount }
        val balance = totalIncome - totalExpense

        canvas.drawText("Ringkasan Keuangan", 40f, 140f, headerPaint)
        canvas.drawText("Total Pemasukan: Rp %,.0f".format(Locale("id", "ID"), totalIncome), 40f, 160f, greenPaint)
        canvas.drawText("Total Pengeluaran: Rp %,.0f".format(Locale("id", "ID"), totalExpense), 40f, 180f, orangePaint)

        val balancePaint = if (balance >= 0) greenPaint else orangePaint
        canvas.drawText("Selisih Bersih: Rp %,.0f".format(Locale("id", "ID"), balance), 40f, 200f, balancePaint)

        // Divider
        canvas.drawLine(40f, 220f, 555f, 220f, linePaint)

        // ── 3. Table Column Headers ──────────────────────────────────────────
        val startY = 240f
        canvas.drawRect(40f, startY, 555f, startY + 25f, fillHeaderPaint)
        canvas.drawLine(40f, startY, 555f, startY, linePaint)
        canvas.drawLine(40f, startY + 25f, 555f, startY + 25f, linePaint)

        canvas.drawText("Tanggal", 45f, startY + 17f, headerPaint)
        canvas.drawText("Tipe", 140f, startY + 17f, headerPaint)
        canvas.drawText("Kategori", 200f, startY + 17f, headerPaint)
        canvas.drawText("Catatan", 300f, startY + 17f, headerPaint)
        canvas.drawText("Nominal", 460f, startY + 17f, headerPaint)

        // ── 4. Table Body ────────────────────────────────────────────────────
        var currentY = startY + 25f
        val rowHeight = 25f

        for (tx in transactions) {
            // Check page boundary (leave some margin at the bottom)
            if (currentY + rowHeight > 800f) {
                // For simplicity, we just stop drawing (A4 single page is usually sufficient for standard exports)
                // Real-world multi-page support can be added if required
                canvas.drawText("... daftar terpotong (halaman penuh) ...", 40f, currentY + 15f, subTitlePaint)
                break
            }

            val dateStr = try {
                tx.date.format(dateFormatter)
            } catch (e: Exception) {
                tx.date.toString()
            }

            // Draw columns text
            canvas.drawText(dateStr, 45f, currentY + 17f, bodyPaint)

            val typeText = if (tx.type == TransactionType.INCOME) "Pemasukan" else "Pengeluaran"
            val typePaint = if (tx.type == TransactionType.INCOME) greenPaint else orangePaint
            canvas.drawText(typeText, 140f, currentY + 17f, typePaint)

            canvas.drawText(tx.category, 200f, currentY + 17f, bodyPaint)

            val noteSnippet = if (tx.note.length > 25) tx.note.substring(0, 22) + "..." else tx.note
            canvas.drawText(noteSnippet, 300f, currentY + 17f, bodyPaint)

            val amountSign = if (tx.type == TransactionType.INCOME) "+" else "-"
            val amountText = "$amountSign Rp %,.0f".format(Locale("id", "ID"), tx.amount)
            canvas.drawText(amountText, 460f, currentY + 17f, typePaint)

            // Draw table grid separator
            canvas.drawLine(40f, currentY + rowHeight, 555f, currentY + rowHeight, linePaint)
            currentY += rowHeight
        }

        // Draw side borders of table
        canvas.drawLine(40f, startY, 40f, currentY, linePaint)
        canvas.drawLine(555f, startY, 555f, currentY, linePaint)

        pdfDocument.finishPage(page)

        // ── 5. File Output Preparation ───────────────────────────────────────
        val exportDir = File(context.getExternalFilesDir(null) ?: context.filesDir, "Documents/Money.Me")
        if (!exportDir.exists()) {
            exportDir.mkdirs()
        }

        // Clean period name for file compatibility
        val cleanPeriod = periodName.replace(" ", "_")
        val pdfFile = File(exportDir, "laporan_$cleanPeriod.pdf")

        pdfDocument.writeTo(FileOutputStream(pdfFile))
        pdfDocument.close()

        return pdfFile
    }
}
