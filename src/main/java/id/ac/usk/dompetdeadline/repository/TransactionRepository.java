package id.ac.usk.dompetdeadline.repository;

import id.ac.usk.dompetdeadline.model.CategorySpending;
import id.ac.usk.dompetdeadline.model.FinanceTransaction;
import java.sql.*;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;

public final class TransactionRepository {
    public List<FinanceTransaction> findAll() throws SQLException {
        String sql = """
                SELECT tr.id, tr.tanggal, tr.jumlah, tr.kategori_id, k.nama AS kategori,
                       k.jenis, tr.tugas_id, COALESCE(t.judul, '') AS tugas, tr.catatan
                  FROM transaksi tr
                  JOIN kategori k ON k.id = tr.kategori_id
                  LEFT JOIN tugas t ON t.id = tr.tugas_id
                 ORDER BY tr.tanggal DESC, tr.id DESC
                """;
        List<FinanceTransaction> list = new ArrayList<>();
        try (Connection db = Database.getConnection(); PreparedStatement ps = db.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                long linkedTask = rs.getLong("tugas_id");
                Long taskId = rs.wasNull() ? null : linkedTask;
                list.add(new FinanceTransaction(rs.getLong("id"), LocalDate.parse(rs.getString("tanggal")),
                        rs.getLong("jumlah"), rs.getLong("kategori_id"), rs.getString("kategori"),
                        rs.getString("jenis"), taskId, rs.getString("tugas"), rs.getString("catatan")));
            }
        }
        return list;
    }
    public void save(long id, LocalDate date, long amount, long categoryId, Long taskId, String note)
            throws SQLException {
        boolean updating = id > 0;
        String sql = updating ? "UPDATE transaksi SET tanggal=?,jumlah=?,kategori_id=?,tugas_id=?,catatan=? WHERE id=?"
                : "INSERT INTO transaksi (tanggal,jumlah,kategori_id,tugas_id,catatan) VALUES (?,?,?,?,?)";
        try (Connection db = Database.getConnection(); PreparedStatement ps = db.prepareStatement(sql)) {
            ps.setString(1,date.toString()); ps.setLong(2,amount); ps.setLong(3,categoryId);
            if (taskId == null) ps.setNull(4,Types.INTEGER); else ps.setLong(4,taskId);
            ps.setString(5,note);
            if (updating) ps.setLong(6,id);
            ps.executeUpdate();
        }
    }
    public void delete(long id) throws SQLException {
        try (Connection db = Database.getConnection(); PreparedStatement ps = db.prepareStatement("DELETE FROM transaksi WHERE id=?")) {
            ps.setLong(1,id); ps.executeUpdate();
        }
    }
    public List<CategorySpending> spendingByCategory(YearMonth month) throws SQLException {
        String sql = """
                SELECT k.nama, SUM(tr.jumlah) AS total
                  FROM transaksi tr JOIN kategori k ON k.id = tr.kategori_id
                 WHERE k.jenis = 'PENGELUARAN' AND tr.tanggal >= ? AND tr.tanggal < ?
                 GROUP BY k.id, k.nama ORDER BY total DESC, k.nama
                """;
        List<CategorySpending> list = new ArrayList<>();
        try (Connection db = Database.getConnection(); PreparedStatement ps = db.prepareStatement(sql)) {
            ps.setString(1,month.atDay(1).toString());
            ps.setString(2,month.plusMonths(1).atDay(1).toString());
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(new CategorySpending(rs.getString(1),rs.getLong(2)));
            }
        }
        return list;
    }
}
