package com.moneyapp.domain.repository

import android.net.Uri
import kotlinx.coroutines.flow.Flow

interface BackupRepository {
    suspend fun createBackup(): Result<Uri>
    suspend fun restoreBackup(uri: Uri): Result<Unit>
    fun getLastBackupDate(): Flow<String?>
}
