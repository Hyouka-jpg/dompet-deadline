package id.ac.usk.dompetdeadline.repository;

import id.ac.usk.dompetdeadline.model.UpcomingTask;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import id.ac.usk.dompetdeadline.service.DateFormats;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;

public final class DashboardRepository {
    public long sumMonthly(YearMonth month, String transactionType) throws SQLException {
        String sql = """
                SELECT COALESCE(SUM(t.jumlah), 0)
                FROM transaksi t
                JOIN kategori k ON k.id = t.kategori_id
                WHERE k.jenis = ? AND t.tanggal >= ? AND t.tanggal < ?
                """;
        try (Connection connection = Database.getConnection();
             PreparedStatement query = connection.prepareStatement(sql)) {
            query.setString(1, transactionType);
            query.setString(2, month.atDay(1).toString());
            query.setString(3, month.plusMonths(1).atDay(1).toString());
            try (ResultSet result = query.executeQuery()) {
                return result.next() ? result.getLong(1) : 0L;
            }
        }
    }

    public long sumActiveTaskExpenses() throws SQLException {
        String sql = """
                SELECT COALESCE(SUM(tr.jumlah), 0)
                FROM transaksi tr
                JOIN tugas tu ON tu.id = tr.tugas_id
                JOIN kategori ka ON ka.id = tr.kategori_id
                WHERE tu.selesai = 0 AND ka.jenis = 'PENGELUARAN'
                """;
        try (Connection connection = Database.getConnection();
             PreparedStatement query = connection.prepareStatement(sql);
             ResultSet result = query.executeQuery()) {
            return result.next() ? result.getLong(1) : 0L;
        }
    }

    public List<UpcomingTask> findThreeUnfinishedTasks() throws SQLException {
        String sql = """
                SELECT id, judul, mata_kuliah, tenggat, prioritas
                FROM tugas
                WHERE selesai = 0
                ORDER BY tenggat ASC, id ASC
                LIMIT 3
                """;
        List<UpcomingTask> tasks = new ArrayList<>();
        try (Connection connection = Database.getConnection();
             PreparedStatement query = connection.prepareStatement(sql);
             ResultSet result = query.executeQuery()) {
            while (result.next()) {
                tasks.add(new UpcomingTask(
                        result.getLong("id"), result.getString("judul"),
                        result.getString("mata_kuliah"),
                        DateFormats.fromDatabase(result.getString("tenggat")).toLocalDate(),
                        result.getString("prioritas")));
            }
        }
        return tasks;
    }
}
