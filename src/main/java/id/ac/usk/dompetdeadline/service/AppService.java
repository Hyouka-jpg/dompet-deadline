package id.ac.usk.dompetdeadline.service;

import id.ac.usk.dompetdeadline.model.*;
import id.ac.usk.dompetdeadline.repository.*;
import java.io.IOException;
import java.nio.file.Path;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.List;

public final class AppService {
    private final CategoryRepository categories = new CategoryRepository();
    private final TaskRepository tasks = new TaskRepository();
    private final TransactionRepository transactions = new TransactionRepository();
    private final ReminderRepository reminders = new ReminderRepository();

    public List<Category> categories() throws SQLException { return categories.findAll(); }
    public List<TaskItem> tasks() throws SQLException { return tasks.findAll(); }
    public List<FinanceTransaction> transactions() throws SQLException { return transactions.findAll(); }
    public List<CategorySpending> spending(YearMonth month) throws SQLException { return transactions.spendingByCategory(month); }

    public void addCategory(String name, String type) throws SQLException {
        String cleaned = name.strip();
        if (cleaned.isBlank()) throw new IllegalArgumentException("Nama kategori wajib diisi.");
        if (cleaned.length() > 60) throw new IllegalArgumentException("Nama kategori maksimal 60 karakter.");
        if (!type.equals("PEMASUKAN") && !type.equals("PENGELUARAN")) throw new IllegalArgumentException("Jenis kategori tidak valid.");
        categories.insert(cleaned,type);
    }
    public void deleteCategory(long id) throws SQLException {
        try { categories.delete(id); }
        catch (SQLException ex) {
            if (ex.getMessage() != null && ex.getMessage().toLowerCase().contains("foreign key"))
                throw new IllegalArgumentException("Kategori tidak dapat dihapus karena sudah digunakan transaksi.");
            throw ex;
        }
    }

    public void saveTask(long id, String title, String course, LocalDateTime deadline, String priority) throws SQLException {
        if (title.strip().isBlank() || course.strip().isBlank())
            throw new IllegalArgumentException("Judul tugas dan mata kuliah wajib diisi.");
        if (title.strip().length() > 120 || course.strip().length() > 100)
            throw new IllegalArgumentException("Judul atau mata kuliah terlalu panjang.");
        if (!List.of("TINGGI", "SEDANG", "RENDAH").contains(priority))
            throw new IllegalArgumentException("Prioritas tugas tidak valid.");
        tasks.save(id, title.strip(), course.strip(), deadline, priority);
    }
    public void setTaskCompleted(long id, boolean completed) throws SQLException { tasks.setCompleted(id, completed); }
    public void deleteTask(long id) throws SQLException { tasks.delete(id); }

    public void saveTransaction(long id, LocalDate date, long amount, long categoryId, Long taskId, String note)
            throws SQLException {
        FinanceMath.requirePositiveAmount(amount);
        if (note.length() > 600) throw new IllegalArgumentException("Catatan maksimal 600 karakter.");
        Category selected = categories().stream().filter(c -> c.id() == categoryId).findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Pilih kategori yang tersedia."));
        if (taskId != null && !selected.type().equals("PENGELUARAN"))
            throw new IllegalArgumentException("Tugas terkait hanya bisa dipilih untuk transaksi pengeluaran.");
        if (taskId != null && tasks().stream().noneMatch(t -> t.id() == taskId))
            throw new IllegalArgumentException("Tugas yang dipilih sudah tidak tersedia.");
        transactions.save(id, date, amount, categoryId, taskId, note.strip());
    }
    public void deleteTransaction(long id) throws SQLException { transactions.delete(id); }
    public int exportCsv(Path file) throws SQLException, IOException { return CsvExporter.export(file,transactions()); }
    public List<TaskItem> dueReminders() throws SQLException { return reminders.findDue(LocalDateTime.now()); }
    public void markReminders(List<TaskItem> due) throws SQLException { reminders.markSent(due); }
}
