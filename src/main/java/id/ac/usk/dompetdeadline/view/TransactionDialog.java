package id.ac.usk.dompetdeadline.view;

import id.ac.usk.dompetdeadline.model.Category;
import id.ac.usk.dompetdeadline.model.FinanceTransaction;
import id.ac.usk.dompetdeadline.model.TaskItem;
import id.ac.usk.dompetdeadline.service.AppService;
import id.ac.usk.dompetdeadline.service.DateFormats;

import javax.swing.*;
import java.awt.*;
import java.time.LocalDate;
import java.util.List;

final class TransactionDialog extends JDialog {
    private final AppService service;
    private final FinanceTransaction existing;
    private final JComboBox<String> type = new JComboBox<>(new String[]{"PENGELUARAN", "PEMASUKAN"});
    private final JComboBox<Category> category = new JComboBox<>();
    private final JComboBox<Object> task = new JComboBox<>();
    private final JTextField date = ViewUtils.input(20);
    private final JTextField amount = ViewUtils.input(20);
    private final JTextArea note = new JTextArea(3,20);
    private final List<Category> categories;
    private final List<TaskItem> tasks;
    private boolean saved;

    TransactionDialog(Window owner, AppService service, FinanceTransaction existing) throws Exception {
        super(owner,existing == null ? "Catat transaksi baru" : "Ubah transaksi",ModalityType.APPLICATION_MODAL);
        this.service=service; this.existing=existing;
        categories=service.categories(); tasks=service.tasks();
        setSize(500,625); setMinimumSize(new Dimension(470,610)); setLocationRelativeTo(owner);
        setLayout(new BorderLayout()); getContentPane().setBackground(Theme.BACKGROUND);
        JPanel form=new JPanel(new GridBagLayout()); form.setOpaque(false);
        form.setBorder(BorderFactory.createEmptyBorder(24,30,12,30));
        ViewUtils.styleInput(type);ViewUtils.styleInput(category);ViewUtils.styleInput(task);
        ViewUtils.styleInput(note); note.setLineWrap(true); note.setWrapStyleWord(true);
        date.setText(existing == null ? LocalDate.now().format(DateFormats.INPUT_DATE)
                : existing.date().format(DateFormats.INPUT_DATE));
        amount.setText(existing == null ? "" : Long.toString(existing.amount()));
        note.setText(existing == null ? "" : existing.note());
        addRow(form,0,"Jenis transaksi",type);
        addRow(form,1,"Tanggal (HH/BB/TTTT)",date);
        addRow(form,2,"Nominal dalam rupiah",amount);
        addRow(form,3,"Kategori",category);
        addRow(form,4,"Tugas terkait (opsional)",task);
        addRow(form,5,"Catatan",new JScrollPane(note));
        add(form,BorderLayout.CENTER);
        JPanel footer=new JPanel(new FlowLayout(FlowLayout.RIGHT,12,14)); footer.setOpaque(false);
        JButton cancel=ViewUtils.button("Batal",false);
        JButton submit=ViewUtils.button(existing==null?"Simpan transaksi":"Simpan perubahan",true);
        cancel.addActionListener(e->dispose()); submit.addActionListener(e->submit());
        footer.add(cancel);footer.add(submit);add(footer,BorderLayout.SOUTH);
        getRootPane().setDefaultButton(submit);
        getRootPane().registerKeyboardAction(e->dispose(),KeyStroke.getKeyStroke("ESCAPE"),JComponent.WHEN_IN_FOCUSED_WINDOW);
        type.addActionListener(e->refreshChoices());
        if(existing!=null)type.setSelectedItem(existing.type());
        refreshChoices();
        if(existing!=null) {
            for(int i=0;i<category.getItemCount();i++)if(category.getItemAt(i).id()==existing.categoryId())category.setSelectedIndex(i);
            if(existing.taskId()!=null)for(int i=0;i<task.getItemCount();i++) {
                Object item=task.getItemAt(i);
                if(item instanceof TaskItem && ((TaskItem)item).id()==existing.taskId()) task.setSelectedIndex(i);
            }
        }
    }
    boolean isSaved(){return saved;}
    private void refreshChoices(){
        Object previousCategory=category.getSelectedItem(); Object previousTask=task.getSelectedItem();
        category.removeAllItems();
        for(Category c:categories)if(c.type().equals(type.getSelectedItem()))category.addItem(c);
        if(previousCategory instanceof Category && ((Category)previousCategory).type().equals(type.getSelectedItem()))
            category.setSelectedItem(previousCategory);
        task.removeAllItems(); task.addItem("Tanpa tugas terkait");
        boolean expense="PENGELUARAN".equals(type.getSelectedItem());
        if(expense)for(TaskItem item:tasks)task.addItem(item);
        task.setEnabled(expense);
        if(expense && previousTask instanceof TaskItem)task.setSelectedItem(previousTask);
    }
    private static void addRow(JPanel form,int row,String title,JComponent field) {
        GridBagConstraints label=new GridBagConstraints();label.gridx=0;label.gridy=row*2;label.weightx=1;
        label.anchor=GridBagConstraints.WEST;label.fill=GridBagConstraints.HORIZONTAL;
        label.insets=new Insets(row==0?0:9,0,5,0);
        form.add(ViewUtils.label(title,13,true,Theme.TEXT),label);
        GridBagConstraints control=new GridBagConstraints();control.gridx=0;control.gridy=row*2+1;control.weightx=1;
        control.fill=GridBagConstraints.HORIZONTAL;control.insets=new Insets(0,0,4,0);
        form.add(field,control);
    }
    private void submit() {
        try {
            Category selected=(Category)category.getSelectedItem();
            if(selected==null)throw new IllegalArgumentException("Buat kategori untuk jenis transaksi ini dahulu.");
            String raw=amount.getText().strip().replace(".","").replace(" ","");
            if(!raw.matches("[0-9]+"))throw new IllegalArgumentException("Nominal harus angka positif, contohnya 25000.");
            long total=Long.parseLong(raw);
            Object linked=task.getSelectedItem();
            Long taskId=linked instanceof TaskItem ? ((TaskItem)linked).id():null;
            service.saveTransaction(existing==null?0:existing.id(),DateFormats.parseDate(date.getText()),
                    total,selected.id(),taskId,note.getText());
            saved=true;dispose();
        }catch(Exception ex){ViewUtils.error(this,ex);}
    }
}
