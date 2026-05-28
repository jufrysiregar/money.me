package com.moneyapp.presentation.screen.transaction

import android.app.DatePickerDialog
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Environment
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.moneyapp.domain.model.TransactionType
import java.io.File
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlin.math.abs

/**
 * Share utility to share captured photo via native Android FileProvider.
 */
fun sharePhoto(context: Context, photoPath: String) {
    try {
        val file = File(photoPath)
        val uri: Uri = FileProvider.getUriForFile(context, "com.moneyapp.fileprovider", file)
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "image/jpeg"
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(Intent.createChooser(intent, "Bagikan Foto Bukti"))
    } catch (e: Exception) {
        Toast.makeText(context, "Gagal membagikan foto", Toast.LENGTH_SHORT).show()
    }
}

/**
 * Download utility to copy internal photos to public Downloads folder.
 */
fun downloadPhotoToGallery(context: Context, photoPath: String) {
    try {
        val srcFile = File(photoPath)
        val downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
        val destFile = File(downloadsDir, "MoneyMe_Bukti_${System.currentTimeMillis()}.jpg")
        
        srcFile.copyTo(destFile, overwrite = true)
        Toast.makeText(context, "Foto disimpan ke: ${destFile.absolutePath}", Toast.LENGTH_LONG).show()
    } catch (e: Exception) {
        Toast.makeText(context, "Gagal menyimpan foto ke Downloads: ${e.message}", Toast.LENGTH_SHORT).show()
    }
}

/**
 * Helper to display DatePickerDialog in Android.
 */
fun showDatePicker(context: Context, initialDate: LocalDate, onDateSelected: (LocalDate) -> Unit) {
    val dialog = DatePickerDialog(
        context,
        { _, year, month, dayOfMonth ->
            onDateSelected(LocalDate.of(year, month + 1, dayOfMonth))
        },
        initialDate.year,
        initialDate.monthValue - 1,
        initialDate.dayOfMonth
    )
    dialog.show()
}

