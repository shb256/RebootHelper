import java.io.*;
import java.awt.Dialog;
import java.awt.Label;
import java.awt.Window;
import java.awt.Button;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.concurrent.TimeUnit;
import javax.swing.*;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.sql.Timestamp;
import java.time.LocalDateTime;

public class RebootHelper {
    static String formatValue(int i){
        if (i < 10) {
            return "0" + i;
        } 
        return  "" + i;
    }
    static void runTask(){
        try {
            Runtime.getRuntime().exec("schtasks /delete /tn force_reboot /f");
            Runtime.getRuntime().exec("shutdown -r -t 60 -c \"Das System startet in 60 Sekunden neu\"");
            System.exit(0);
        } catch (IOException io) { System.out.print(io); }
    }
    public static void main(String args[]) {
        try{
        Process checkRunning = Runtime.getRuntime().exec("schtasks /query /tn force_reboot");
        if( checkRunning.waitFor()== 0){
            System.out.println("Task already set");
            System.exit(0);
        }
    }catch(IOException io){

    }catch(InterruptedException ie){

    }

        int dauer = 8; // in Tagen
        LocalDateTime currentDateAndTime = LocalDateTime.now();
        LocalDateTime runReboot = currentDateAndTime.plusHours(dauer);
        Timestamp tsNow = Timestamp.valueOf(currentDateAndTime);
        Timestamp tsRunReboot = Timestamp.valueOf(runReboot);
        String startDate = formatValue(runReboot.getDayOfMonth())+"/"+formatValue(runReboot.getMonthValue())+"/"+formatValue(runReboot.getYear());
        String startTime = formatValue(runReboot.getHour())+":"+formatValue(runReboot.getMinute());
        int diffTime = (int)(tsRunReboot.getTime() - tsNow.getTime()) / 1000;
        int i = diffTime;
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        int x = (int) screenSize.getWidth() - 260;
        int y = (int) screenSize.getHeight() - 150;
        
        Dialog dialog = new Dialog(((Window) null), "Neustart gefordert");
        dialog.setAlwaysOnTop(true);
        dialog.setLayout(new java.awt.FlowLayout());
        dialog.setLocation(x, y);
        dialog.setBounds(x, y, 260, 120);

        ActionListener alHide = new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                dialog.setVisible(false);
                try {
                    TimeUnit.SECONDS.sleep(60 * 30);
                } catch (InterruptedException ex) { System.out.println(ex); }
                dialog.setVisible(true);
            }
        };
        ActionListener alReboot = new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                runTask();
            }
        };

        try {
            Runtime.getRuntime().exec("schtasks /create /sc OnStart /ru system /tn force_reboot_remove /tr \"schtasks.exe /delete /tn force_reboot /f\"");
            Runtime.getRuntime().exec("schtasks /create /sc Once /sd \""+startDate+"\" /st \""+startTime+"\" /tn force_reboot /ru system /tr \"shutdown -r\"");
            //Runtime.getRuntime().exec("schtasks /create /sc HOURLY /mo "+dauer+" /tn force_reboot /ru system /tr \"shutdown -r\"");
        } catch (IOException e) { System.out.println(e); }

        Label label = new Label("Der Rechner wird in 0"+dauer+":00:00 neu gestartet");
        JProgressBar progressBar = new JProgressBar();
        Button btnReboot = new Button("jetzt neustarten");
        Button btnRemeber = new Button("in 30 Minuten erinnern");

        btnRemeber.addActionListener(alHide);
        btnReboot.addActionListener(alReboot);
        
        progressBar.setMinimum(0);
        progressBar.setMaximum(diffTime);
        progressBar.setValue(diffTime);
        
        dialog.add(label);
        dialog.add(progressBar);
        dialog.add(btnReboot);
        dialog.add(btnRemeber);

        dialog.setVisible(true);

        while (i > 0) {
            currentDateAndTime = LocalDateTime.now();
            tsNow = Timestamp.valueOf(currentDateAndTime);
            i = (int)(tsRunReboot.getTime() - tsNow.getTime()) / 1000;
            int hour = i / 3600;
            int remain = i - hour * 3600;
            int min = remain / 60;
            remain = remain - min * 60;
            int sec = remain;
            label.setText("Der Rechner wird in 0" + hour + ":" + formatValue(min) + ":" + formatValue(sec) + " neu gestartet");
            progressBar.setValue(i);
            if(i < 60*30 && !dialog.isVisible()){
                dialog.setVisible(true);
                btnRemeber.setEnabled(false);;
            }
            try {
                Thread.sleep(1000L); // 1000L = 1000ms = 1 second
            } catch (InterruptedException e) { }
        }
        runTask();
    }
}