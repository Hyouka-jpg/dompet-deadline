package id.ac.usk.dompetdeadline.view;

import id.ac.usk.dompetdeadline.repository.DashboardRepository;
import id.ac.usk.dompetdeadline.service.AppService;
import id.ac.usk.dompetdeadline.service.DashboardService;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.KeyStroke;
import javax.swing.JOptionPane;
import java.awt.Toolkit;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import javax.swing.SwingConstants;
import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.util.LinkedHashMap;
import java.util.Map;

public final class MainFrame extends JFrame {
    private final CardLayout pages = new CardLayout();
    private final JPanel content = new JPanel(pages);
    private final Map<String, JButton> buttons = new LinkedHashMap<>();
    private final DashboardPanel dashboard;
    private final AppService appService = new AppService();
    private final TransactionPanel transactions;
    private final TaskPanel tasks;
    private final ReportPanel reports;
    private final DeadlineNotifier notifier;

    public MainFrame() {
        super("Dompet Deadline — Keuangan & Tugas");
        dashboard = new DashboardPanel(new DashboardService(new DashboardRepository()));
        transactions = new TransactionPanel(appService, this::refreshOverview);
        tasks = new TaskPanel(appService, this::refreshOverview);
        reports = new ReportPanel(appService);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setMinimumSize(new Dimension(1020, 700));
        setSize(1300, 810);
        setLocationRelativeTo(null);

        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(Theme.BACKGROUND);
        root.add(createSidebar(), BorderLayout.WEST);

        content.setOpaque(false);
        content.add(dashboard, "Dashboard");
        content.add(transactions, "Transaksi");
        content.add(tasks, "Tugas");
        content.add(reports, "Laporan");
        root.add(content, BorderLayout.CENTER);
        setContentPane(root);
        setJMenuBar(createMenuBar());
        navigate("Dashboard");
        notifier = new DeadlineNotifier(this, appService);
        addWindowListener(new java.awt.event.WindowAdapter() {
            @Override public void windowClosing(java.awt.event.WindowEvent event) {
                notifier.stop();
            }
        });
    }

    private JMenuBar createMenuBar() {
        JMenuBar bar = new JMenuBar();
        JMenu file = new JMenu("Berkas");
        JMenuItem export = new JMenuItem("Ekspor transaksi ke CSV...");
        export.addActionListener(event -> reports.exportCsv());
        file.add(export);
        file.addSeparator();
        JMenuItem exit = new JMenuItem("Keluar");
        exit.addActionListener(event -> dispatchEvent(new java.awt.event.WindowEvent(
                this, java.awt.event.WindowEvent.WINDOW_CLOSING)));
        file.add(exit);
        bar.add(file);

        JMenu navigate = new JMenu("Navigasi");
        String[] sections = {"Dashboard", "Transaksi", "Tugas", "Laporan"};
        int shortcut = Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx();
        for (int i = 0; i < sections.length; i++) {
            String key = sections[i];
            JMenuItem item = new JMenuItem(key);
            item.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_1 + i, shortcut));
            item.addActionListener(event -> navigate(key));
            navigate.add(item);
        }
        bar.add(navigate);

        JMenu help = new JMenu("Bantuan");
        JMenuItem about = new JMenuItem("Tentang Dompet Deadline");
        about.addActionListener(event -> JOptionPane.showMessageDialog(this,
                "Dompet Deadline v1.0.0\nAplikasi manajemen keuangan dan tenggat tugas mahasiswa.\n"
                + "Dibuat dengan Java Swing, FlatLaf dan SQLite.",
                "Tentang aplikasi", JOptionPane.INFORMATION_MESSAGE));
        help.add(about);
        bar.add(help);
        return bar;
    }

    private JPanel createSidebar() {
        JPanel sidebar = new JPanel(new BorderLayout());
        sidebar.setPreferredSize(new Dimension(254, 0));
        sidebar.setBackground(Theme.SIDEBAR);
        sidebar.setBorder(BorderFactory.createEmptyBorder(36, 20, 24, 20));

        JPanel main = new JPanel();
        main.setOpaque(false);
        main.setLayout(new BoxLayout(main, BoxLayout.Y_AXIS));

        JLabel eyebrow = label("STUDENT PRODUCTIVITY", 11, Font.BOLD, Theme.SIDEBAR_MUTED);
        JLabel logo = label("dompet.", 29, Font.BOLD, Color.WHITE);
        JLabel second = label("deadline", 29, Font.BOLD, new Color(98, 213, 194));
        main.add(eyebrow);
        main.add(Box.createVerticalStrut(12));
        main.add(logo);
        main.add(second);
        main.add(Box.createVerticalStrut(55));
        main.add(label("MENU UTAMA", 11, Font.BOLD, Theme.SIDEBAR_MUTED));
        main.add(Box.createVerticalStrut(17));

        addNav(main, "Dashboard", "01   Ringkasan");
        addNav(main, "Transaksi", "02   Transaksi");
        addNav(main, "Tugas", "03   Tugas Kuliah");
        addNav(main, "Laporan", "04   Laporan");
        sidebar.add(main, BorderLayout.NORTH);

        JPanel footer = new JPanel(new GridLayout(2, 1, 0, 5));
        footer.setOpaque(false);
        footer.add(label("Rapi mengatur pengeluaran.", 12, Font.PLAIN, Theme.SIDEBAR_MUTED));
        footer.add(label("Tenang menghadapi deadline.", 12, Font.PLAIN, Theme.SIDEBAR_MUTED));
        sidebar.add(footer, BorderLayout.SOUTH);
        return sidebar;
    }

    private void addNav(JPanel parent, String key, String text) {
        JButton button = new JButton(text);
        button.setHorizontalAlignment(SwingConstants.LEFT);
        button.setFont(Theme.font(15, Font.BOLD));
        button.setForeground(Theme.SIDEBAR_MUTED);
        button.setBackground(Theme.SIDEBAR);
        button.setOpaque(true);
        button.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 10));
        button.setFocusPainted(false);
        button.setMaximumSize(new Dimension(Integer.MAX_VALUE, 50));
        button.addActionListener(event -> navigate(key));
        parent.add(button);
        parent.add(Box.createVerticalStrut(7));
        buttons.put(key, button);
    }

    private void navigate(String key) {
        pages.show(content, key);
        for (Map.Entry<String, JButton> entry : buttons.entrySet()) {
            boolean selected = entry.getKey().equals(key);
            entry.getValue().setBackground(selected ? new Color(35, 67, 81) : Theme.SIDEBAR);
            entry.getValue().setForeground(selected ? Color.WHITE : Theme.SIDEBAR_MUTED);
        }
        switch (key) {
            case "Dashboard" -> dashboard.refresh();
            case "Transaksi" -> transactions.refresh();
            case "Tugas" -> tasks.refresh();
            case "Laporan" -> reports.refresh();
            default -> { }
        }
    }

    private void refreshOverview() {
        dashboard.refresh();
        reports.refresh();
        transactions.refresh();
        tasks.refresh();
    }

    private JLabel label(String text, int size, int weight, Color color) {
        JLabel result = new JLabel(text);
        result.setFont(Theme.font(size, weight));
        result.setForeground(color);
        result.setAlignmentX(LEFT_ALIGNMENT);
        return result;
    }
}
