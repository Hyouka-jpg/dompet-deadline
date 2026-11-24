package id.ac.usk.dompetdeadline.view;

import id.ac.usk.dompetdeadline.model.FinanceTransaction;
import id.ac.usk.dompetdeadline.service.AppService;
import id.ac.usk.dompetdeadline.service.DateFormats;
import id.ac.usk.dompetdeadline.service.MoneyFormatter;
import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

public final class TransactionPanel extends JPanel {
    private final AppService service;
    private final Runnable onChanged;
    private final TransactionModel model=new TransactionModel();
    private final JTable table=new JTable(model);
    private final JTextField search=ViewUtils.input(24);
    private final JComboBox<String> filter=new JComboBox<>(new String[]{"Semua jenis","Pemasukan","Pengeluaran"});
    private final JLabel count=ViewUtils.label("Memuat transaksi...",12,false,Theme.MUTED);
    private final TableRowSorter<TransactionModel> sorter=new TableRowSorter<>(model);

    public TransactionPanel(AppService service,Runnable onChanged) {
        super(new BorderLayout());this.service=service;this.onChanged=onChanged;
        setBackground(Theme.BACKGROUND);
        JPanel page=ViewUtils.page();add(page);
        JPanel top=new JPanel(new BorderLayout(10,18));top.setOpaque(false);
        top.add(ViewUtils.header("01 / KEUANGAN","Riwayat transaksi",
                "Seluruh pemasukan dan pengeluaran tercatat, termasuk biaya tugas."),BorderLayout.CENTER);
        JPanel actions=new JPanel(new FlowLayout(FlowLayout.RIGHT,8,0));actions.setOpaque(false);
        JButton categories=ViewUtils.button("Kelola kategori",false);
        JButton add=ViewUtils.button("+ Tambah transaksi",true);
        categories.addActionListener(e->manageCategories());add.addActionListener(e->edit(null));
        actions.add(categories);actions.add(add);top.add(actions,BorderLayout.EAST);
        JPanel tools=ViewUtils.card(new BorderLayout(12,0));
        JPanel left=new JPanel(new FlowLayout(FlowLayout.LEFT,10,0));left.setOpaque(false);
        left.add(ViewUtils.label("Cari",13,true,Theme.TEXT));left.add(search);
        ViewUtils.styleInput(filter);left.add(filter);tools.add(left,BorderLayout.WEST);
        JPanel right=new JPanel(new FlowLayout(FlowLayout.RIGHT,8,0));right.setOpaque(false);
        JButton edit=ViewUtils.button("Ubah",false);JButton delete=ViewUtils.button("Hapus",false);
        edit.addActionListener(e->editSelected());delete.addActionListener(e->deleteSelected());
        right.add(edit);right.add(delete);tools.add(right,BorderLayout.EAST);
        top.add(tools,BorderLayout.SOUTH);page.add(top,BorderLayout.NORTH);
        JPanel center=ViewUtils.card(new BorderLayout(0,12));
        ViewUtils.styleTable(table);table.setRowSorter(sorter);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setDefaultRenderer(java.time.LocalDate.class,new DefaultTableCellRenderer(){
            @Override protected void setValue(Object value){setText(value == null?"":((java.time.LocalDate)value).format(DateFormats.DISPLAY_DATE));}
        });
        table.setDefaultRenderer(Long.class,new DefaultTableCellRenderer(){
            @Override protected void setValue(Object value){setText(value==null?"":MoneyFormatter.rupiah((Long)value));setHorizontalAlignment(RIGHT);}
        });
        table.getColumnModel().getColumn(0).setPreferredWidth(130);
        table.getColumnModel().getColumn(3).setPreferredWidth(148);
        center.add(ViewUtils.scroll(table),BorderLayout.CENTER);
        center.add(count,BorderLayout.SOUTH);page.add(center,BorderLayout.CENTER);
        DocumentListener listener=new DocumentListener(){
            public void insertUpdate(DocumentEvent e){applyFilter();}
            public void removeUpdate(DocumentEvent e){applyFilter();}
            public void changedUpdate(DocumentEvent e){applyFilter();}
        };
        search.getDocument().addDocumentListener(listener);filter.addActionListener(e->applyFilter());
        refresh();
    }
    public void refresh(){
        ViewUtils.async(this,service::transactions,list->{model.setRows(list);applyFilter();});
    }
    private void applyFilter(){
        String q=search.getText().strip();int choice=filter.getSelectedIndex();
        sorter.setRowFilter(new RowFilter<>(){
            @Override public boolean include(Entry<? extends TransactionModel,? extends Integer> entry){
                FinanceTransaction t=model.get(entry.getIdentifier());
                boolean matching=choice==0||(choice==1&&t.type().equals("PEMASUKAN"))
                        ||(choice==2&&t.type().equals("PENGELUARAN"));
                return matching&&(q.isBlank() || (t.date()+" "+t.categoryName()+" "+t.type()+" "+t.amount()+" "+t.taskTitle()+" "+t.note())
                        .toLowerCase().contains(q.toLowerCase()));
            }
        });
        count.setText(table.getRowCount()+" transaksi ditampilkan · Klik judul kolom untuk mengurutkan");
    }
    private FinanceTransaction selected(){
        int row=table.getSelectedRow();
        if(row<0){ViewUtils.info(this,"Pilih salah satu transaksi dari tabel terlebih dahulu.");return null;}
        return model.get(table.convertRowIndexToModel(row));
    }
    private void editSelected(){FinanceTransaction t=selected();if(t!=null)edit(t);}
    private void edit(FinanceTransaction current){
        try {
            TransactionDialog dialog=new TransactionDialog(SwingUtilities.getWindowAncestor(this),service,current);
            dialog.setVisible(true);
            if(dialog.isSaved()){refresh();onChanged.run();}
        }catch(Exception ex){ViewUtils.error(this,ex);}
    }
    private void deleteSelected(){
        FinanceTransaction t=selected();if(t==null)return;
        if(!ViewUtils.confirm(this,"Hapus transaksi "+t.categoryName()+" senilai "
                +MoneyFormatter.rupiah(t.amount())+"?\nTindakan ini tidak dapat dibatalkan."))return;
        ViewUtils.async(this,()->{service.deleteTransaction(t.id());return null;},unused->{refresh();onChanged.run();});
    }
    private void manageCategories(){
        try {CategoryDialog dialog=new CategoryDialog(SwingUtilities.getWindowAncestor(this),service);
            dialog.setVisible(true);if(dialog.isChanged()){refresh();onChanged.run();}
        }catch(Exception ex){ViewUtils.error(this,ex);}
    }
    private static final class TransactionModel extends AbstractTableModel {
        private final String[] cols={"Tanggal","Kategori","Jenis","Nominal","Tugas terkait","Catatan"};
        private List<FinanceTransaction> rows=new ArrayList<>();
        public void setRows(List<FinanceTransaction> rows){this.rows=rows;fireTableDataChanged();}
        public FinanceTransaction get(int row){return rows.get(row);}
        @Override public int getRowCount(){return rows.size();}
        @Override public int getColumnCount(){return cols.length;}
        @Override public String getColumnName(int i){return cols[i];}
        @Override public Class<?> getColumnClass(int i){return i==0?java.time.LocalDate.class:i==3?Long.class:String.class;}
        @Override public Object getValueAt(int r,int c){FinanceTransaction t=rows.get(r);
            return switch(c){case 0->t.date();case 1->t.categoryName();case 2->t.type().equals("PEMASUKAN")?"Pemasukan":"Pengeluaran";
                case 3->t.amount();case 4->t.taskTitle().isBlank()?"—":t.taskTitle();default->t.note();};
        }
    }
}
