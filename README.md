# FJ Launcher (Minimalist Android Launcher)

Android Launcher minimalis berbasis teks dengan visual **GLMatrix xscreensaver (OpenGL ES 2.0)** dan integrasi waktu sholat offline (Kemenag RI).

## Fitur Utama

### 1. Home Screen
- **Header Widget Dinamis:**
  - Jam Digital (12-jam / 24-jam format), aksi klik/touch dapat dikustomisasi untuk membuka aplikasi tertentu (Default Alarm/Clock, None, atau aplikasi terinstal apa saja).
  - Tanggal Bahasa Indonesia.
  - Waktu Sholat Offline (Subuh, Dzuhur, Ashar, Maghrib, Isya) berdasarkan formula astronomi hisab Kemenag RI beserta hitung mundur waktu sholat berikutnya (contoh: `Ashar 15:18 (-32m)`).
  - Indikator cuaca ringkas.
- **Pinned / Favorite Apps (Berbasis Teks):**
  - Perataan horizontal: **Rata Kiri (Left)**, **Tengah (Center)**, atau **Rata Kanan (Right)**.
  - Perataan vertikal: **Atas (Top)**, **Tengah (Center)**, atau **Bawah (Bottom)** untuk jangkauan satu tangan yang mudah.
  - Ukuran font dapat disesuaikan (slider 14sp s/d 32sp).
  - Opsi teks tebal (*Bold*).
  - Warna teks dapat dikustomisasi.
  - Tap untuk membuka aplikasi, tahan lama (*long-press*) untuk menu opsi cepat.
- **Gestur Layar Utama:**
  - Geser ke atas (*Swipe Up*): Buka App Drawer.
  - Geser ke bawah (*Swipe Down*): Buka panel notifikasi.
  - Geser ke kiri (*Swipe Left*): Buka aplikasi khusus sesuai pengaturan (atau None).
  - Geser ke kanan (*Swipe Right*): Buka aplikasi khusus sesuai pengaturan (atau None).
  - Tahan layar (*Long Press*): Buka Pengaturan (Settings).
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
- [`fj-launcher.apk`](file:///home/jrs-service/Projects/android_apps/fj-launcher/fj-launcher.apk) (Universal APK)
- [`fj-launcher-arm64-v8a.apk`](file:///home/jrs-service/Projects/android_apps/fj-launcher/fj-launcher-arm64-v8a.apk) (ARM 64-bit)
- [`fj-launcher-armeabi-v7a.apk`](file:///home/jrs-service/Projects/android_apps/fj-launcher/fj-launcher-armeabi-v7a.apk) (ARM 32-bit)
- [`fj-launcher-x86_64.apk`](file:///home/jrs-service/Projects/android_apps/fj-launcher/fj-launcher-x86_64.apk) (x86 64-bit)
- [`fj-launcher-x86.apk`](file:///home/jrs-service/Projects/android_apps/fj-launcher/fj-launcher-x86.apk) (x86 32-bit)
- [`fj-launcher-universal.apk`](file:///home/jrs-service/Projects/android_apps/fj-launcher/fj-launcher-universal.apk) (Universal)

## Cara Build dari Terminal
```bash
# Set path JDK 17
$env:JAVA_HOME = "D:\tools\jdk-17"

# Build debug APK
.\gradlew assembleDebug
```
