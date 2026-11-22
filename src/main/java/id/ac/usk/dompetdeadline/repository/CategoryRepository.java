package id.ac.usk.dompetdeadline.repository;

import id.ac.usk.dompetdeadline.model.Category;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public final class CategoryRepository {
    public List<Category> findAll() throws SQLException {
        List<Category> result = new ArrayList<>();
        String sql = "SELECT id, nama, jenis FROM kategori ORDER BY jenis, nama COLLATE NOCASE";
        try (Connection db = Database.getConnection();
             PreparedStatement ps = db.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {
            while (rs.next()) result.add(new Category(rs.getLong(1), rs.getString(2), rs.getString(3)));
        }
        return result;
    }
    public void insert(String name, String type) throws SQLException {
        String sql = "INSERT INTO kategori (nama, jenis) VALUES (?, ?)";
        try (Connection db = Database.getConnection(); PreparedStatement ps = db.prepareStatement(sql)) {
            ps.setString(1, name); ps.setString(2, type); ps.executeUpdate();
        }
    }
    public void delete(long id) throws SQLException {
        try (Connection db = Database.getConnection();
             PreparedStatement ps = db.prepareStatement("DELETE FROM kategori WHERE id = ?")) {
            ps.setLong(1, id); ps.executeUpdate();
        }
    }
}
