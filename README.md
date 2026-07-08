# Money.me — Personal Finance & Stock Portfolio Tracker

**Money.me** adalah aplikasi manajemen keuangan pribadi (*personal finance*) berbasis Android yang dirancang dengan antarmuka modern, intuitif, dan responsif. Aplikasi ini membantu pengguna melacak aliran keuangan harian mulai dari pemasukan, pengeluaran, tabungan target, hingga simulasi portofolio investasi saham secara realistis.

---

## 🎯 Tujuan Aplikasi

Tujuan utama dari **Money.me** adalah memberdayakan pengguna agar memiliki kendali penuh atas kesehatan finansial mereka melalui pencatatan transaksi yang disiplin, visualisasi tabungan berbasis target, dan pengelolaan portofolio investasi saham yang realistis menggunakan perhitungan profit/loss dunia nyata.

---

## 📱 Rincian Fitur dari Setiap Seksi (Section)

### 1. Dashboard (Halaman Utama)
*   **Greeting Section**: Menyapa pengguna secara personal dengan nama lengkap mereka.
*   **Alert CTA Banner**: Banner ajakan cepat reaktif untuk mencatat transaksi harian.
*   **Grid Cards Aset Finansial**: Menampilkan ringkasan keuangan dalam susunan grid terstruktur:
    1.  **PEMASUKAN**: Total dana masuk.
    2.  **PENGELUARAN**: Total dana keluar.
    3.  **TABUNGAN**: Total tabungan yang telah terkumpul.
    4.  **INVESTASI**: Total modal investasi aktif beserta status *unrealized profit/loss* berjalan di bawahnya (misal: `+Rp 500.000` dengan warna hijau jika untung, merah jika rugi).
*   **Monthly Overview**: Visualisasi ringkasan aktivitas keuangan bersih bulanan.

### 2. Pemasukan & Pengeluaran (Transaksi Harian)
*   Form pencatatan transaksi dengan kategori terarah, nominal, tanggal, catatan opsional, dan lampiran foto bukti.
*   Daftar transaksi harian yang diurutkan berdasarkan tanggal terbaru.

### 3. Tabungan (Saving Goals)
*   Membuat target tabungan khusus (contoh: "Beli Laptop Baru", "Dana Darurat").
*   Menentukan jumlah target uang yang ingin dicapai dan tenggat waktu.
*   Visualisasi perkembangan (*progress bar*) tabungan secara interaktif.
*   Kemudahan untuk menyetor/menambah tabungan kapan saja secara langsung.

### 4. Investasi Saham (Stock Market Logic)
Fitur investasi yang dirancang menyerupai mekanisme pasar saham sebenarnya:
*   **Average Price (AVG)**: Harga beli rata-rata per lembar saham.
*   **Harga Saham Saat Ini**: Estimasi nilai pasar saham berjalan (*market price*).
*   **Total Uang Investasi**: Total modal (*capital*) yang telah ditempatkan pada saham tersebut.
*   **Kalkulasi Profit/Loss Realistis**:
    *   $\text{Jumlah Lembar} = \frac{\text{Total Uang Investasi}}{\text{Average Price}}$
    *   $\text{Nilai Sekarang} = \text{Jumlah Lembar} \times \text{Harga Saat Ini}$
    *   $\text{Profit/Loss} = \text{Nilai Sekarang} - \text{Total Uang Investasi}$
    *   *Pill* persentase profit berwarna hijau untuk keuntungan (+%) dan merah untuk kerugian (-%).
*   **Tombol Jual (Sell)**: Membuka modal konfirmasi dengan rincian profit/loss sebelum menjual. Setelah dijual, status saham ditandai sebagai terjual (keluar dari daftar aktif) dan **transaksi Pemasukan (INCOME) senilai hasil penjualan akan dibuat secara otomatis** pada riwayat keuangan Anda.

---

## 🛠️ Bahasa Pemrograman & Tech Stack

Aplikasi Android native ini dibangun menggunakan teknologi modern berikut:

1.  **Kotlin**: Bahasa pemrograman utama yang efisien dan aman.
2.  **Jetpack Compose**: Framework deklaratif modern untuk merancang User Interface (UI) yang kaya estetika dan responsif.
3.  **Room Database (SQLite)**: Library persistensi data lokal Android dengan migrasi database kustom (versi 3) untuk menyimpan profil pengguna, transaksi, tabungan, dan investasi.
4.  **Dagger Hilt**: Library Dependency Injection (DI) untuk mengelola siklus hidup komponen dan mempermudah unit testing.
5.  **Kotlin Coroutines & Flow**: Mengelola operasi *background* asinkron dan aliran data reaktif secara *real-time*.
6.  **Jetpack Navigation**: Navigasi antar-screen yang aman secara tipe (*type-safe navigation*).
7.  **GitHub Actions**: Integrasi CI/CD untuk otomatisasi kompilasi kode dan ekspor APK hasil *build* (`MoneyMe-APK`).

---

## 🚀 Cara Menjalankan Project Secara Lokal

1.  Clone repository ini ke komputer lokal Anda:
    ```bash
    git clone https://github.com/jufrysiregar/money.me.git
    ```
2.  Buka project menggunakan **Android Studio** (Koala atau versi yang lebih baru).
3.  Pastikan JDK terkonfigurasi pada **Java 17**.
4.  Jalankan aplikasi di Emulator atau Perangkat Android fisik dengan menekan tombol **Run**.
5.  Untuk membuat APK debug secara lokal melalui terminal (Windows PowerShell):
    ```powershell
    ./gradlew assembleDebug
    ```
