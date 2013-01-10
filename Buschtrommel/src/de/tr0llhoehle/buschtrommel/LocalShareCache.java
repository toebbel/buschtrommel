package de.tr0llhoehle.buschtrommel;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Hashtable;
import de.tr0llhoehle.buschtrommel.models.FileAnnouncementMessage;
import de.tr0llhoehle.buschtrommel.models.LocalShare;
import de.tr0llhoehle.buschtrommel.models.Message;
import de.tr0llhoehle.buschtrommel.models.Share;
import de.tr0llhoehle.buschtrommel.network.MessageDeserializer;

/**
 * All the Shared Files from the user are stored in this class
 * 
 * 
 * 
 * @author benjamin
 * 
 */
public class LocalShareCache {

	protected Hashtable<String, LocalShare> shares = new Hashtable<>();
	private int defaultTTL;


	/**
	 * 
	 * @param hash
	 *            the SHA-1 Hash of the file
	 * @return true, if file is known - else false
	 */
	public boolean has(String hash) {
		if (hash != null) {
			return shares.containsKey(hash);
		} else
			return false;

	}

	/**
	 * returns the Share to a given hash
	 * 
	 * @param hash
	 *            a hash
	 * @return the Share to a given hash or null, if no such share is known
	 */

	public LocalShare get(String hash) {
		if (hash != null) {
			return this.shares.get(hash);
		}
		return null;

	}

	/**
	 * creates a FileAnnouncement message with all shares
	 * 
	 * @return a FileAnnouncement, when there are no Shares a empty String is
	 *         returned
	 */
	public String getAllShares() {
		StringBuilder allShares = new StringBuilder("");

		ArrayList<LocalShare> shareList = new ArrayList<LocalShare>(shares.values());

		for (LocalShare i : shareList) {
			allShares.append((new FileAnnouncementMessage(i).Serialize()));
		}

		return allShares.toString();
	}

	/**
	 * 
	 * @param share
	 */
	public void newShare(LocalShare share) {
		if (share == null) {
			LoggerWrapper.logError("no share given");
			return;
		}
		// String hash = share.getHash();
		if (this.has(share.getHash())) {
			LoggerWrapper.logError("A Share with the given Hash: " + share.getHash()
					+ "has already be defined. It is now replaced with the new Share");
			this.shares.remove(share.getHash());

		}
		// LoggerWrapper.logInfo("set hash: " + share.getHash());
		this.shares.put(share.getHash(), share);
		// LoggerWrapper.logInfo("file has been added");

	}

	protected boolean convertToShares(String shares) {
		if (shares == null) {
			return false;
		}
		Hashtable<String, LocalShare> temp_shares = new Hashtable<String, LocalShare>();
		// TODO
		String[] temp = shares.split("\n");
		for (String i : temp) {
			// TODO deserialize
			LoggerWrapper.logError("Not yet implemented");

			// temp_shares.put("hash", "share");
		}

		return true;

	}

	protected String convertSharesToString() {
		StringBuilder allShares = new StringBuilder();

		ArrayList<LocalShare> shareList = new ArrayList<LocalShare>(shares.values());

		for (LocalShare i : shareList) {
			if(i.getTTL() == Share.TTL_INFINITY)
				allShares.append((new FileAnnouncementMessage(i).Serialize()) + new java.io.File(i.getPath()).getAbsolutePath() + "\n");
		}

		return allShares.toString();
	}

	/**
	 * saves the actual shares to disk
	 * 
	 * @param path
	 *            a path to an binary ht-file
	 * @throws IllegalArgumentException
	 *             if the path is invalid (must end with .ht)
	 * @return true if shares could be saved to file
	 */
	public boolean saveToFile(String path) {
		LoggerWrapper.logInfo("Write all shares into " + path);
		if (path == null || !path.endsWith(".ht")) {
			throw new IllegalArgumentException("the given path: " + path + " is not valid (must end with .ht)");
		}
		
		try {
			FileWriter writer = new FileWriter(path, false);
			writer.write(convertSharesToString());
			writer.close();
		} catch (FileNotFoundException e1) {
			LoggerWrapper.logError("the given path: " + path + " is not valid");
			return false;
		} catch (IOException e) {
			LoggerWrapper.logError("Could not write to file '" + path + "' - " + e.getMessage());
			return false;
		}
		return true;
	}

	/**
	 * reads shares from a file
	 * 
	 * @param path
	 *            path to a ht-file
	 */
	public boolean restoreFromFile(String path) {
		if (path == null || !path.endsWith(".ht")) {
			throw new IllegalArgumentException("the given path: " + path + " is not valid (must end with .ht)");
		}
		BufferedReader reader;
		try {
			reader = new BufferedReader(new FileReader(path));
			int lineCount = 1;
			while (reader.ready()) {
				// split into parseable part and path
				String[] line = reader.readLine().split(String.valueOf(Message.MESSAGE_SPERATOR));
				if (line.length != 2) {
					LoggerWrapper.logError("Could not parse line " + line + " in shareCache file");
					lineCount++;
					continue;
				}
				lineCount++;

				// check if file is still there
				if (!(new java.io.File(line[1]).exists())) {
					LoggerWrapper.logError("The file '" + line[1]
							+ "' is not available any more. Removed it from ShareCache");
					continue;
				}

				// check if part 1 parseable
				Message m = MessageDeserializer.Deserialize(line[0] + Message.MESSAGE_SPERATOR); //has to append seperator because deserializer needs this and we cut it off.
				if (m == null || !(m instanceof FileAnnouncementMessage)) {
					LoggerWrapper.logError("Could not parse line " + line + ": " + line[0]);
					continue;
				}

				// create share from this information
				LocalShare s = ((FileAnnouncementMessage) m).getFile();
				s = new LocalShare(s.getHash(), s.getLength(), s.getTTL(), s.getDisplayName(), s.getMeta(), line[1]); // TODO
																												// check
																												// if
																												// length
																												// is
																												// still
																												// correct
				newShare(s);
			}
		} catch (IOException e) {
			LoggerWrapper.logError("Could not read ShareCache: " + e.getMessage());
		}
		return true;
	}
}
