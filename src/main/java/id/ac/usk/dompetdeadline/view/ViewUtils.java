package id.ac.usk.dompetdeadline.view;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.util.concurrent.Callable;
import java.util.function.Consumer;

public final class ViewUtils {
    private ViewUtils() { }

    public static JLabel label(String text, int size, boolean bold, Color color) {
        JLabel result = new JLabel(text);
        result.setForeground(color);
        result.setFont(Theme.font(size, bold ? Font.BOLD : Font.PLAIN));
        return result;
    }
    public static JPanel page() {
        JPanel result = new JPanel(new BorderLayout(0,22));
        result.setBackground(Theme.BACKGROUND);
        result.setBorder(new EmptyBorder(36,38,28,38));
        return result;
    }
    public static JPanel header(String kicker, String title, String sub) {
        JPanel area = new JPanel(); area.setOpaque(false);
        area.setLayout(new BoxLayout(area, BoxLayout.Y_AXIS));
        area.add(label(kicker.toUpperCase(),11,true,Theme.PRIMARY));
        area.add(Box.createVerticalStrut(10));
        area.add(label(title,30,true,Theme.TEXT));
        area.add(Box.createVerticalStrut(8));
        area.add(label(sub,14,false,Theme.MUTED));
        return area;
    }
    public static JButton button(String text, boolean primary) {
        JButton result = new JButton(text);
        result.setFont(Theme.font(14,Font.BOLD));
        result.setForeground(primary ? Color.WHITE : Theme.TEXT);
        result.setBackground(primary ? Theme.PRIMARY : Theme.WHITE);
        result.setOpaque(true); result.setFocusPainted(false);
        result.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        result.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(primary ? Theme.PRIMARY : Theme.BORDER,1),
                new EmptyBorder(11,18,11,18)));
        return result;
    }
    public static JPanel card(LayoutManager layout) {
        RoundedPanel result = new RoundedPanel(Theme.WHITE, 18);
        result.setLayout(layout);
        result.setBorder(new EmptyBorder(24,24,24,24));
        return result;
    }
    public static JTextField input(int columns) {
        JTextField result = new JTextField(columns);
        styleInput(result); return result;
    }
    public static void styleInput(JComponent input) {
        input.setFont(Theme.font(14,Font.PLAIN));
        input.setBackground(Color.WHITE); input.setForeground(Theme.TEXT);
        input.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Theme.BORDER), new EmptyBorder(9,11,9,11)));
    }
    public static void styleTable(JTable table) {
        table.setFont(Theme.font(13,Font.PLAIN)); table.setForeground(Theme.TEXT);
        table.setBackground(Color.WHITE); table.setGridColor(Theme.BORDER);
        table.setRowHeight(44); table.setShowVerticalLines(false);
        table.setSelectionBackground(Theme.PRIMARY_LIGHT);
        table.setSelectionForeground(Theme.TEXT);
        table.setFillsViewportHeight(true);
        table.setIntercellSpacing(new Dimension(0,1));
        JTableHeader header = table.getTableHeader();
        header.setPreferredSize(new Dimension(0,43));
        header.setFont(Theme.font(12,Font.BOLD));
        header.setBackground(new Color(242,246,249));
        header.setForeground(Theme.MUTED);
        DefaultTableCellRenderer cell = new DefaultTableCellRenderer();
        cell.setBorder(new EmptyBorder(0,10,0,10));
        table.setDefaultRenderer(Object.class,cell);
    }
    public static JScrollPane scroll(JTable table) {
        JScrollPane result = new JScrollPane(table);
        result.setBorder(BorderFactory.createLineBorder(Theme.BORDER));
        result.getViewport().setBackground(Color.WHITE);
        return result;
    }
    public static void error(Component parent, Throwable error) {
        Throwable root = error;
        while (root.getCause() != null) root = root.getCause();
        String detail = root.getMessage() == null ? root.toString() : root.getMessage();
        JOptionPane.showMessageDialog(parent,detail,"Tidak berhasil",JOptionPane.ERROR_MESSAGE);
    }
    public static void info(Component parent, String message) {
        JOptionPane.showMessageDialog(parent,message,"Dompet Deadline",JOptionPane.INFORMATION_MESSAGE);
    }
    public static boolean confirm(Component parent,String message) {
        return JOptionPane.showConfirmDialog(parent,message,"Konfirmasi tindakan",
                JOptionPane.YES_NO_OPTION,JOptionPane.WARNING_MESSAGE) == JOptionPane.YES_OPTION;
    }
    public static <T> void async(Component parent, Callable<T> job, Consumer<T> onSuccess) {
        new SwingWorker<T,Void>() {
            @Override protected T doInBackground() throws Exception { return job.call(); }
            @Override protected void done() {
                try { onSuccess.accept(get()); }
                catch (Exception ex) { error(parent,ex); }
            }
        }.execute();
    }
}
