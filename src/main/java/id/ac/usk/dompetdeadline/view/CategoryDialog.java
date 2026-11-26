package id.ac.usk.dompetdeadline.view;

import id.ac.usk.dompetdeadline.model.Category;
import id.ac.usk.dompetdeadline.service.AppService;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

final class CategoryDialog extends JDialog {
    private final AppService service;
    private final DefaultListModel<Category> listModel = new DefaultListModel<>();
    private final JList<Category> list = new JList<>(listModel);
    private final JTextField name = ViewUtils.input(20);
    private final JComboBox<String> type = new JComboBox<>(
            new String[]{"PENGELUARAN", "PEMASUKAN"});
    private boolean changed;

    CategoryDialog(Window owner, AppService service) throws Exception {
        super(owner, "Kelola kategori", ModalityType.APPLICATION_MODAL);
        this.service = service;

        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        getContentPane().setBackground(Theme.BACKGROUND);
        setLayout(new BorderLayout());

        JPanel main = new JPanel(new BorderLayout(0, 18));
        main.setBackground(Theme.BACKGROUND);
        main.setBorder(new EmptyBorder(26, 30, 24, 30));
        main.add(ViewUtils.header("KATEGORI", "Atur kategori",
                "Buat kategori baru atau hapus yang belum digunakan."), BorderLayout.NORTH);

        // Daftar kategori memenuhi ruang tengah dan dapat di-scroll.
        list.setFont(Theme.font(14, Font.PLAIN));
        list.setFixedCellHeight(40);
        list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        list.setCellRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> source, Object value,
                    int index, boolean selected, boolean focused) {
                JLabel label = (JLabel) super.getListCellRendererComponent(
                        source, value, index, selected, focused);
                if (value instanceof Category c) {
                    label.setText(c.name() + "   ·   "
                            + ("PEMASUKAN".equals(c.type()) ? "Pemasukan" : "Pengeluaran"));
                    label.setBorder(new EmptyBorder(6, 12, 6, 8));
                }
                return label;
            }
        });
        JScrollPane scroll = new JScrollPane(list);
        scroll.setBorder(BorderFactory.createLineBorder(Theme.BORDER));
        scroll.getViewport().setBackground(Theme.WHITE);
        main.add(scroll, BorderLayout.CENTER);

        // Form dua kolom; tombol Tambah ada di baris sendiri agar tidak terpotong.
        ViewUtils.styleInput(type);
        type.setPreferredSize(new Dimension(170, 42));

        JPanel form = new JPanel(new GridBagLayout());
        form.setOpaque(false);
        GridBagConstraints g = new GridBagConstraints();
        g.fill = GridBagConstraints.HORIZONTAL;
        g.anchor = GridBagConstraints.WEST;

        g.gridx = 0;
        g.gridy = 0;
        g.weightx = 1;
        g.insets = new Insets(0, 0, 7, 12);
        form.add(ViewUtils.label("Nama kategori", 13, true, Theme.TEXT), g);

        g.gridx = 1;
        g.weightx = 0;
        g.insets = new Insets(0, 0, 7, 0);
        form.add(ViewUtils.label("Jenis transaksi", 13, true, Theme.TEXT), g);

        g.gridx = 0;
        g.gridy = 1;
        g.weightx = 1;
        g.insets = new Insets(0, 0, 0, 12);
        form.add(name, g);

        g.gridx = 1;
        g.weightx = 0;
        g.insets = new Insets(0, 0, 0, 0);
        form.add(type, g);

        JButton save = ViewUtils.button("+ Tambah kategori", true);
        save.addActionListener(e -> addCategory());
        getRootPane().setDefaultButton(save);

        JPanel addRow = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        addRow.setOpaque(false);
        addRow.add(save);

        JButton delete = ViewUtils.button("Hapus terpilih", false);
        delete.addActionListener(e -> deleteCategory());
        JButton done = ViewUtils.button("Selesai", false);
        done.addActionListener(e -> dispose());

        JPanel footer = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        footer.setOpaque(false);
        footer.setBorder(new EmptyBorder(10, 0, 0, 0));
        footer.add(delete);
        footer.add(done);

        JPanel addArea = new JPanel(new BorderLayout(0, 12));
        addArea.setOpaque(false);
        addArea.add(form, BorderLayout.CENTER);
        addArea.add(addRow, BorderLayout.SOUTH);

        JPanel bottom = new JPanel(new BorderLayout(0, 14));
        bottom.setOpaque(false);
        bottom.add(addArea, BorderLayout.NORTH);
        bottom.add(footer, BorderLayout.SOUTH);
        main.add(bottom, BorderLayout.SOUTH);

        add(main, BorderLayout.CENTER);
        setSize(680, 650);
        setMinimumSize(new Dimension(610, 580));
        setLocationRelativeTo(owner);
        refresh();
    }

    boolean isChanged() {
        return changed;
    }

    private void refresh() throws Exception {
        listModel.clear();
        for (Category c : service.categories()) {
            listModel.addElement(c);
        }
    }

    private void addCategory() {
        try {
            service.addCategory(name.getText(), (String) type.getSelectedItem());
            name.setText("");
            changed = true;
            refresh();
            name.requestFocusInWindow();
        } catch (Exception e) {
            ViewUtils.error(this, e);
        }
    }

    private void deleteCategory() {
        Category c = list.getSelectedValue();
        if (c == null) {
            ViewUtils.info(this, "Pilih kategori yang ingin dihapus.");
            return;
        }
        if (!ViewUtils.confirm(this, "Hapus kategori ‘" + c.name()
                + "’? Kategori yang sudah dipakai transaksi tidak bisa dihapus.")) {
            return;
        }
        try {
            service.deleteCategory(c.id());
            changed = true;
            refresh();
        } catch (Exception e) {
            ViewUtils.error(this, e);
        }
    }
}
