package id.ac.usk.dompetdeadline.view;

import id.ac.usk.dompetdeadline.model.CategorySpending;
import id.ac.usk.dompetdeadline.service.MoneyFormatter;
import javax.swing.*;
import java.awt.*;
import java.util.List;

/** Grafik batang horizontal mandiri (tanpa pustaka grafik). */
public final class SpendingChart extends JComponent {
    private List<CategorySpending> rows=List.of();
    public SpendingChart(){setOpaque(true);setBackground(Theme.WHITE);setFont(Theme.font(13,Font.PLAIN));}
    public void setData(List<CategorySpending> data){rows=List.copyOf(data);
        setPreferredSize(new Dimension(800,Math.max(330,rows.size()*58+105)));
        revalidate();repaint();
    }
    @Override protected void paintComponent(Graphics original){
        super.paintComponent(original);
        Graphics2D g=(Graphics2D)original.create();
        try {
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
            int W=getWidth(),H=getHeight();g.setColor(Theme.WHITE);g.fillRect(0,0,W,H);
            if(rows.isEmpty()){
                g.setFont(Theme.font(19,Font.BOLD));g.setColor(Theme.TEXT);
                drawCentered(g,"Belum ada pengeluaran pada bulan ini",H/2-8);
                g.setFont(Theme.font(13,Font.PLAIN));g.setColor(Theme.MUTED);
                drawCentered(g,"Catat transaksi pengeluaran untuk melihat grafik.",H/2+23);return;
            }
            int left=205,right=140,top=40,bottom=51;
            int graphWidth=Math.max(80,W-left-right);
            int baseline=Math.max(top+60,H-bottom);
            long max=1;for(CategorySpending row:rows)max=Math.max(max,row.amount());
            // Garis grid dan label sumbu dibagi menjadi empat bagian agar tetap terbaca.
            for(int tick=0;tick<=4;tick++){
                int x=left+Math.round(graphWidth*tick/4f);
                g.setColor(Theme.BORDER);g.setStroke(new BasicStroke(1));
                g.drawLine(x,top-10,x,baseline);
                long value=Math.round((double)max*tick/4);
                String label=compact(value);
                g.setColor(Theme.MUTED);g.setFont(Theme.font(11,Font.PLAIN));
                FontMetrics metrics=g.getFontMetrics();g.drawString(label,x-metrics.stringWidth(label)/2,baseline+23);
            }
            g.setFont(Theme.font(12,Font.BOLD));g.setColor(Theme.MUTED);
            g.drawString("PENGELUARAN (RP)",left,16);
            int step=58;
            for(int i=0;i<rows.size();i++){
                CategorySpending row=rows.get(i);
                int y=top+i*step+4;
                int drawWidth=Math.max(2,Math.round((float)row.amount()/max*graphWidth));
                g.setColor(i==0?Theme.PRIMARY:new Color(67,143,161));
                g.fillRoundRect(left,y,drawWidth,24,10,10);
                g.setColor(Theme.TEXT);g.setFont(Theme.font(13,Font.BOLD));
                String name=ellipsize(g,row.category(),left-23);
                g.drawString(name,left-15-g.getFontMetrics().stringWidth(name),y+17);
                g.setColor(Theme.TEXT);g.setFont(Theme.font(12,Font.BOLD));
                String price=MoneyFormatter.rupiah(row.amount());
                int priceX=Math.min(W-g.getFontMetrics().stringWidth(price)-12,left+drawWidth+10);
                g.drawString(price,priceX,y+17);
            }
        }finally{g.dispose();}
    }
    private static String compact(long value){
        if(value>=1_000_000_000)return String.format(java.util.Locale.US,"%.1f M",value/1_000_000_000d);
        if(value>=1_000_000)return String.format(java.util.Locale.US,"%.1f jt",value/1_000_000d);
        if(value>=1_000)return String.format(java.util.Locale.US,"%.0f rb",value/1_000d);
        return Long.toString(value);
    }
    private static String ellipsize(Graphics2D g,String text,int maxWidth){
        FontMetrics fm=g.getFontMetrics();if(fm.stringWidth(text)<=maxWidth)return text;
        while(text.length()>3 && fm.stringWidth(text+"…")>maxWidth)text=text.substring(0,text.length()-1);
        return text+"…";
    }
    private void drawCentered(Graphics2D g,String text,int y){
        g.drawString(text,(getWidth()-g.getFontMetrics().stringWidth(text))/2,y);
    }
}
