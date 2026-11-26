# Checklist pengujian manual Dompet Deadline

Selesaikan checklist ini sendiri setelah aplikasi berhasil dibuild di MacBook, lalu ambil screenshot untuk dokumentasi GitHub. Semua item di bawah merupakan **skenario pengujian**, bukan klaim bahwa perangkat sudah diuji.

- [ ] Jalankan `mvn clean test` dan pastikan semua unit test lulus.
- [ ] Jalankan `mvn clean package` lalu `java -jar target/dompet-deadline-1.0.0.jar`.
- [ ] Saat database kosong, dashboard menampilkan saldo dan biaya tugas Rp 0.
- [ ] Tambahkan kategori pemasukan dan pengeluaran; kategori baru muncul di form transaksi.
- [ ] Tambahkan pemasukan Rp 500.000 dan pengeluaran Rp 50.000 pada bulan yang sama; saldo menjadi Rp 450.000.
- [ ] Ubah transaksi, lalu hapus transaksi dengan dialog konfirmasi.
- [ ] Buat tugas dengan prioritas tinggi dan deadline besok; ubah dan tandai selesai.
- [ ] Buat pengeluaran Rp 25.000 yang dikaitkan dengan tugas; tabel tugas menampilkan biaya Rp 25.000.
- [ ] Biaya tugas aktif berkurang saat tugas selesai, tanpa menghapus transaksi.
- [ ] Gunakan pencarian dan filter transaksi/tugas serta klik header tabel untuk mengurutkan.
- [ ] Grafik laporan menampilkan pengeluaran untuk bulan yang dipilih; navigasi bulan bekerja.
- [ ] Ekspor CSV, buka di aplikasi spreadsheet, lalu cocokkan kolom/angka transaksi.
- [ ] Buat tugas dengan tenggat dalam 24 jam untuk mencoba notifikasi System Tray/dialog.
- [ ] Pastikan notifikasi tugas yang sama tidak berulang setelah pemeriksaan berikutnya.
- [ ] Tutup dan buka ulang aplikasi; semua data masih ada.
- [ ] Hapus tugas yang punya pengeluaran; transaksinya tetap ada tanpa relasi tugas.
- [ ] Perbesar/perkecil jendela; teks, tombol, dan tabel tetap bisa diakses.
- [ ] Pastikan file SQLite berada di folder pengguna, bukan dalam repositori GitHub.
- [ ] Simpan screenshot dashboard, transaksi, tugas, laporan dan GIF demo untuk README.
- [ ] Publikasikan JAR hasil pengujian melalui GitHub Releases.
