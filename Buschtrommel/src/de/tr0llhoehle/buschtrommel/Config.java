package de.tr0llhoehle.buschtrommel;

import java.beans.XMLDecoder;
import java.beans.XMLEncoder;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;

/**
 * This class is a container for all settings
 * 
 * @author Tobias Sturm
 *
 */
public class Config {
	
	public static int defaultTTL;
	public static int maximumYoResponseTime; //in milliseconds
	public static int TTLRenewalTimer;
	public static String shareCachePath;
	public static String alias;

	public static Config readFromFile(File f) throws FileNotFoundException {
		java.beans.XMLDecoder decoder = new XMLDecoder(new FileInputStream(f));
		Config result = (Config) decoder.readObject();
		decoder.close();
		return result;
	}
	
	public static void saveToFile(File f, Config c) throws FileNotFoundException {
		java.beans.XMLEncoder encoder = new XMLEncoder(new FileOutputStream(f, false));
		encoder.writeObject(c);
		encoder.close();
	}
}
