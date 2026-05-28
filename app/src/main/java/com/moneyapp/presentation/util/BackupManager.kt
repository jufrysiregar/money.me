package com.moneyapp.presentation.util

import android.content.Context
import com.moneyapp.data.local.db.AppDatabase
import com.moneyapp.data.local.db.entity.InvestmentEntity
import com.moneyapp.data.local.db.entity.SavingEntity
import com.moneyapp.data.local.db.entity.TransactionEntity
import com.moneyapp.data.local.db.entity.UserEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.BufferedInputStream
import java.io.BufferedOutputStream
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.OutputStream
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream
import java.util.zip.ZipOutputStream

/**
 * Handles full ZIP-based backup and restore of the database and attachment photos.
 * Keeps data secure, offline-first, and lightweight (Satisfies O03, L03, L04).
 */
object BackupManager {

    private const val BACKUP_JSON_NAME = "backup_data.json"
    private const val SCHEMA_VERSION = 1

    suspend fun performBackupToStream(
        context: Context,
        db: AppDatabase,
        fileName: String,
        outputStream: OutputStream
    ) = withContext(Dispatchers.IO) {
        val tempBackup = performBackup(context, db, fileName)
        FileInputStream(tempBackup).use { input ->
            input.copyTo(outputStream)
        }
        tempBackup.delete()
    }

    /**
     * Packages the entire local database (users, transactions, investments, savings)
     * as JSON and bundles it along with the captured camera photos into a single ZIP file.
     */
    suspend fun performBackup(context: Context, db: AppDatabase, fileName: String): File = withContext(Dispatchers.IO) {
        val baseDir = File(context.getExternalFilesDir(null) ?: context.filesDir, "Documents/Money.Me")
        val photosDir = File(baseDir, "photos")
        val exportsDir = File(baseDir, "exports").apply { if (!exists()) mkdirs() }
        val tempDir = File(context.cacheDir, "backup_temp").apply { if (!exists()) mkdirs() }

        // 1. Gather all database records
        val user = db.userDao().getUser().first()
        val transactions = db.transactionDao().getAllTransactions().first()
        val investments = db.investmentDao().getAllInvestments().first()
        val savings = db.savingDao().getAllSavings().first()

        // 2. Build JSONObject
        val backupJson = JSONObject().apply {
            put("backupVersion", SCHEMA_VERSION)

            // User Object
            if (user != null) {
                put("user", JSONObject().apply {
                    put("id", user.id)
                    put("fullName", user.fullName)
                })
            } else {
                put("user", JSONObject.NULL)
            }

            // Transactions Array
            val txArray = JSONArray()
            for (tx in transactions) {
                txArray.put(JSONObject().apply {
                    put("id", tx.id)
                    put("type", tx.type)
                    put("amount", tx.amount)
                    put("category", tx.category)
                    put("date", tx.date)
                    put("note", tx.note)
                    put("photoPath", tx.photoPath ?: JSONObject.NULL)
                })
            }
            put("transactions", txArray)

            // Investments Array
            val invArray = JSONArray()
            for (inv in investments) {
                invArray.put(JSONObject().apply {
                    put("id", inv.id)
                    put("name", inv.name)
                    put("amount", inv.amount)
                    put("currentValue", inv.currentValue)
                    put("date", inv.date)
                })
            }
            put("investments", invArray)

            // Savings Array
            val savArray = JSONArray()
            for (sav in savings) {
                savArray.put(JSONObject().apply {
                    put("id", sav.id)
                    put("targetName", sav.targetName)
                    put("targetAmount", sav.targetAmount)
                    put("currentAmount", sav.currentAmount)
                    put("targetDate", sav.targetDate)
                })
            }
            put("savings", savArray)
        }

        // 3. Write JSON file to temp folder
        val jsonFile = File(tempDir, BACKUP_JSON_NAME)
        FileOutputStream(jsonFile).use { out ->
            out.write(backupJson.toString(2).toByteArray())
        }

        // 4. Create ZIP output stream inside exports/
        val zipFile = File(exportsDir, fileName)
        ZipOutputStream(BufferedOutputStream(FileOutputStream(zipFile))).use { zos ->
            // Add backup_data.json to zip
            val jsonBytes = jsonFile.readBytes()
            val jsonEntry = ZipEntry(BACKUP_JSON_NAME)
            zos.putNextEntry(jsonEntry)
            zos.write(jsonBytes)
            zos.closeEntry()

            // Add photos folder contents to zip
            if (photosDir.exists() && photosDir.isDirectory) {
                val photos = photosDir.listFiles() ?: emptyArray()
                for (photo in photos) {
                    if (photo.isFile) {
                        val photoBytes = photo.readBytes()
                        val photoEntry = ZipEntry("photos/${photo.name}")
                        zos.putNextEntry(photoEntry)
                        zos.write(photoBytes)
                        zos.closeEntry()
                    }
                }
            }
        }

        // Clean up temp
        jsonFile.delete()
        tempDir.deleteRecursively()

        return@withContext zipFile
    }

