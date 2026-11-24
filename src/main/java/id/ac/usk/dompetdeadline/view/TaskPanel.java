package id.ac.usk.dompetdeadline.view;

import id.ac.usk.dompetdeadline.model.TaskItem;
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

public final class TaskPanel extends JPanel {
    private final AppService service;private final Runnable onChanged;
    private final TaskModel model=new TaskModel();private final JTable table=new JTable(model);
    private final TableRowSorter<TaskModel> sorter=new TableRowSorter<>(model);
    private final JTextField search=ViewUtils.input(23);
    private final JComboBox<String> filter=new JComboBox<>(new String[]{"Semua tugas","Belum selesai","Selesai"});
    private final JLabel count=ViewUtils.label("Memuat tugas...",12,false,Theme.MUTED);

    public TaskPanel(AppService service,Runnable onChanged){
        super(new BorderLayout());this.service=service;this.onChanged=onChanged;setBackground(Theme.BACKGROUND);
        JPanel page=ViewUtils.page();add(page);
        JPanel top=new JPanel(new BorderLayout(10,20));top.setOpaque(false);
        top.add(ViewUtils.header("02 / AKADEMIK","Tugas & tenggat",
                "Pantau prioritas, status pengerjaan, dan total pengeluaran tiap tugas."),BorderLayout.CENTER);
        JButton add=ViewUtils.button("+ Tambah tugas",true);add.addActionListener(e->edit(null));
        JPanel action=new JPanel(new FlowLayout(FlowLayout.RIGHT,0,0));action.setOpaque(false);action.add(add);top.add(action,BorderLayout.EAST);
        JPanel tools=ViewUtils.card(new BorderLayout(10,0));
        JPanel left=new JPanel(new FlowLayout(FlowLayout.LEFT,10,0));left.setOpaque(false);
        left.add(ViewUtils.label("Cari",13,true,Theme.TEXT));left.add(search);
        ViewUtils.styleInput(filter);left.add(filter);tools.add(left,BorderLayout.WEST);
        JPanel right=new JPanel(new FlowLayout(FlowLayout.RIGHT,7,0));right.setOpaque(false);
        JButton toggle=ViewUtils.button("Tandai selesai/aktif",false);
        JButton edit=ViewUtils.button("Ubah",false);JButton delete=ViewUtils.button("Hapus",false);
        toggle.addActionListener(e->toggle());edit.addActionListener(e->editSelected());delete.addActionListener(e->deleteSelected());
        right.add(toggle);right.add(edit);right.add(delete);tools.add(right,BorderLayout.EAST);
        top.add(tools,BorderLayout.SOUTH);page.add(top,BorderLayout.NORTH);
        JPanel center=ViewUtils.card(new BorderLayout(0,12));
        ViewUtils.styleTable(table);table.setRowSorter(sorter);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setDefaultRenderer(java.time.LocalDateTime.class,new DefaultTableCellRenderer(){
            @Override protected void setValue(Object v){setText(((java.time.LocalDateTime)v).format(DateFormats.DISPLAY_DEADLINE));}
        });
        table.setDefaultRenderer(Long.class,new DefaultTableCellRenderer(){
            @Override protected void setValue(Object v){setText(MoneyFormatter.rupiah((Long)v));setHorizontalAlignment(RIGHT);}
        });
        table.getColumnModel().getColumn(0).setPreferredWidth(190);
        table.getColumnModel().getColumn(2).setPreferredWidth(185);
        table.getColumnModel().getColumn(5).setPreferredWidth(150);
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
    public void refresh(){ViewUtils.async(this,service::tasks,list->{model.setRows(list);applyFilter();});}
    private void applyFilter(){
        String q=search.getText().strip().toLowerCase();int selected=filter.getSelectedIndex();
        sorter.setRowFilter(new RowFilter<>(){
            @Override public boolean include(Entry<? extends TaskModel,? extends Integer> entry){
                TaskItem t=model.get(entry.getIdentifier());
                return (selected==0||(selected==1&&!t.completed())||(selected==2&&t.completed()))
                        && (q.isEmpty()||(t.title()+" "+t.course()+" "+t.priority()).toLowerCase().contains(q));
            }
        });
        count.setText(table.getRowCount()+" tugas ditampilkan · Total biaya dihitung dari transaksi pengeluaran terkait");
    }
    private TaskItem selected(){
        int r=table.getSelectedRow();if(r<0){ViewUtils.info(this,"Pilih salah satu tugas terlebih dahulu.");return null;}
        return model.get(table.convertRowIndexToModel(r));
    }
    private void editSelected(){TaskItem task=selected();if(task!=null)edit(task);}
    private void edit(TaskItem current){
        TaskDialog dialog=new TaskDialog(SwingUtilities.getWindowAncestor(this),service,current);
        dialog.setVisible(true);if(dialog.isSaved()){refresh();onChanged.run();}
    }
    private void toggle(){TaskItem t=selected();if(t==null)return;
        ViewUtils.async(this,()->{service.setTaskCompleted(t.id(),!t.completed());return null;},unused->{refresh();onChanged.run();});
    }
    private void deleteSelected(){TaskItem t=selected();if(t==null)return;
        if(!ViewUtils.confirm(this,"Hapus tugas ‘"+t.title()+"’?\nTransaksi terkait tetap ada, tetapi hubungan dengan tugas ini dihapus."))return;
        ViewUtils.async(this,()->{service.deleteTask(t.id());return null;},unused->{refresh();onChanged.run();});
    }
    private static final class TaskModel extends AbstractTableModel{
        private final String[] cols={"Judul tugas","Mata kuliah","Tenggat","Prioritas","Status","Total biaya"};
        private List<TaskItem> rows=new ArrayList<>();
        public void setRows(List<TaskItem> rows){this.rows=rows;fireTableDataChanged();}
        public TaskItem get(int i){return rows.get(i);}
        @Override public int getRowCount(){return rows.size();}
        @Override public int getColumnCount(){return cols.length;}
        @Override public String getColumnName(int i){return cols[i];}
        @Override public Class<?> getColumnClass(int i){return i==2?java.time.LocalDateTime.class:i==5?Long.class:String.class;}
        @Override public Object getValueAt(int r,int c){TaskItem t=rows.get(r);return switch(c){
            case 0->t.title();case 1->t.course();case 2->t.deadline();case 3->t.priority();
            case 4->t.completed()?"Selesai":"Aktif";default->t.totalCost();};}
    }
}
