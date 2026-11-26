# Panduan commit bertahap — Dompet Deadline

**File sumber sudah lengkap di paket ini, tetapi tidak perlu sekaligus `git add .`.**

Jika ingin commit benar-benar mencerminkan proses kerja, lakukan per tahap setelah kamu membaca kode, menjalankan fitur, dan memperbaiki bila diperlukan. *Jangan membuat 10 commit palsu secara instan hanya untuk mengejar angka.* Tugas meminta minimal 10 commit **tersebar selama pengerjaan**.

> File yang belum di-commit dapat tetap berada di folder lokal dan dibuka di VS Code. Jika satu commit belum dapat dibuild mandiri karena dependensi antarkelas, lakukan commit integrasi setelah fitur terkait terhubung. Di GitHub, hasil akhir harus bisa dibuild.

## Pertama: siapkan repository

1. Buat repository **Public** bernama `dompet-deadline` di GitHub, jangan centang README di GitHub.
2. Buka folder proyek di terminal VS Code.
3. Gunakan perintah:

```bash
git init
git branch -M main
git remote add origin https://github.com/USERNAME/dompet-deadline.git
```

Ganti `USERNAME` dengan username GitHub kamu.

## Rencana commit (minimal 10 tahap)

Jalankan tiap kelompok setelah memahami dan menguji bagiannya. Berikut cara memilih file per fitur menggunakan `git add`:

**Commit 1 — Konfigurasi**

```bash
git add pom.xml .gitignore LICENSE
git commit -m "chore: setup Maven and repository configuration"
```

**Commit 2 — Model**

```bash
git add src/main/java/id/ac/usk/dompetdeadline/model/
git commit -m "feat: define domain models for tasks and finances"
```

**Commit 3 — Database**

```bash
git add src/main/java/id/ac/usk/dompetdeadline/repository/Database.java src/main/java/id/ac/usk/dompetdeadline/repository/CategoryRepository.java
git commit -m "feat: initialize SQLite schema and category access"
```

**Commit 4 — Repository transaksi dan tugas**

```bash
git add src/main/java/id/ac/usk/dompetdeadline/repository/TaskRepository.java src/main/java/id/ac/usk/dompetdeadline/repository/TransactionRepository.java src/main/java/id/ac/usk/dompetdeadline/repository/DashboardRepository.java
git commit -m "feat: implement task, transaction, and dashboard repositories"
```

**Commit 5 — Logika bisnis**

```bash
git add src/main/java/id/ac/usk/dompetdeadline/service/
git commit -m "feat: implement business rules, validation, and CSV export"
```

**Commit 6 — Dasar tampilan dan dashboard**

```bash
git add src/main/java/id/ac/usk/dompetdeadline/Main.java src/main/java/id/ac/usk/dompetdeadline/view/Theme.java src/main/java/id/ac/usk/dompetdeadline/view/RoundedPanel.java src/main/java/id/ac/usk/dompetdeadline/view/ViewUtils.java src/main/java/id/ac/usk/dompetdeadline/view/DashboardPanel.java
git commit -m "feat: create Swing design system and dashboard"
```

**Commit 7 — Transaksi dan kategori**

```bash
git add src/main/java/id/ac/usk/dompetdeadline/view/TransactionDialog.java src/main/java/id/ac/usk/dompetdeadline/view/TransactionPanel.java src/main/java/id/ac/usk/dompetdeadline/view/CategoryDialog.java
git commit -m "feat: add transactions CRUD, category management, and filters"
```

**Commit 8 — Pengelolaan tugas**

```bash
git add src/main/java/id/ac/usk/dompetdeadline/view/TaskPanel.java src/main/java/id/ac/usk/dompetdeadline/view/TaskDialog.java
git commit -m "feat: add task CRUD, priorities, completion, and cost tracking"
```

**Commit 9 — Grafik dan pengingat**

```bash
git add src/main/java/id/ac/usk/dompetdeadline/view/SpendingChart.java src/main/java/id/ac/usk/dompetdeadline/view/ReportPanel.java src/main/java/id/ac/usk/dompetdeadline/view/DeadlineNotifier.java src/main/java/id/ac/usk/dompetdeadline/repository/ReminderRepository.java
git commit -m "feat: add custom spending chart, CSV UI, and deadline alerts"
```

**Commit 10 — Integrasi, pengujian, dokumentasi**

```bash
git add src/main/java/id/ac/usk/dompetdeadline/view/MainFrame.java src/test/ README.md PANDUAN_COMMIT.md CHECKLIST_PENGUJIAN.md
git commit -m "test: integrate screens, add JUnit tests, and document usage"
```

Saat siap mengunggah:

```bash
git status
git push -u origin main
```

**Penting:** Contoh di atas adalah pengelompokan file, bukan histori pengerjaan yang sudah dilakukan. Sebelum setiap commit, jalankan `git diff --cached` untuk melihat persis yang akan dikirim. Setelah integrasi akhir, jalankan `mvn clean test` dan `mvn clean package`.

## Rilis

Unggah JAR hasil `mvn clean package` ke GitHub Releases. Link repository dan Releases menjadi bahan pengumpulan.
