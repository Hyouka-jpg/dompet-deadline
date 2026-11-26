package id.ac.usk.dompetdeadline.view;

import id.ac.usk.dompetdeadline.model.TaskItem;
import id.ac.usk.dompetdeadline.service.AppService;
import id.ac.usk.dompetdeadline.service.DateFormats;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

/** Pengingat berkala; catatan pengiriman tersimpan di SQLite agar tidak spam. */
public final class DeadlineNotifier {
    private final Window owner;
    private final AppService service;
    private final Timer timer;
    private final AtomicBoolean running=new AtomicBoolean();
    private TrayIcon trayIcon;

    public DeadlineNotifier(Window owner,AppService service){
        this.owner=owner;this.service=service;
        configureTray();
        timer=new Timer(120_000,e->check());
        timer.setInitialDelay(15_000);
        timer.start();
        // Pemeriksaan pertama dilakukan langsung setelah aplikasi dimulai.
        check();
    }
    private void configureTray(){
        try {
            if(!SystemTray.isSupported())return;
            BufferedImage image=new BufferedImage(32,32,BufferedImage.TYPE_INT_ARGB);
            Graphics2D g=image.createGraphics();g.setColor(Theme.PRIMARY);g.fillRoundRect(1,1,30,30,9,9);
            g.setColor(Color.WHITE);g.setFont(new Font("SansSerif",Font.BOLD,23));g.drawString("D",8,25);g.dispose();
            trayIcon=new TrayIcon(image,"Dompet Deadline");trayIcon.setImageAutoSize(true);
            SystemTray.getSystemTray().add(trayIcon);
        }catch(Exception ex){trayIcon=null;}
    }
    private void check(){
        if(!running.compareAndSet(false,true))return;
        new SwingWorker<List<TaskItem>,Void>(){
            @Override protected List<TaskItem> doInBackground() throws Exception {
                List<TaskItem> due=service.dueReminders();
                if(!due.isEmpty())service.markReminders(due);
                return due;
            }
            @Override protected void done(){
                running.set(false);
                try{List<TaskItem> due=get();if(due.isEmpty())return;
                    StringBuilder message=new StringBuilder();
                    for(int i=0;i<Math.min(due.size(),5);i++){
                        TaskItem t=due.get(i);
                        message.append("• ").append(t.title()).append(" — ")
                                .append(t.deadline().format(DateFormats.DISPLAY_DEADLINE)).append("\n");
                    }
                    if(due.size()>5)message.append("Dan ").append(due.size()-5).append(" tugas lainnya.");
                    if(trayIcon!=null)trayIcon.displayMessage("Deadline dalam 24 jam",message.toString(),TrayIcon.MessageType.WARNING);
                    else JOptionPane.showMessageDialog(owner,message.toString(),"Pengingat deadline dalam 24 jam",JOptionPane.WARNING_MESSAGE);
                }catch(Exception ex){System.err.println("Pemeriksaan pengingat gagal: "+ex.getMessage());}
            }
        }.execute();
    }
    public void stop(){timer.stop();if(trayIcon!=null)SystemTray.getSystemTray().remove(trayIcon);}
}
