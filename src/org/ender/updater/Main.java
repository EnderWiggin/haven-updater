package org.ender.updater;

import java.io.IOException;


public class Main {

    public static void main(String[] args) {
	System.out.println(String.format("OS version: %s\nOS name: %s\narch: %s",
	System.getProperty("os.version"),
	System.getProperty("os.name"),
	System.getProperty("os.arch")));
	
	Updater u = new Updater();
	
	ProcessBuilder pb = new ProcessBuilder("javaw", "-Xmx512m", "-jar", "salem.jar", "-U", "http://plymouth.seatribe.se/res/", "plymouth.seatribe.se");
	try {
	    pb.start();
	} catch (IOException e) {
	    e.printStackTrace();
	}
    }
    

}
