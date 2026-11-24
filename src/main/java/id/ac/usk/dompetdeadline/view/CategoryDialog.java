package id.ac.usk.dompetdeadline.view;

import id.ac.usk.dompetdeadline.model.Category;
import id.ac.usk.dompetdeadline.service.AppService;
import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

final class CategoryDialog extends JDialog {
    private final AppService service;
    private final DefaultListModel<Category> listModel=new DefaultListModel<>();
    private final JList<Category> list=new JList<>(listModel);
    private final JTextField name=ViewUtils.input(20);
    private final JComboBox<String> type=new JComboBox<>(new String[]{"PENGELUARAN","PEMASUKAN"});
    private boolean changed;
    CategoryDialog(Window owner,AppService service) throws Exception {
        super(owner,"Kelola kategori",ModalityType.APPLICATION_MODAL);this.service=service;
        setSize(520,500);setLocationRelativeTo(owner);setLayout(new BorderLayout(12,12));
        getContentPane().setBackground(Theme.BACKGROUND);
        JPanel main=ViewUtils.page();main.add(ViewUtils.header("KATEGORI","Atur kategori",
                "Buat kategori baru atau hapus yang belum digunakan."),BorderLayout.NORTH);
        list.setFont(Theme.font(14,Font.PLAIN));list.setFixedCellHeight(38);
        list.setCellRenderer(new DefaultListCellRenderer() {
            @Override public Component getListCellRendererComponent(JList<?> source, Object value, int index,
                                                                       boolean selected, boolean focused) {
                JLabel label=(JLabel)super.getListCellRendererComponent(source,value,index,selected,focused);
                if(value instanceof Category c) {
                    label.setText(c.name()+"   ·   "+(c.type().equals("PEMASUKAN")?"Pemasukan":"Pengeluaran"));
                    label.setBorder(BorderFactory.createEmptyBorder(6,11,6,6));
                }
                return label;
            }
        });
        main.add(new JScrollPane(list),BorderLayout.CENTER);
        JPanel bottom=new JPanel(new GridLayout(2,1,0,10));bottom.setOpaque(false);
        JPanel add=new JPanel(new FlowLayout(FlowLayout.LEFT,8,0));add.setOpaque(false);
        ViewUtils.styleInput(type);add.add(name);add.add(type);
        JButton save=ViewUtils.button("+ Tambah",true);save.addActionListener(e->addCategory());add.add(save);
        JPanel remove=new JPanel(new FlowLayout(FlowLayout.RIGHT,6,0));remove.setOpaque(false);
        JButton delete=ViewUtils.button("Hapus terpilih",false);delete.addActionListener(e->deleteCategory());
        JButton done=ViewUtils.button("Selesai",false);done.addActionListener(e->dispose());
        remove.add(delete);remove.add(done);
        bottom.add(add);bottom.add(remove);main.add(bottom,BorderLayout.SOUTH);add(main);
        refresh();
    }
    boolean isChanged(){return changed;}
    private void refresh() throws Exception {
        listModel.clear();for(Category c:service.categories())listModel.addElement(c);
    }
    private void addCategory(){
        try{service.addCategory(name.getText(),(String)type.getSelectedItem());
            name.setText("");changed=true;refresh();}
        catch(Exception e){ViewUtils.error(this,e);}
    }
    private void deleteCategory(){
        Category c=list.getSelectedValue();if(c==null){ViewUtils.info(this,"Pilih kategori yang ingin dihapus.");return;}
        if(!ViewUtils.confirm(this,"Hapus kategori ‘"+c.name()+"’? Kategori yang sudah dipakai transaksi tidak bisa dihapus."))return;
        try{service.deleteCategory(c.id());changed=true;refresh();}
        catch(Exception e){ViewUtils.error(this,e);}
    }
}