    /**
     * Restores database and photos by extracting a ZIP backup file.
     * Wipes current tables and replaces them with data parsed from the backup.
     */
    suspend fun performRestore(context: Context, db: AppDatabase, zipFile: File): Boolean = withContext(Dispatchers.IO) {
        val baseDir = File(context.getExternalFilesDir(null) ?: context.filesDir, "Documents/Money.Me")
        val photosDir = File(baseDir, "photos").apply { if (!exists()) mkdirs() }
        val tempDir = File(context.cacheDir, "restore_temp").apply { if (!exists()) mkdirs() }

        try {
            // 1. Unzip everything to temp
            ZipInputStream(BufferedInputStream(FileInputStream(zipFile))).use { zis ->
                var entry: ZipEntry? = zis.nextEntry
                while (entry != null) {
                    val destFile = File(tempDir, entry.name)
                    if (entry.isDirectory) {
                        destFile.mkdirs()
                    } else {
                        destFile.parentFile?.mkdirs()
                        FileOutputStream(destFile).use { fos ->
                            val buffer = ByteArray(1024)
                            var len = zis.read(buffer)
                            while (len > 0) {
                                fos.write(buffer, 0, len)
                                len = zis.read(buffer)
                            }
                        }
                    }
                    zis.closeEntry()
                    entry = zis.nextEntry
                }
            }

            // 2. Parse backup_data.json
            val jsonFile = File(tempDir, BACKUP_JSON_NAME)
            if (!jsonFile.exists()) return@withContext false

            val jsonContent = jsonFile.readText()
            val backupJson = JSONObject(jsonContent)
            val version = backupJson.optInt("backupVersion", 1)
            if (version > SCHEMA_VERSION) {
                // Incompatible future version
                return@withContext false
            }

            // 3. Clear existing database tables
            db.clearAllTables()

            // 4. Restore User Profile
            val userObj = backupJson.optJSONObject("user")
            if (userObj != null) {
                val userEntity = UserEntity(
                    id = userObj.getInt("id"),
                    fullName = userObj.getString("fullName")
                )
                db.userDao().insert(userEntity)
            }

            // 5. Restore Transactions
            val txArray = backupJson.optJSONArray("transactions") ?: JSONArray()
            for (i in 0 until txArray.length()) {
                val obj = txArray.getJSONObject(i)
                val tx = TransactionEntity(
                    id = obj.optLong("id", 0),
                    type = obj.getString("type"),
                    amount = obj.getDouble("amount"),
                    category = obj.getString("category"),
                    date = obj.getString("date"),
                    note = obj.optString("note", ""),
                    photoPath = if (obj.isNull("photoPath")) null else obj.getString("photoPath")
                )
                db.transactionDao().insert(tx)
            }

            // 6. Restore Investments
            val invArray = backupJson.optJSONArray("investments") ?: JSONArray()
            for (i in 0 until invArray.length()) {
                val obj = invArray.getJSONObject(i)
                val inv = InvestmentEntity(
                    id = obj.optLong("id", 0),
                    name = obj.getString("name"),
                    amount = obj.getDouble("amount"),
                    currentValue = obj.getDouble("currentValue"),
                    date = obj.getString("date")
                )
                db.investmentDao().insert(inv)
            }

            // 7. Restore Savings
            val savArray = backupJson.optJSONArray("savings") ?: JSONArray()
            for (i in 0 until savArray.length()) {
                val obj = savArray.getJSONObject(i)
                val sav = SavingEntity(
                    id = obj.optLong("id", 0),
                    targetName = obj.getString("targetName"),
                    targetAmount = obj.getDouble("targetAmount"),
                    currentAmount = obj.getDouble("currentAmount"),
                    targetDate = obj.getString("targetDate")
                )
                db.savingDao().insert(sav)
            }

            // 8. Restore Attachment Photos from ZIP
            val tempPhotosDir = File(tempDir, "photos")
            if (tempPhotosDir.exists() && tempPhotosDir.isDirectory) {
                val photos = tempPhotosDir.listFiles() ?: emptyArray()
                for (photo in photos) {
                    if (photo.isFile) {
                        val destPhoto = File(photosDir, photo.name)
                        photo.copyTo(destPhoto, overwrite = true)
                    }
                }
            }

            // Clean up temp folder
            tempDir.deleteRecursively()
            return@withContext true

        } catch (e: Exception) {
            e.printStackTrace()
            tempDir.deleteRecursively()
            return@withContext false
        }
    }
}
