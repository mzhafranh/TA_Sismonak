# Welcome to Sismonak (Sistem Monitoring Anak)!
<img src="https://raw.githubusercontent.com/mzhafranh/TA_Sismonak/master/graphics/icons/sismonak_icon.png" alt="Sismonak Icon"/>

**Sismonak** adalah aplikasi kontrol orang tua android gratis dan sumber terbuka yang memungkinkan orang tua mengendalikan aktivitas anak-anak mereka. Sismonak adalah pengembangan dari proyek yang sudah ada yang dikenal sebagai [KidSafe](https://github.com/xMansour/KidSafe) oleh Mahmoud Mansour dan Khalid Samir.

##Panduan setup
Video 
https://youtu.be/tXr5AVMVrxo

Teks

Langkah-1angkah:

Bagian 1 - Mengunduh project Sismonak
1. Klik tombol hijau pada halaman ini lalu klik download as zip
2. Ekstrak file yang diunduh pada lokasi yang diinginkan, misalnya di desktop "C:\Users\user\Desktop\" hingga terdapat folder baru "C:\Users\user\Desktop\TA_Sismonak-main"

Bagian 2 - Setup Android Studio
1. Download [Android Studio](https://developer.android.com/studio)
2. Instal android studio
3. Setelah instalasi selesai pilih "Open Project"
4. Buka folder Sismonak bagian Application, misalnya "C:\Users\user\Desktop\TA_Sismonak-main\Application"
5. Tunggu hingga proses loading project selesai

Bagian 3 - Setup Google Firebase
1. Kunjungi halaman [Firebase](https://console.firebase.google.com/)
2. Pilih "Create a project"
3. Masukkan nama projek, misalnya "sismonak-mzhafranh", lalu klik "continue"
4. Klik continue
5. Pilih akun, misalnya "Default Account for Firebase", lalu klik "Create project"
6. Tunggu hingga project selesai dibuat
7. Klik tombol dengan ikon Android
8. Masukkan nama package android baru yang berbeda dengan "com.mzhtech.sismonakdev", misalnya "com.mzhafranh.sismonakdev". Nama ini akan digunakan kembali nanti.
9. Masukkan nama app, misalnya "Sismonak"
10. Klik "Register"
11. Klik Next, lalu Next, dan "Continue to console"
12. Klik Build -> Authentication -> Get started -> Email/Password -> Enable Email/Password -> Save
13. Klik Build -> Realtime Database -> Create Database -> Pilih lokasi database (misalnya "Singapore (asia-southeast1)") -> Next -> Start in "test mode" -> Enable
14. Klik Build -> Storage -> Get started -> "Start in production mode" -> Next -> Pilih lokasi database (misalnya "asia-southeast1") -> Done
15. Klik Rules pada Storage
16. Ubah Rules pada Storage menjadi "allow read, write: if true;" lalu klik "Publish"
17. Klik icon pengaturan pada atas kiri lalu pilih "Project settings"
18. Klik "download google-servies.json" lalu simpan pada folder projek bagian app, misalnya "C:\Users\user\Desktop\TA_Sismonak-main\Application\app"
19. Buka kembali Android Studio
20. Tekan "Ctrl + Shift + R" lalu klik "In Project"
21. Masukkan nama package lama "com.mzhtech.sismonakdev" pada kolom atas dan nama package baru, misalnya "com.mzhafranh.sismonakdev" pada kolom di bawahnya
22. Klik "Replace All" lalu "Replace"
23. Klik icon "Sync Project with Gradle Files" di atas kanan
24. Tunggu hingga proses "sync" selesai
25. Ubah nama pada folder di projek sismonak sesuai nama package baru, misalnya pada "C:\Users\user\Desktop\TA_Sismonak-main\Application\app\src\main\java\com" folder mzhtech diubah namanya menjadi mzhafranh
26. Pastikan nama folder sesuai dengan nama package yang baru
27. Klik icon garis pada ujung kiri atas Android Studio untuk membuka "Main Menu"
28. Klik Build -> Build App Bundle(s) / APK(s) -> Build APK(s)
29. Tunggu hingga selesai lalu klik locate
30. File app-debug.apk dapat diinstall ke smartphone Anda

Bagian 4 - Setup Rules
1. Lakukan instalasi APK pada smartphone orang tua dan anak
2. Pastikan kedua smartphone berhasil terhubung dan aplikasi berjalan lancar
3. Kunjungi halaman [Firebase](https://console.firebase.google.com/)
4. Pilih projek Sismonak Anda
5. Klik Realtime Database -> Rules
6. Masukkan rules di bawah untuk memastikan tidak ada orang luar yang dapat menggunakan cloud Anda.

{
  "rules": {
    ".read": "auth != null && (auth.token.email == 'abc@gmail.com' || auth.token.email == 'def@gmail.com')",
    ".write": "auth != null && (auth.token.email == 'abc@gmail.com' || auth.token.email == 'def@gmail.com')"
  }
}

7. Ubah "abc@gmail dan def@gmail" dengan email orang tua dan anak yang telah dipakai pada aplikasi Sismonak lalu klik "Publish"
8. Klik Storage -> Rules
9. Masukkan rules di bawah untuk memastikan tidak ada orang luar yang dapat menggunakan cloud Anda.

service firebase.storage {
  match /b/{bucket}/o {
    match /{allPaths=**} {
      allow read, write: if request.auth != null && (request.auth.token.email == 'abc@gmail.com' || request.auth.token.email == 'def@gmail.com');
    }
  }
}

10. Ubah "abc@gmail dan def@gmail" dengan email orang tua dan anak yang telah dipakai pada aplikasi Sismonak lalu klik "Publish"

Bagian Ekstra - Menambahkan pengguna baru pada sistem anda
1. Kunjungi halaman [Firebase](https://console.firebase.google.com/)
2. Pilih projek Sismonak Anda
3. Klik Realtime Database -> Rules
4. Masukkan rules di bawah untuk memperbolehkan orang lain menggunakan cloud Anda lalu klik "Publish"

{
  "rules": {
    ".read": true,
    ".write": true
  }
}

5. Klik Storage -> Rules
6. Masukkan rules di bawah untuk memperbolehkan orang lain menggunakan cloud Anda lalu klik "Publish"

service firebase.storage {
  match /b/{bucket}/o {
    match /{allPaths=**} {
      allow read, write: if true;
    }
  }
}

7. Lakukan pendaftaran pengguna baru pada Aplikasi Sismonak misalnya dengan email "ghi@gmail.com"
8. Klik Realtime Database -> Rules
9. Masukkan rules sebelumnya, kemudian untuk menambahkan pengguna baru tambahkan "|| auth.token.email == 'email.pengguna.baru@email.com'", sebagai contoh email "ghi@gmail.com"

{
  "rules": {
    ".read": "auth != null && (auth.token.email == 'abc@gmail.com' || auth.token.email == 'def@gmail.com' || auth.token.email == 'ghi@gmail.com')",
    ".write": "auth != null && (auth.token.email == 'abc@gmail.com' || auth.token.email == 'def@gmail.com' || auth.token.email == 'ghi@gmail.com')"
  }
}

10. Klik "Publish"
11. Klik Storage -> Rules
12. Masukkan rules sebelumnya, kemudian untuk menambahkan pengguna baru tambahkan "|| request.auth.token.email == 'email.pengguna.baru@email.com'", sebagai contoh email "ghi@gmail.com"

service firebase.storage {
  match /b/{bucket}/o {
    match /{allPaths=**} {
      allow read, write: if request.auth != null && (request.auth.token.email == 'abc@gmail.com' || request.auth.token.email == 'def@gmail.com' || || request.auth.token.email == 'ghi@gmail.com');
    }
  }
}

13. Klik "Publish"