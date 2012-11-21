package org.ender.updater;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class UpdaterConfig {
    private static final String EXTRACT = "extract";
    private static final String ITEM = "item";
    private static final String ARCH = "arch";
    private static final String OS = "os";
    private static final String FILE = "file";
    private static final String LINK = "link";
    
    public String mem, res, server, jar;

    List<Item> items = new ArrayList<UpdaterConfig.Item>();

    public UpdaterConfig(File file){
	check_config(file);

	DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
	DocumentBuilder builder;
	try {
	    builder = factory.newDocumentBuilder();
	    Document doc = builder.parse(file);
	    
	    NamedNodeMap attrs = doc.getDocumentElement().getAttributes();
	    mem = attrs.getNamedItem("mem").getNodeValue();
	    res = attrs.getNamedItem("res").getNodeValue();
	    server = attrs.getNamedItem("server").getNodeValue();
	    jar = attrs.getNamedItem("jar").getNodeValue();

	    NodeList groupNodes = doc.getElementsByTagName(ITEM);
	    for (int i = 0; i < groupNodes.getLength(); i++) {
		Item itm = parseItem(groupNodes.item(i));
		if (itm != null)
		    items.add(itm);
	    }
	} catch (Exception e) {
	    e.printStackTrace();
	}
    }

    private void check_config(File file) {
	if(!file.exists()){
	    try {
		FileOutputStream out = new FileOutputStream(file);
		InputStream in = UpdaterConfig.class.getResourceAsStream("/config.xml");

		int k = 512;
		byte[] b = new byte[512];
		while(k>0){
		    k = in.read(b, 0, 512);
		    if(k>0){
			out.write(b, 0, k);
		    }
		}
		out.close();
		in.close();
	    } catch (FileNotFoundException e) {
	    } catch (IOException e) {
	    }
	}
    }

    private Item parseItem(Node node) {
	Item itm = new Item();
	if (node.getNodeType() != Node.ELEMENT_NODE)
	    return null;
	Element el = (Element) node;

	itm.link = el.getAttribute(LINK);
	itm.file = el.getAttribute(FILE);
	itm.os = el.getAttribute(OS);
	itm.arch = el.getAttribute(ARCH);
	itm.extract = el.getAttribute(EXTRACT);
	return itm;
    }

    public static class Item{
	public String arch;
	public String os;
	public String file;
	public String link;
	public long date = 0;
	public long size = 0;
	public String extract = null;

    }
}
