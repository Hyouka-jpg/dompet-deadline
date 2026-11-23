package id.ac.usk.dompetdeadline.view;

import id.ac.usk.dompetdeadline.model.DashboardData;
import id.ac.usk.dompetdeadline.model.UpcomingTask;
import id.ac.usk.dompetdeadline.service.DashboardService;
import id.ac.usk.dompetdeadline.service.MoneyFormatter;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingWorker;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;

public final class DashboardPanel extends JPanel {
    private static final Locale INDONESIAN = new Locale("id", "ID");
    private final DashboardService service;
    private final JLabel balance = numberLabel();
    private final JLabel income = numberLabel();
    private final JLabel expense = numberLabel();
    private final JLabel taskCost = numberLabel();
    private final JPanel deadlineList = new JPanel();
    private final JLabel status = new JLabel("Memuat data...");

    public DashboardPanel(DashboardService service) {
        super(new BorderLayout(0, 24));
        this.service = service;
        setBackground(Theme.BACKGROUND);
        setBorder(BorderFactory.createEmptyBorder(47, 46, 35, 46));
        buildUi();
    }

    private void buildUi() {
        JPanel page = new JPanel();
        page.setLayout(new BoxLayout(page, BoxLayout.Y_AXIS));
        page.setOpaque(false);

        JLabel eyebrow = text("RINGKASAN HARI INI", 12, Font.BOLD, Theme.PRIMARY);
        JLabel headline = text("Keuangan terarah, tugas terjaga.", 30, Font.BOLD, Theme.TEXT);
        JLabel subtitle = text("Pantau uang kuliah dan tenggat dalam satu tempat.",
                15, Font.PLAIN, Theme.MUTED);
        page.add(eyebrow);
        page.add(Box.createVerticalStrut(11));
        page.add(headline);
        page.add(Box.createVerticalStrut(9));
        page.add(subtitle);
        page.add(Box.createVerticalStrut(34));

        JPanel statistics = new JPanel(new GridLayout(1, 4, 14, 0));
        statistics.setOpaque(false);
        statistics.setMaximumSize(new Dimension(Integer.MAX_VALUE, 149));
        statistics.add(statCard("SALDO BULAN INI", balance, true));
        statistics.add(statCard("PEMASUKAN", income, false));
        statistics.add(statCard("PENGELUARAN", expense, false));
        statistics.add(statCard("BIAYA TUGAS AKTIF", taskCost, false));
        page.add(statistics);
        page.add(Box.createVerticalStrut(28));

        JPanel lower = new JPanel(new GridLayout(1, 2, 18, 0));
        lower.setOpaque(false);
        lower.setMaximumSize(new Dimension(Integer.MAX_VALUE, 320));

        RoundedPanel deadlines = new RoundedPanel(Theme.WHITE, 18);
        deadlines.setLayout(new BorderLayout(0, 20));
        deadlines.setBorder(BorderFactory.createEmptyBorder(26, 24, 24, 24));
        deadlines.add(text("03  /  Deadline terdekat", 18, Font.BOLD, Theme.TEXT), BorderLayout.NORTH);
        deadlineList.setOpaque(false);
        deadlineList.setLayout(new BoxLayout(deadlineList, BoxLayout.Y_AXIS));
        deadlines.add(deadlineList, BorderLayout.CENTER);
        lower.add(deadlines);

        RoundedPanel reminder = new RoundedPanel(Theme.PRIMARY_LIGHT, 18);
        reminder.setLayout(new BorderLayout(0, 18));
        reminder.setBorder(BorderFactory.createEmptyBorder(28, 26, 24, 26));
        reminder.add(text("Satu tempat. Lebih teratur.", 20, Font.BOLD, Theme.TEXT), BorderLayout.NORTH);
        JPanel note = new JPanel();
        note.setLayout(new BoxLayout(note, BoxLayout.Y_AXIS));
        note.setOpaque(false);
        note.add(text("Hubungkan pengeluaran ke tugas kuliah", 15, Font.BOLD, Theme.TEXT));
        note.add(Box.createVerticalStrut(13));
        note.add(text("agar biaya laporan, cetak, atau bahan", 14, Font.PLAIN, Theme.MUTED));
        note.add(Box.createVerticalStrut(6));
        note.add(text("praktikum bisa dihitung dengan jelas.", 14, Font.PLAIN, Theme.MUTED));
        note.add(Box.createVerticalStrut(28));
        note.add(text("SEMUA DATA TERSIMPAN OTOMATIS", 12, Font.BOLD, Theme.PRIMARY));
        reminder.add(note, BorderLayout.CENTER);
        lower.add(reminder);
        page.add(lower);

        add(page, BorderLayout.NORTH);
        status.setFont(Theme.font(12, Font.PLAIN));
        status.setForeground(Theme.MUTED);
        add(status, BorderLayout.SOUTH);
    }

