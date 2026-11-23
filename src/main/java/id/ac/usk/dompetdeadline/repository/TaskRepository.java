package id.ac.usk.dompetdeadline.repository;

import id.ac.usk.dompetdeadline.model.TaskItem;
import id.ac.usk.dompetdeadline.service.DateFormats;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.time.LocalDateTime;

public final class TaskRepository {
    public List<TaskItem> findAll() throws SQLException {
        String sql = """
                SELECT tu.id, tu.judul, tu.mata_kuliah, tu.tenggat, tu.prioritas, tu.selesai,
                       COALESCE(SUM(CASE WHEN ka.jenis = 'PENGELUARAN' THEN tr.jumlah ELSE 0 END),0) AS biaya
                  FROM tugas tu
                  LEFT JOIN transaksi tr ON tr.tugas_id = tu.id
                  LEFT JOIN kategori ka ON tr.kategori_id = ka.id
                 GROUP BY tu.id, tu.judul, tu.mata_kuliah, tu.tenggat, tu.prioritas, tu.selesai
                 ORDER BY tu.selesai, tu.tenggat, tu.id
                """;
        List<TaskItem> list = new ArrayList<>();
        try (Connection db = Database.getConnection();
             PreparedStatement ps = db.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {
            while (rs.next()) list.add(new TaskItem(rs.getLong("id"), rs.getString("judul"),
                    rs.getString("mata_kuliah"), DateFormats.fromDatabase(rs.getString("tenggat")),
                    rs.getString("prioritas"), rs.getInt("selesai") == 1, rs.getLong("biaya")));
        }
        return list;
    }
    public void save(long id, String title, String course, LocalDateTime deadline, String priority) throws SQLException {
        boolean updating = id > 0;
        String sql = updating ? "UPDATE tugas SET judul=?, mata_kuliah=?, tenggat=?, prioritas=? WHERE id=?"
                : "INSERT INTO tugas (judul,mata_kuliah,tenggat,prioritas,selesai) VALUES (?,?,?,?,0)";
        try (Connection db = Database.getConnection(); PreparedStatement ps = db.prepareStatement(sql)) {
            ps.setString(1,title); ps.setString(2,course);
            ps.setString(3,deadline.toString()); ps.setString(4,priority);
            if (updating) ps.setLong(5,id);
            ps.executeUpdate();
        }
    }
    public void setCompleted(long id, boolean completed) throws SQLException {
        try (Connection db = Database.getConnection();
             PreparedStatement ps = db.prepareStatement("UPDATE tugas SET selesai=? WHERE id=?")) {
            ps.setInt(1, completed ? 1 : 0); ps.setLong(2,id); ps.executeUpdate();
        }
    }
    public void delete(long id) throws SQLException {
        try (Connection db = Database.getConnection(); PreparedStatement ps = db.prepareStatement("DELETE FROM tugas WHERE id=?")) {
            ps.setLong(1,id); ps.executeUpdate();
        }
    }
}
