package id.ac.usk.dompetdeadline.repository;

import id.ac.usk.dompetdeadline.model.TaskItem;
import id.ac.usk.dompetdeadline.service.DateFormats;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public final class ReminderRepository {
    public List<TaskItem> findDue(LocalDateTime now) throws SQLException {
        String sql = """
                SELECT t.id,t.judul,t.mata_kuliah,t.tenggat,t.prioritas,t.selesai
                  FROM tugas t LEFT JOIN pengingat p ON p.tugas_id=t.id AND p.tenggat=t.tenggat
                 WHERE t.selesai=0 AND t.tenggat>=? AND t.tenggat<=? AND p.tugas_id IS NULL
                 ORDER BY t.tenggat ASC
                """;
        List<TaskItem> list = new ArrayList<>();
        try (Connection db = Database.getConnection(); PreparedStatement ps = db.prepareStatement(sql)) {
            ps.setString(1, now.toString()); ps.setString(2, now.plusHours(24).toString());
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(new TaskItem(rs.getLong("id"), rs.getString("judul"),
                        rs.getString("mata_kuliah"), DateFormats.fromDatabase(rs.getString("tenggat")),
                        rs.getString("prioritas"),false,0));
            }
        }
        return list;
    }
    public void markSent(List<TaskItem> tasks) throws SQLException {
        String sql = """
                INSERT INTO pengingat (tugas_id,tenggat,terkirim_pada) VALUES (?,?,?)
                ON CONFLICT(tugas_id) DO UPDATE SET tenggat=excluded.tenggat,
                terkirim_pada=excluded.terkirim_pada
                """;
        try (Connection db = Database.getConnection(); PreparedStatement ps = db.prepareStatement(sql)) {
            for (TaskItem task : tasks) {
                ps.setLong(1, task.id()); ps.setString(2, task.deadline().toString());
                ps.setString(3, LocalDateTime.now().toString()); ps.addBatch();
            }
            ps.executeBatch();
        }
    }
}
