# FJ Launcher (Minimalist Android Launcher)

Android Launcher minimalis berbasis teks dengan visual **GLMatrix xscreensaver (OpenGL ES 2.0)** dan integrasi waktu sholat offline (Kemenag RI).

## Fitur Utama

### 1. Home Screen
- **Header Widget Dinamis:**
  - Jam Digital (12-jam / 24-jam format), tap untuk membuka aplikasi Alarm/Clock default.
  - Tanggal Bahasa Indonesia.
  - Waktu Sholat Offline (Subuh, Dzuhur, Ashar, Maghrib, Isya) berdasarkan formula astronomi hisab Kemenag RI beserta hitung mundur waktu sholat berikutnya (contoh: `Ashar 15:18 (-32m)`).
  - Indikator cuaca ringkas.
- **Pinned / Favorite Apps (Berbasis Teks):**
  - Perataan teks fleksibel: **Rata Kiri (Left)**, **Tengah (Center)**, atau **Rata Kanan (Right)**.
  - Ukuran font dapat disesuaikan (slider 14sp s/d 32sp).
  - Opsi teks tebal (*Bold*).
  - Warna teks dapat dikustomisasi.
  - Tap untuk membuka aplikasi, tahan lama (*long-press*) untuk menu opsi cepat.
- **Background GLMatrix (xscreensaver Jamie Zawinski):**
  - Dirender menggunakan OpenGL ES 2.0 dengan tekstur atlas karakter Katakana, numerik, dan simbol Matrix.
  - Kepala tetesan (*head*) bersinar putih/terang, ekor hijau bertransisi memudar (*trail fade*).
  - Efek mutasi karakter acak secara dinamis saat kode jatuh (*glyph flipping*).
  - Efek 3D Camera Sway / drift sudut pandang khas screensaver asli.
  - Pengaturan tema warna: *Classic Matrix Green*, *Cyberpunk Cyan*, *Golden Amber*, *Blood Red*, *Hacker White*.
  - Slider kecepatan jatuh, kepadatan kolom (*density*), dan dimmer latar belakang.
  - **Hemat Baterai:** Render loop otomatis di-pause saat drawer terbuka, settings aktif, atau layar mati.

### 2. App Drawer / Daftar Aplikasi
- Tampilan list teks minimalis dan cepat.
- Kolom pencarian instan (*instant live filter*).
- **Sistem Folder:** Buat folder, masukkan aplikasi ke folder, dan buka isi folder.
- **Folder First:** Menampilkan folder di posisi paling atas sebelum daftar aplikasi.
- **Pengurutan:** A ke Z atau Z ke A.
- **Menu Aksi Aplikasi (Long Press):**
  - ✏️ **Rename / Alias:** Berikan nama custom untuk aplikasi.
  - 📌 **Pin / Unpin to Home:** Sematkan atau lepas dari Home.
  - 📁 **Atur Folder:** Masukkan ke folder atau keluarkan.
  - 👁️ **Sembunyikan Aplikasi (Hide):** Sembunyikan dari drawer.
  - ℹ️ **Info Aplikasi:** Buka detail izin & penyimpanan aplikasi di sistem Android.
  - 🗑️ **Uninstall:** Hapus aplikasi dari perangkat.

### 3. Pengaturan (Settings)
- Pengaturan Home Screen (widget on/off, font, alignment).
- Pengaturan GLMatrix (warna tema, kecepatan, density, 3D sway, dimmer).
- Pengaturan App Drawer (folder first, pengurutan, kelola aplikasi tersembunyi).

## Lokasi File APK
File APK siap instal tersedia langsung di root folder proyek:
- [`fj-launcher.apk`](file:///g:/My%20Drive/priv_web_apps/fj-launcher/fj-launcher.apk) (Root folder)
- [`app/build/outputs/apk/debug/app-debug.apk`](file:///g:/My%20Drive/priv_web_apps/fj-launcher/app/build/outputs/apk/debug/app-debug.apk)

## Cara Build dari Terminal
```bash
# Set path JDK 17
$env:JAVA_HOME = "D:\tools\jdk-17"

# Build debug APK
.\gradlew assembleDebug
```
