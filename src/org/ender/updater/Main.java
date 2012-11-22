package org.ender.updater;

import java.io.File;
import java.io.IOException;

import javax.swing.BoxLayout;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JTextArea;


public class Main extends JFrame implements IUpdaterListener{
    private static final int PROGRESS_MAX = 1024;
    private static final long serialVersionUID = 1L;
    private static Updater updater;

    public static void main(String[] args) {
	try {
	    javax.swing.UIManager.setLookAndFeel(javax.swing.UIManager.getSystemLookAndFeelClassName());
	} catch(Exception e) {}
	Main gui = new Main();
	gui.setVisible(true);
	gui.setSize(350, 450);
	gui.log(String.format("OS: '%s', arch: '%s'", System.getProperty("os.name"), System.getProperty("os.arch")));
	gui.log("Checking for updates...");

	updater = new Updater(gui);
	updater.update();
    }

    private JTextArea logbox;
    private JProgressBar progress;

    public Main(){
	super("Salem updater");
	setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	JPanel p;
	add(p = new JPanel());
	p.setLayout(new BoxLayout(p, BoxLayout.PAGE_AXIS));
	
	p.add(logbox = new JTextArea());
	logbox.setEditable(false);
	
	p.add(progress = new JProgressBar());
	progress.setMinimum(0);
	progress.setMaximum(PROGRESS_MAX);
	pack();
    }

    @Override
    public void log(String message) {
	logbox.append(message+"\n");
    }

    @Override
    public void fisnished() {
	log("Starting client...");
	String libs = String.format("-Djava.library.path=%%PATH%%%s.", File.pathSeparator);
	UpdaterConfig cfg = updater.cfg;
	ProcessBuilder pb = new ProcessBuilder("java", "-Xmx"+cfg.mem, libs, "-jar", cfg.jar, "-U", cfg.res, cfg.server);
	pb.directory(UpdaterConfig.dir);
	try {
	    pb.start();
	} catch (IOException e) {
	    e.printStackTrace();
	}
	//System.exit(0);
    }

    @Override
    public void progress(long position, long size) {
	progress.setValue((int) (PROGRESS_MAX * ((float) position / size)));
    }


}