    private RoundedPanel statCard(String title, JLabel value, boolean highlighted) {
        RoundedPanel card = new RoundedPanel(highlighted ? Theme.SIDEBAR : Theme.WHITE, 18);
        card.setLayout(new BorderLayout(0, 17));
        card.setBorder(BorderFactory.createEmptyBorder(23, 19, 23, 13));
        JLabel name = text(title, 11, Font.BOLD, highlighted ? Theme.SIDEBAR_MUTED : Theme.MUTED);
        value.setForeground(highlighted ? Theme.WHITE : Theme.TEXT);
        card.add(name, BorderLayout.NORTH);
        card.add(value, BorderLayout.CENTER);
        return card;
    }

    public void refresh() {
        status.setText("Memperbarui dashboard...");
        new SwingWorker<DashboardData, Void>() {
            @Override
            protected DashboardData doInBackground() throws Exception {
                return service.getDashboard(YearMonth.now());
            }

            @Override
            protected void done() {
                try {
                    DashboardData data = get();
                    balance.setText(MoneyFormatter.rupiah(data.balance()));
                    income.setText(MoneyFormatter.rupiah(data.income()));
                    expense.setText(MoneyFormatter.rupiah(data.expense()));
                    taskCost.setText(MoneyFormatter.rupiah(data.activeTaskCost()));
                    showDeadlines(data.upcomingTasks());
                    status.setText("Bulan " + YearMonth.now().format(
                            DateTimeFormatter.ofPattern("MMMM yyyy", INDONESIAN))
                            + "  •  Data tersimpan otomatis di SQLite");
                } catch (Exception error) {
                    status.setText("Gagal membaca data: " + error.getMessage());
                }
            }
        }.execute();
    }

    private void showDeadlines(List<UpcomingTask> tasks) {
        deadlineList.removeAll();
        if (tasks.isEmpty()) {
            deadlineList.add(text("Belum ada tugas yang dicatat.", 15, Font.PLAIN, Theme.MUTED));
            deadlineList.add(Box.createVerticalStrut(13));
            deadlineList.add(text("Nanti tugas terdekat muncul di sini.", 13, Font.PLAIN, Theme.MUTED));
        } else {
            for (UpcomingTask task : tasks) {
                JLabel name = text(task.title(), 15, Font.BOLD, Theme.TEXT);
                JLabel details = text(task.course() + "  •  "
                        + task.deadline().format(DateTimeFormatter.ofPattern("dd MMM yyyy", INDONESIAN))
                        + (task.deadline().isBefore(LocalDate.now()) ? "  •  Terlambat" : ""),
                        13, Font.PLAIN, Theme.MUTED);
                deadlineList.add(name);
                deadlineList.add(Box.createVerticalStrut(6));
                deadlineList.add(details);
                deadlineList.add(Box.createVerticalStrut(22));
            }
        }
        deadlineList.revalidate();
        deadlineList.repaint();
    }

    private static JLabel text(String text, int size, int style, java.awt.Color color) {
        JLabel label = new JLabel(text);
        label.setForeground(color);
        label.setFont(Theme.font(size, style));
        label.setAlignmentX(LEFT_ALIGNMENT);
        return label;
    }

    private static JLabel numberLabel() {
        return text("Rp 0", 22, Font.BOLD, Theme.TEXT);
    }
}
