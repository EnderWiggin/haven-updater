package org.ender.updater;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import org.ender.updater.UpdaterConfig.Item;

public class Updater {
    public UpdaterConfig cfg;
    private IUpdaterListener listener;
    public Updater(IUpdaterListener listener){
	this.listener = listener;
	cfg = new UpdaterConfig();
    }

    public void update() {
	(new Thread(new Runnable() {

	    @Override
	    public void run() {
		List<Item> update = new ArrayList<UpdaterConfig.Item>();
		for(Item item : cfg.items){
		    if(!correct_platform(item)){continue;}
		    set_date(item);
		    if(has_update(item)){
			listener.log(String.format("Updates found for '%s'", item.file));
			update.add(item);
		    } else {
			listener.log(String.format("No updates for '%s'", item.file));
		    }
		}
		for (Item item: update){
		    download(item);
		    if(item.extract.length() > 0){
			extract(item);
		    }
		}
		
		listener.fisnished();
	    }
	})).start();
    }

    private boolean correct_platform(Item item) {
	String os = System.getProperty("os.name");
	String arch = System.getProperty("os.arch");
	return (os.indexOf(item.os) >= 0) && (arch.equals(item.arch) || item.arch.length() == 0);
    }

    private void set_date(Item item) {
	File file = new File(item.file);
	if(file.exists()){ 
	    item.date = file.lastModified();
	}
    }

    private boolean has_update(Item item) {
	try {
	    URL  url = new URL(item.link);
	    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
	    conn.setRequestMethod("HEAD");
	    conn.setIfModifiedSince(item.date);
	    try {
		if(conn.getResponseCode() == HttpURLConnection.HTTP_OK){
		    item.size = Long.parseLong(conn.getHeaderField("Content-Length"));
		    return true;
		}
	    } catch(NumberFormatException e){}
	    conn.disconnect();
	} catch (MalformedURLException e) {
	    e.printStackTrace();
	} catch (IOException e) {
	    e.printStackTrace();
	}
	return false;
    }

    private void download(Item item) {
	listener.log(String.format("Downloading '%s'", item.file));
	URL link;
	try {
	    link = new URL(item.link);
	    ReadableByteChannel rbc = Channels.newChannel(link.openStream());
	    FileOutputStream fos = new FileOutputStream(item.file);
	    long position = 0;
	    int step = 20480;
	    listener.progress(position, item.size);
	    while(position < item.size){
		position += fos.getChannel().transferFrom(rbc, position, step);
		listener.progress(position, item.size);
	    }
	    listener.progress(0, item.size);
	    fos.close();
	} catch (MalformedURLException e) {
	    e.printStackTrace();
	} catch (IOException e) {
	    e.printStackTrace();
	}
    }

    private void extract(Item item) {
	listener.log(String.format("Unpacking '%s'", item.file));
	try {
	    ZipFile zip;
	    zip = new ZipFile(item.file);
	    Enumeration<? extends ZipEntry> contents=zip.entries();
	    while (contents.hasMoreElements()) {
		ZipEntry file=(ZipEntry)contents.nextElement();
		String name = file.getName();
		if(name.indexOf("META-INF") == 0){continue;}
		listener.log("\t"+name);
		ReadableByteChannel rbc = Channels.newChannel(zip.getInputStream(file));
		FileOutputStream fos = new FileOutputStream(item.extract+File.separatorChar+name);
		long position = 0;
		long size = file.getSize();
		int step = 20480;
		while(position < size){
		    position += fos.getChannel().transferFrom(rbc, position, step);
		}
		fos.close();
	    }
	} catch (IOException e) {
	    e.printStackTrace();
	}
    }
}