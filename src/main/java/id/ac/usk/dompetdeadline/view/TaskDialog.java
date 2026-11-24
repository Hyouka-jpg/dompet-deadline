package id.ac.usk.dompetdeadline.view;

import id.ac.usk.dompetdeadline.model.TaskItem;
import id.ac.usk.dompetdeadline.service.AppService;
import id.ac.usk.dompetdeadline.service.DateFormats;
import javax.swing.*;
import java.awt.*;
import java.time.LocalDateTime;

final class TaskDialog extends JDialog {
    private final AppService service;
    private final TaskItem existing;
    private final JTextField title=ViewUtils.input(27);
    private final JTextField course=ViewUtils.input(27);
    private final JTextField deadline=ViewUtils.input(27);
    private final JComboBox<String> priority=new JComboBox<>(new String[]{"SEDANG","TINGGI","RENDAH"});
    private boolean saved;
    TaskDialog(Window owner,AppService service,TaskItem existing){
        super(owner,existing==null?"Tugas kuliah baru":"Ubah tugas kuliah",ModalityType.APPLICATION_MODAL);
        this.service=service;this.existing=existing;
        setSize(485,485);setLocationRelativeTo(owner);setLayout(new BorderLayout());
        getContentPane().setBackground(Theme.BACKGROUND);
        JPanel form=new JPanel(new GridBagLayout());form.setOpaque(false);
        form.setBorder(BorderFactory.createEmptyBorder(30,32,20,32));
        ViewUtils.styleInput(priority);
        if(existing!=null){title.setText(existing.title());course.setText(existing.course());
            deadline.setText(existing.deadline().format(DateFormats.INPUT_DEADLINE));priority.setSelectedItem(existing.priority());}
        else deadline.setText(LocalDateTime.now().plusDays(1).withSecond(0).withNano(0).format(DateFormats.INPUT_DEADLINE));
        row(form,0,"Judul tugas",title);row(form,1,"Mata kuliah",course);
        row(form,2,"Tenggat (HH/BB/TTTT JJ:MM)",deadline);row(form,3,"Prioritas",priority);
        add(form,BorderLayout.CENTER);
        JPanel buttons=new JPanel(new FlowLayout(FlowLayout.RIGHT,10,16));buttons.setOpaque(false);
        JButton cancel=ViewUtils.button("Batal",false);cancel.addActionListener(e->dispose());
        JButton save=ViewUtils.button("Simpan tugas",true);save.addActionListener(e->submit());
        buttons.add(cancel);buttons.add(save);add(buttons,BorderLayout.SOUTH);
        getRootPane().setDefaultButton(save);
        getRootPane().registerKeyboardAction(e->dispose(),KeyStroke.getKeyStroke("ESCAPE"),JComponent.WHEN_IN_FOCUSED_WINDOW);
    }
    boolean isSaved(){return saved;}
    private static void row(JPanel form,int i,String label,JComponent input){
        GridBagConstraints c=new GridBagConstraints();c.gridx=0;c.gridy=2*i;c.anchor=GridBagConstraints.WEST;
        c.fill=GridBagConstraints.HORIZONTAL;c.weightx=1;c.insets=new Insets(10,0,6,0);
        form.add(ViewUtils.label(label,13,true,Theme.TEXT),c);
        GridBagConstraints f=new GridBagConstraints();f.gridx=0;f.gridy=2*i+1;f.fill=GridBagConstraints.HORIZONTAL;f.weightx=1;
        f.insets=new Insets(0,0,6,0);form.add(input,f);
    }
    private void submit(){
        try{service.saveTask(existing==null?0:existing.id(),title.getText(),course.getText(),
                DateFormats.parseDeadline(deadline.getText()),(String)priority.getSelectedItem());
            saved=true;dispose();}
        catch(Exception e){ViewUtils.error(this,e);}
    }
}
