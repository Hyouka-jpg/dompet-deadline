package id.ac.usk.dompetdeadline;

import id.ac.usk.dompetdeadline.repository.Database;
import id.ac.usk.dompetdeadline.view.MainFrame;

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import com.formdev.flatlaf.FlatLightLaf;

public final class Main {
    private Main() { }

    public static void main(String[] args) {
        System.setProperty("apple.laf.useScreenMenuBar", "true");
        try {
            FlatLightLaf.setup();
            UIManager.put("Button.arc", 12);
            UIManager.put("Component.arc", 12);
            UIManager.put("TextComponent.arc", 10);
        } catch (Exception ignored) {
            // Gunakan Look and Feel bawaan bila tema sistem tidak tersedia.
        }

        SwingUtilities.invokeLater(() -> {
            try {
                Database.initialize();
                new MainFrame().setVisible(true);
            } catch (Exception exception) {
                JOptionPane.showMessageDialog(null,
                        "Aplikasi gagal membuka database.\n" + exception.getMessage(),
                        "Kesalahan Awal Aplikasi", JOptionPane.ERROR_MESSAGE);
                exception.printStackTrace();
            }
        });
    }
}
