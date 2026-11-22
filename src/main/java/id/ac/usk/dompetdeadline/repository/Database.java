package id.ac.usk.dompetdeadline.repository;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public final class Database {
    private static final Path DATABASE_DIRECTORY = Path.of(
            System.getProperty("user.home"), ".dompet-deadline");
    private static final Path DATABASE_FILE = DATABASE_DIRECTORY.resolve("dompet-deadline.db");

    private Database() { }

    public static Path getDatabaseFile() {
        return DATABASE_FILE;
    }

    public static Connection getConnection() throws SQLException {
        Connection connection = DriverManager.getConnection("jdbc:sqlite:" + DATABASE_FILE);
        try (Statement statement = connection.createStatement()) {
            statement.execute("PRAGMA foreign_keys = ON");
        }
        try (Statement statement = connection.createStatement()) {
            statement.execute("PRAGMA busy_timeout = 5000");
        }
        return connection;
    }

    public static void initialize() throws SQLException, IOException {
        Files.createDirectories(DATABASE_DIRECTORY);
        try (Connection connection = getConnection();
             Statement statement = connection.createStatement()) {
            statement.executeUpdate("""
                    CREATE TABLE IF NOT EXISTS kategori (
                        id INTEGER PRIMARY KEY AUTOINCREMENT,
                        nama TEXT NOT NULL,
                        jenis TEXT NOT NULL CHECK(jenis IN ('PEMASUKAN', 'PENGELUARAN')),
                        UNIQUE(nama, jenis)
                    )
                    """);
            statement.executeUpdate("""
                    CREATE TABLE IF NOT EXISTS tugas (
                        id INTEGER PRIMARY KEY AUTOINCREMENT,
                        judul TEXT NOT NULL,
                        mata_kuliah TEXT NOT NULL,
                        tenggat TEXT NOT NULL,
                        prioritas TEXT NOT NULL CHECK(prioritas IN ('TINGGI', 'SEDANG', 'RENDAH')),
                        selesai INTEGER NOT NULL DEFAULT 0 CHECK(selesai IN (0, 1))
                    )
                    """);
            statement.executeUpdate("""
                    CREATE TABLE IF NOT EXISTS transaksi (
                        id INTEGER PRIMARY KEY AUTOINCREMENT,
                        tanggal TEXT NOT NULL,
                        jumlah INTEGER NOT NULL CHECK(jumlah > 0),
                        kategori_id INTEGER NOT NULL,
                        tugas_id INTEGER,
                        catatan TEXT NOT NULL DEFAULT '',
                        FOREIGN KEY(kategori_id) REFERENCES kategori(id) ON DELETE RESTRICT,
                        FOREIGN KEY(tugas_id) REFERENCES tugas(id) ON DELETE SET NULL
                    )
                    """);
            statement.executeUpdate("CREATE INDEX IF NOT EXISTS idx_transaksi_tanggal ON transaksi(tanggal)");
            statement.executeUpdate("CREATE INDEX IF NOT EXISTS idx_transaksi_tugas ON transaksi(tugas_id)");
            statement.executeUpdate("CREATE INDEX IF NOT EXISTS idx_tugas_tenggat ON tugas(tenggat)");

            statement.executeUpdate("""
                    CREATE TABLE IF NOT EXISTS pengingat (
                        tugas_id INTEGER PRIMARY KEY,
                        tenggat TEXT NOT NULL,
                        terkirim_pada TEXT NOT NULL,
                        FOREIGN KEY(tugas_id) REFERENCES tugas(id) ON DELETE CASCADE
                    )
                    """);
            // Adaptasi database awal yang menyimpan tenggat hanya sebagai tanggal.
            statement.executeUpdate("UPDATE tugas SET tenggat = tenggat || 'T23:59:00' WHERE length(tenggat) = 10");

            // Kategori awal dibuat sekali saja; kategori yang dihapus pengguna
            // tidak akan muncul kembali setiap aplikasi dibuka.
            statement.executeUpdate("""
                    CREATE TABLE IF NOT EXISTS app_meta (
                        key TEXT PRIMARY KEY,
                        value TEXT NOT NULL
                    )
                    """);
            try (PreparedStatement firstRun = connection.prepareStatement(
                    "INSERT OR IGNORE INTO app_meta (key,value) VALUES ('categories_seeded','1')")) {
                if (firstRun.executeUpdate() == 1) {
                    insertCategory(connection, "Uang Saku", "PEMASUKAN");
                    insertCategory(connection, "Beasiswa", "PEMASUKAN");
                    insertCategory(connection, "Lainnya", "PEMASUKAN");
                    insertCategory(connection, "Makan & Minum", "PENGELUARAN");
                    insertCategory(connection, "Transportasi", "PENGELUARAN");
                    insertCategory(connection, "Fotokopi & Cetak", "PENGELUARAN");
                    insertCategory(connection, "Perlengkapan Kuliah", "PENGELUARAN");
                    insertCategory(connection, "Lainnya", "PENGELUARAN");
                }
            }
        }
    }

    private static void insertCategory(Connection connection, String name, String type)
            throws SQLException {
        try (PreparedStatement query = connection.prepareStatement(
                "INSERT OR IGNORE INTO kategori (nama, jenis) VALUES (?, ?)")) {
            query.setString(1, name);
            query.setString(2, type);
            query.executeUpdate();
        }
    }
}
