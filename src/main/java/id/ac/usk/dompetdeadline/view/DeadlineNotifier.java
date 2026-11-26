package id.ac.usk.dompetdeadline.view;

import id.ac.usk.dompetdeadline.model.TaskItem;
import id.ac.usk.dompetdeadline.service.AppService;
import id.ac.usk.dompetdeadline.service.DateFormats;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Mengingatkan tugas yang jatuh tempo dalam 24 jam.
 * macOS menggunakan dialog Swing agar pengingat terlihat.
 * Platform lain menggunakan System Tray jika tersedia, atau dialog cadangan.
 */
public final class DeadlineNotifier {
    private final Window owner;
    private final AppService service;
    private final Timer timer;
    private final AtomicBoolean running = new AtomicBoolean(false);
    private TrayIcon trayIcon;

    public DeadlineNotifier(Window owner, AppService service) {
        this.owner = owner;
        this.service = service;
        configureTray();

        timer = new Timer(120_000, e -> check());
        timer.setInitialDelay(15_000);
        timer.start();
        check();
    }

    private void configureTray() {
        // TrayIcon.displayMessage() tidak selalu menampilkan banner di macOS.
        // Pada macOS, langsung gunakan dialog yang pasti tampak di aplikasi.
        boolean isMac = System.getProperty("os.name", "")
                .toLowerCase(Locale.ROOT).contains("mac");
        if (isMac || !SystemTray.isSupported()) return;

        try {
            BufferedImage image = new BufferedImage(32, 32, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g = image.createGraphics();
            g.setColor(Theme.PRIMARY);
            g.fillRoundRect(1, 1, 30, 30, 9, 9);
            g.setColor(Color.WHITE);
            g.setFont(new Font("SansSerif", Font.BOLD, 23));
            g.drawString("D", 8, 25);
            g.dispose();

            trayIcon = new TrayIcon(image, "Dompet Deadline");
            trayIcon.setImageAutoSize(true);
            SystemTray.getSystemTray().add(trayIcon);
        } catch (Exception ex) {
            trayIcon = null;
        }
    }

    private void check() {
        if (!running.compareAndSet(false, true)) return;

        new SwingWorker<List<TaskItem>, Void>() {
            @Override
            protected List<TaskItem> doInBackground() throws Exception {
                // Hanya mencari tugas di background; jangan tandai dulu.
                return service.dueReminders();
            }

            @Override
            protected void done() {
                final List<TaskItem> due;
                try {
                    due = get();
                } catch (Exception ex) {
                    System.err.println("Pemeriksaan pengingat gagal: " + ex.getMessage());
                    running.set(false);
                    return;
                }

                if (due.isEmpty()) {
                    running.set(false);
                    return;
                }

                StringBuilder message = new StringBuilder();
                for (int i = 0; i < Math.min(due.size(), 5); i++) {
                    TaskItem task = due.get(i);
                    message.append("• ").append(task.title()).append(" — ")
                            .append(task.deadline().format(DateFormats.DISPLAY_DEADLINE))
                            .append("\n");
                }
                if (due.size() > 5) {
                    message.append("Dan ").append(due.size() - 5).append(" tugas lainnya.");
                }

                try {
                    if (trayIcon != null) {
                        try {
                            trayIcon.displayMessage("Deadline dalam 24 jam",
                                    message.toString(), TrayIcon.MessageType.WARNING);
                        } catch (RuntimeException ex) {
                            showDialog(message.toString());
                        }
                    } else {
                        showDialog(message.toString());
                    }
                } catch (RuntimeException ex) {
                    System.err.println("Tidak dapat menampilkan pengingat: " + ex.getMessage());
                    running.set(false);
                    return;
                }

                // Simpan penanda hanya SETELAH pengingat diproses untuk ditampilkan.
                // Database tetap diakses melalui SwingWorker, bukan di thread UI.
                new SwingWorker<Void, Void>() {
                    @Override
                    protected Void doInBackground() throws Exception {
                        service.markReminders(due);
                        return null;
                    }

                    @Override
                    protected void done() {
                        try {
                            get();
                        } catch (Exception ex) {
                            System.err.println("Gagal mencatat pengingat: " + ex.getMessage());
                        } finally {
                            running.set(false);
                        }
                    }
                }.execute();
            }
        }.execute();
    }

    private void showDialog(String message) {
        JOptionPane.showMessageDialog(owner, message,
                "Pengingat deadline dalam 24 jam", JOptionPane.WARNING_MESSAGE);
    }

    public void stop() {
        timer.stop();
        if (trayIcon != null) {
            SystemTray.getSystemTray().remove(trayIcon);
        }
    }
}
