package de.tr0llhoehle.buschtrommel;

import java.beans.XMLDecoder;
import java.beans.XMLEncoder;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;

/**
 * This class is a container for all settings
 * 
 * @author Tobias Sturm
 * 
 */
public class Config {

	private static Config instance;
	private Properties prop;

	public static int defaultTTL;
	public static int maximumYoResponseTime; // in milliseconds
	public static int TTLRenewalTimer;
	public static String shareCachePath;
	public static String alias;
	public static long minDiscoveryMulticastIddle; // min. time between two
													// broadcast-YO messages
	public static boolean useIPv4, useIPv6;
	public static int FileReannounceGraceTime; // time in sec before a ttl of a
												// localshare is over -> send it
												// to buschtrommel to reannounce

	private Config() {
		this.prop = new Properties();
	}

	public synchronized static Config getInstance() {
		if (instance == null) {
			instance = new Config();
		}
		return instance;
	}

	public void readFromFile(String path) throws IOException {
		// java.beans.XMLDecoder decoder = new XMLDecoder(new
		// FileInputStream(f));
		// Config result = (Config) decoder.readObject();
		// decoder.close();
		// return result;

		this.prop.load(new FileInputStream(path));
		defaultTTL = Integer.parseInt(prop.getProperty("defaultTTL"));
		maximumYoResponseTime = Integer.parseInt(prop.getProperty("maximumYoResponseTime"));
		TTLRenewalTimer = Integer.parseInt(prop.getProperty("TTLRenewalTimer"));
		shareCachePath = prop.getProperty("shareCachePath");
		alias = prop.getProperty("alias");
		minDiscoveryMulticastIddle = Long.parseLong(prop.getProperty("minDiscoveryMulticastIddle"));
		useIPv4 = prop.getProperty("useIPv4").equals("true");
		useIPv6 = prop.getProperty("useIPv6").equals("true");
		FileReannounceGraceTime = Integer.parseInt(prop.getProperty("FileReannounceGraceTime"));
	}

	public void saveToFile(String path) throws IOException {
		// java.beans.XMLEncoder encoder = new XMLEncoder(new
		// FileOutputStream(f, false));
		// encoder.writeObject(instance);
		// encoder.close();

		this.prop.setProperty("defaultTTL", Integer.toString(defaultTTL));
		this.prop.setProperty("maximumYoResponseTime", Integer.toString(maximumYoResponseTime));
		this.prop.setProperty("TTLRenewalTimer", Integer.toString(TTLRenewalTimer));
		if(shareCachePath != null) this.prop.setProperty("shareCachePath", shareCachePath);
		if(alias != null) this.prop.setProperty("alias", alias);
		this.prop.setProperty("minDiscoveryMulticastIddle", Long.toString(minDiscoveryMulticastIddle));
		this.prop.setProperty("useIPv4", Boolean.toString(useIPv4));
		this.prop.setProperty("useIPv6", Boolean.toString(useIPv6));
		this.prop.setProperty("FileReannounceGraceTime", Integer.toString(FileReannounceGraceTime));
		
		this.prop.store(new FileOutputStream(path), null);
	}
}
