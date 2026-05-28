package com.moneyapp.presentation.screen.settings

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.moneyapp.data.local.datastore.ThemePreferences
import com.moneyapp.data.local.db.AppDatabase
import com.moneyapp.domain.model.AppResult
import com.moneyapp.domain.model.User
import com.moneyapp.domain.repository.UserRepository
import com.moneyapp.domain.usecase.user.SaveUserUseCase
import com.moneyapp.presentation.theme.ThemeMode
import com.moneyapp.presentation.util.BackupManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.io.File
import java.io.OutputStream
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import javax.inject.Inject

sealed class SettingsUiEvent {
    object Success : SettingsUiEvent()
    data class BackupSuccess(val filePath: String) : SettingsUiEvent()
    data class Error(val message: String) : SettingsUiEvent()
}

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val userRepository: UserRepository,
    private val saveUserUseCase: SaveUserUseCase,
    private val themePreferences: ThemePreferences,
    private val db: AppDatabase
) : ViewModel() {

    val user: StateFlow<User?> = userRepository.getUser()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = null
        )

    val themeMode: StateFlow<ThemeMode> = themePreferences.themeMode
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = ThemeMode.SYSTEM
        )

    private val _uiEvent = Channel<SettingsUiEvent>(Channel.BUFFERED)
    val uiEvent = _uiEvent.receiveAsFlow()

    fun updateProfileName(name: String) {
        viewModelScope.launch {
            if (name.isBlank()) {
                _uiEvent.send(SettingsUiEvent.Error("Nama tidak boleh kosong"))
                return@launch
            }
            when (val result = saveUserUseCase(User(fullName = name.trim()))) {
                is AppResult.Success -> _uiEvent.send(SettingsUiEvent.Success)
                is AppResult.Error -> _uiEvent.send(SettingsUiEvent.Error(result.message))
            }
        }
    }

    fun setThemeMode(mode: ThemeMode) {
        viewModelScope.launch {
            themePreferences.setThemeMode(mode)
        }
    }

    fun backupDatabase(context: Context) {
        viewModelScope.launch {
            try {
                val fileName = "money_backup_${LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"))}.zip"
                val file = BackupManager.performBackup(context, db, fileName)
                _uiEvent.send(SettingsUiEvent.BackupSuccess(file.absolutePath))
            } catch (e: Exception) {
                _uiEvent.send(SettingsUiEvent.Error("Gagal mencadangkan data"))
            }
        }
    }

    fun backupDatabaseToStream(context: Context, fileName: String, outputStream: OutputStream) {
        viewModelScope.launch {
            try {
                outputStream.use { output ->
                    BackupManager.performBackupToStream(context, db, fileName, output)
                }
                _uiEvent.send(SettingsUiEvent.BackupSuccess(fileName))
            } catch (e: Exception) {
                _uiEvent.send(SettingsUiEvent.Error("Gagal mencadangkan data"))
            }
        }
    }

    fun restoreDatabase(context: Context, zipFile: File) {
        viewModelScope.launch {
            val success = BackupManager.performRestore(context, db, zipFile)
            if (success) {
                _uiEvent.send(SettingsUiEvent.Success)
            } else {
                _uiEvent.send(SettingsUiEvent.Error("Gagal memulihkan cadangan. Pastikan file valid!"))
            }
        }
    }

}
