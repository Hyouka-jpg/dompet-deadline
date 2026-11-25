package id.ac.usk.dompetdeadline.view;

import id.ac.usk.dompetdeadline.model.CategorySpending;
import id.ac.usk.dompetdeadline.service.AppService;
import id.ac.usk.dompetdeadline.service.MoneyFormatter;
import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.nio.file.Path;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;

public final class ReportPanel extends JPanel {
    private final AppService service;
    private final SpendingChart chart=new SpendingChart();
    private final JLabel monthLabel=ViewUtils.label("",16,true,Theme.TEXT);
    private final JLabel summary=ViewUtils.label("",13,false,Theme.MUTED);
    private YearMonth month=YearMonth.now();
    private static final DateTimeFormatter MONTH_FORMAT=DateTimeFormatter.ofPattern("MMMM yyyy",new Locale("id","ID"));

    public ReportPanel(AppService service){
        super(new BorderLayout());this.service=service;setBackground(Theme.BACKGROUND);
        JPanel page=ViewUtils.page();add(page);
        JPanel head=new JPanel(new BorderLayout(12,0));head.setOpaque(false);
        head.add(ViewUtils.header("03 / ANALISIS","Laporan pengeluaran",
                "Lihat pengeluaran per kategori dan ekspor seluruh transaksi ke CSV."),BorderLayout.CENTER);
        JButton export=ViewUtils.button("Ekspor CSV",true);export.addActionListener(e->exportCsv());
        JPanel action=new JPanel(new FlowLayout(FlowLayout.RIGHT,0,0));action.setOpaque(false);action.add(export);
        head.add(action,BorderLayout.EAST);page.add(head,BorderLayout.NORTH);
        JPanel content=ViewUtils.card(new BorderLayout(0,22));
        JPanel bar=new JPanel(new BorderLayout());bar.setOpaque(false);
        JPanel selector=new JPanel(new FlowLayout(FlowLayout.LEFT,11,0));selector.setOpaque(false);
        JButton previous=ViewUtils.button("‹",false);JButton next=ViewUtils.button("›",false);
        previous.addActionListener(e->{month=month.minusMonths(1);refresh();});
        next.addActionListener(e->{month=month.plusMonths(1);refresh();});
        selector.add(previous);selector.add(monthLabel);selector.add(next);
        bar.add(selector,BorderLayout.WEST);bar.add(summary,BorderLayout.EAST);
        content.add(bar,BorderLayout.NORTH);
        JScrollPane scroll=new JScrollPane(chart);
        scroll.setBorder(null);scroll.getViewport().setBackground(Theme.WHITE);
        scroll.getVerticalScrollBar().setUnitIncrement(16);
        content.add(scroll,BorderLayout.CENTER);page.add(content,BorderLayout.CENTER);
        refresh();
    }
    public void refresh(){
        monthLabel.setText(month.format(MONTH_FORMAT));
        YearMonth selected=month;
        ViewUtils.async(this,()->service.spending(selected),rows->{
            if(!selected.equals(month))return;
            chart.setData(rows);
            long total=0;for(CategorySpending c:rows)total=Math.addExact(total,c.amount());
            summary.setText(rows.size()+" kategori · Total "+MoneyFormatter.rupiah(total));
        });
    }
    public void exportCsv(){
        JFileChooser chooser=new JFileChooser();
        chooser.setDialogTitle("Simpan laporan transaksi CSV");
        chooser.setSelectedFile(new File("dompet-deadline-transaksi.csv"));
        if(chooser.showSaveDialog(this)!=JFileChooser.APPROVE_OPTION)return;
        Path path=chooser.getSelectedFile().toPath();
        if(!path.toString().toLowerCase().endsWith(".csv"))path=Path.of(path.toString()+".csv");
        if(path.toFile().exists()&&!ViewUtils.confirm(this,"Berkas sudah ada. Timpa berkas tersebut?"))return;
        Path target=path;
        ViewUtils.async(this,()->service.exportCsv(target),count->
                ViewUtils.info(this,"Berhasil mengekspor "+count+" transaksi.\nLokasi: "+target));
    }
}