/**
 * TransactionFormScreen: Add / Edit transactions with camera previews, nominals,
 * category chips, and clean tabs. Satisfies T01, T02, T03, T04, Wireframes 2a & 2b.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransactionFormScreen(
    navController: NavController,
    transactionId: Long = -1L,
    viewModel: TransactionViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val formState by viewModel.formState.collectAsState()
    var horizontalDragAmount by remember { mutableStateOf(0f) }

    // Date formatting in Indonesian
    val dateFormatter = remember { DateTimeFormatter.ofPattern("dd MMMM yyyy", Locale("id", "ID")) }

    // Reset form when loading new screen
    LaunchedEffect(transactionId) {
        viewModel.resetForm()
        if (transactionId != -1L) {
            viewModel.loadTransactionForEdit(transactionId)
        }
    }

    // Single-time event listener (Pop back or Show Error)
    LaunchedEffect(Unit) {
        viewModel.uiEvent.collect { event ->
            when (event) {
                is UiEvent.NavigateBack -> {
                    Toast.makeText(context, "Transaksi berhasil disimpan!", Toast.LENGTH_SHORT).show()
                    navController.popBackStack()
                }
                is UiEvent.ShowError -> {
                    Toast.makeText(context, "⚠️ ${event.message}", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    // Camera launching setup using secure cache file URI
    val tempPhotoFile = remember { File(context.cacheDir, "temp_capture.jpg") }
    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture(),
        onResult = { success ->
            if (success && tempPhotoFile.exists()) {
                // Save to Secure /Documents/Money.Me/photos/ path
                val baseDir = File(context.getExternalFilesDir(null) ?: context.filesDir, "Documents/Money.Me")
                val photosDir = File(baseDir, "photos").apply { if (!exists()) mkdirs() }
                val targetFile = File(photosDir, "transaksi_${System.currentTimeMillis()}.jpg")

                tempPhotoFile.copyTo(targetFile, overwrite = true)
                viewModel.onPhotoPathChange(targetFile.absolutePath)
                Toast.makeText(context, "Bukti foto berhasil disimpan!", Toast.LENGTH_SHORT).show()
            }
        }
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    val actionLabel = if (formState.id == 0L) "Tambah" else "Edit"
                    Text(
                        text = if (formState.type == TransactionType.INCOME) "$actionLabel Pemasukan" else "$actionLabel Pengeluaran",
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(imageVector = Icons.Filled.ArrowBack, contentDescription = "Kembali")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
        ) {
            // Segmented Tabs: Pemasukan vs Pengeluaran (T01)
            TabRow(
                selectedTabIndex = if (formState.type == TransactionType.INCOME) 0 else 1,
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = MaterialTheme.colorScheme.primary,
                indicator = { tabPositions ->
                    TabRowDefaults.SecondaryIndicator(
                        modifier = Modifier.tabIndicatorOffset(tabPositions[if (formState.type == TransactionType.INCOME) 0 else 1]),
                        color = if (formState.type == TransactionType.INCOME) Color(0xFF52B788) else Color(0xFFF4A261)
                    )
                }
            ) {
                Tab(
                    selected = formState.type == TransactionType.INCOME,
                    onClick = { viewModel.onTypeChange(TransactionType.INCOME) },
                    text = {
                        Text(
                            text = "PEMASUKAN",
                            fontWeight = FontWeight.Bold,
                            color = if (formState.type == TransactionType.INCOME) Color(0xFF52B788) else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                    }
                )
                Tab(
                    selected = formState.type == TransactionType.EXPENSE,
                    onClick = { viewModel.onTypeChange(TransactionType.EXPENSE) },
                    text = {
                        Text(
                            text = "PENGELUARAN",
                            fontWeight = FontWeight.Bold,
                            color = if (formState.type == TransactionType.EXPENSE) Color(0xFFF4A261) else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                    }
                )
            }

            Column(
                modifier = Modifier
                    .padding(24.dp)
                    .pointerInput(formState.type) {
                        detectHorizontalDragGestures(
                            onDragStart = { horizontalDragAmount = 0f },
                            onHorizontalDrag = { _, dragAmount ->
                                horizontalDragAmount += dragAmount
                            },
                            onDragEnd = {
                                val swipeThreshold = 80f
                                if (abs(horizontalDragAmount) > swipeThreshold) {
                                    val nextType = if (horizontalDragAmount < 0) {
                                        TransactionType.EXPENSE
                                    } else {
                                        TransactionType.INCOME
                                    }
                                    if (nextType != formState.type) {
                                        viewModel.onTypeChange(nextType)
                                    }
                                }
                                horizontalDragAmount = 0f
                            },
                            onDragCancel = { horizontalDragAmount = 0f }
                        )
                    }
            ) {
                // 1. 💰 NOMINAL FIELD
                Text(
                    text = "💰 NOMINAL",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    letterSpacing = 0.5.sp
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = formState.amount,
                    onValueChange = { viewModel.onAmountChange(it) },
                    prefix = { Text("Rp ", fontWeight = FontWeight.Bold) },
                    placeholder = { Text("Masukkan nominal") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    isError = formState.amountError != null,
                    supportingText = {
                        if (formState.amountError != null) {
                            Text(text = "⚠️ ${formState.amountError}", color = Color(0xFFF4A261), fontWeight = FontWeight.Bold)
                        }
                    },
                    shape = RoundedCornerShape(16.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = if (formState.type == TransactionType.INCOME) Color(0xFF52B788) else Color(0xFFF4A261),
                        unfocusedBorderColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f),
                        focusedContainerColor = MaterialTheme.colorScheme.surface,
                        unfocusedContainerColor = MaterialTheme.colorScheme.surface
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(20.dp))

                // 2. 📁 KATEGORI FIELD (Chips Selection)
                Text(
                    text = "📁 KATEGORI",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    letterSpacing = 0.5.sp
                )
                Spacer(modifier = Modifier.height(8.dp))

                // Chip Options Group based on transaction type
                val chipCategories = if (formState.type == TransactionType.INCOME) {
                    listOf("Gaji", "Bonus", "Lainnya")
                } else {
                    listOf("Makanan", "Transport", "Belanja", "Tagihan")
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    chipCategories.forEach { category ->
                        val isSelected = formState.category.equals(category, ignoreCase = true)
                        val chipColor = if (isSelected) {
                            if (formState.type == TransactionType.INCOME) Color(0xFF52B788) else Color(0xFFF4A261)
                        } else {
                            MaterialTheme.colorScheme.surface
                        }

                        val textColor = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                        val borderColor = if (isSelected) Color.Transparent else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f)

                        Box(
                            modifier = Modifier
                                .border(1.dp, borderColor, RoundedCornerShape(12.dp))
                                .background(chipColor, RoundedCornerShape(12.dp))
                                .clickable { viewModel.onCategoryChange(category) }
                                .padding(horizontal = 14.dp, vertical = 8.dp)
                        ) {
                            Text(
                                text = category,
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = textColor
                                )
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Standard Text Field for Custom Category Input
                OutlinedTextField(
                    value = formState.category,
                    onValueChange = { viewModel.onCategoryChange(it) },
                    placeholder = { Text("Ketik kategori khusus") },
                    singleLine = true,
                    isError = formState.categoryError != null,
                    supportingText = {
                        if (formState.categoryError != null) {
                            Text(text = "⚠️ ${formState.categoryError}", color = Color(0xFFF4A261), fontWeight = FontWeight.Bold)
                        }
                    },
                    shape = RoundedCornerShape(16.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = if (formState.type == TransactionType.INCOME) Color(0xFF52B788) else Color(0xFFF4A261),
                        unfocusedBorderColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f),
                        focusedContainerColor = MaterialTheme.colorScheme.surface,
                        unfocusedContainerColor = MaterialTheme.colorScheme.surface
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(20.dp))

                // 3. 📅 TANGGAL FIELD (Native Picker trigger)
                Text(
                    text = "📅 TANGGAL",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    letterSpacing = 0.5.sp
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .border(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f), RoundedCornerShape(16.dp))
                        .background(MaterialTheme.colorScheme.surface)
                        .clickable { showDatePicker(context, formState.date) { viewModel.onDateChange(it) } }
                        .padding(horizontal = 16.dp, vertical = 14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = formState.date.format(dateFormatter),
                        style = MaterialTheme.typography.bodyLarge.copy(
                            color = MaterialTheme.colorScheme.onSurface,
                            fontWeight = FontWeight.Bold
                        )
                    )
                    Icon(
                        imageVector = Icons.Filled.CalendarMonth,
                        contentDescription = "Pilih Tanggal",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                // 4. 📝 CATATAN FIELD
                Text(
                    text = "📝 CATATAN",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    letterSpacing = 0.5.sp
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = formState.note,
                    onValueChange = { viewModel.onNoteChange(it) },
                    placeholder = { Text("cth: Restoran A (opsional)") },
                    shape = RoundedCornerShape(16.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = if (formState.type == TransactionType.INCOME) Color(0xFF52B788) else Color(0xFFF4A261),
                        unfocusedBorderColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f),
                        focusedContainerColor = MaterialTheme.colorScheme.surface,
                        unfocusedContainerColor = MaterialTheme.colorScheme.surface
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(24.dp))

                // 5. 📸 BUKTI FOTO SECTION (T03, T04, Mockup 6)
                Text(
                    text = "📸 BUKTI FOTO",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    letterSpacing = 0.5.sp
                )
                Spacer(modifier = Modifier.height(10.dp))

                val hasPhoto = !formState.photoPath.isNullOrBlank()

                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        if (hasPhoto) {
                            val file = File(formState.photoPath!!)
                            if (file.exists()) {
                                // Picture preview frame (Mockup 6)
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(180.dp)
                                        .clip(RoundedCornerShape(12.dp))
                                        .border(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f), RoundedCornerShape(12.dp))
                                ) {
                                    val bitmap = remember(formState.photoPath) {
                                        android.graphics.BitmapFactory.decodeFile(formState.photoPath)
                                    }
                                    if (bitmap != null) {
                                        Image(
                                            bitmap = bitmap.asImageBitmap(),
                                            contentDescription = "Bukti Preview",
                                            contentScale = ContentScale.Crop,
                                            modifier = Modifier.fillMaxSize()
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.height(14.dp))

                                // Media actions row (Share, Download, Delete) (T04)
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceAround
                                ) {
                                    Button(
                                        onClick = { sharePhoto(context, formState.photoPath!!) },
                                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f), contentColor = MaterialTheme.colorScheme.primary),
                                        shape = RoundedCornerShape(10.dp)
                                    ) {
                                        Icon(imageVector = Icons.Filled.Share, contentDescription = "Share", modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("Share")
                                    }

                                    Button(
                                        onClick = { downloadPhotoToGallery(context, formState.photoPath!!) },
                                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f), contentColor = MaterialTheme.colorScheme.primary),
                                        shape = RoundedCornerShape(10.dp)
                                    ) {
                                        Icon(imageVector = Icons.Filled.Download, contentDescription = "Download", modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("Download")
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(if (hasPhoto) 12.dp else 0.dp))

                        // Camera snap trigger button
                        Button(
                            onClick = {
                                val uri: Uri = FileProvider.getUriForFile(
                                    context,
                                    "com.moneyapp.fileprovider",
                                    tempPhotoFile
                                )
                                cameraLauncher.launch(uri)
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (formState.type == TransactionType.INCOME) Color(0xFF52B788).copy(alpha = 0.12f) else Color(0xFFF4A261).copy(alpha = 0.12f),
                                contentColor = if (formState.type == TransactionType.INCOME) Color(0xFF2D6A4F) else Color(0xFFE07B3A)
                            ),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(imageVector = Icons.Filled.CameraAlt, contentDescription = "Kamera")
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(if (hasPhoto) "Ganti Foto Bukti" else "Ambil Foto Bukti", fontWeight = FontWeight.Bold)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))

                // 6. SAVE BUTTON (Bottom layout anchor)
                Button(
                    onClick = { viewModel.saveTransaction() },
                    enabled = !formState.isLoading,
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (formState.type == TransactionType.INCOME) Color(0xFF52B788) else Color(0xFFF4A261),
                        contentColor = Color.White
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp)
                ) {
                    Text("SIMPAN TRANSAKSI", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
                }
            }
        }
    }
}
