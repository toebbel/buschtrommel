package de.tr0llhoehle.buschtrommel;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Hashtable;
import de.tr0llhoehle.buschtrommel.models.FileAnnouncementMessage;
import de.tr0llhoehle.buschtrommel.models.Share;

/**
 * All the Shared Files from the user are stored in this class
 * 
 * 
 * 
 * @author benjamin
 * 
 */
public class ShareCache {

	protected Hashtable<String, Share> shares = new Hashtable<>();

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

	public Share get(String hash) {
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

		ArrayList<Share> shareList = new ArrayList<Share>(shares.values());

		for (Share i : shareList) {
			allShares.append((new FileAnnouncementMessage(i).Serialize()));
		}

		return allShares.toString();
	}

	/**
	 * 
	 * @param share
	 */
	public void newShare(Share share) {
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

	protected void convertToShares(String shares) {
		Hashtable<String, Share> temp_shares = new Hashtable<>();
		// TODO

	}

	protected String convertSharesToString() {
		StringBuilder allShares = new StringBuilder("");

		ArrayList<Share> shareList = new ArrayList<Share>(shares.values());

		for (Share i : shareList) {
			allShares.append((new FileAnnouncementMessage(i).Serialize()) + i.getPath() + "\n");
		}

		return allShares.toString();
	}

	/**
	 * saves the actual shares to disk
	 * 
	 * @param path
	 *            a path to an binary ht-file
	 */
	public void saveToFile(String path) {
		if (path == null || !path.endsWith(".ht")) {
			LoggerWrapper.logError("the given path: " + path + " is not valid (must end with .ht)");
			return;
		}
		FileOutputStream fos;
		try {
			fos = new FileOutputStream(path);
			ObjectOutputStream oos = new ObjectOutputStream(fos);

			oos.writeObject(convertSharesToString());
			LoggerWrapper.logInfo("All Shares written to file: " + path);
			oos.close();
		} catch (FileNotFoundException e1) {
			LoggerWrapper.logError("the given path: " + path + " is not valid");

		} catch (IOException e) {
			LoggerWrapper.logError("the given path: " + path + " is not valid");

		}

	}

	/**
	 * reads shares from a file
	 * 
	 * @param path
	 *            path to a ht-file
	 */
	@SuppressWarnings("unchecked")
	public void restoreFromFile(String path) {
		if (path == null || !path.endsWith(".ht")) {
			LoggerWrapper.logError("the given path: " + path + " is not valid (must end with .ht)");
			return;
		}

		LoggerWrapper.logError("Not implemented");
		this.shares = new Hashtable<>();
		return;
		//
		// FileInputStream fis;
		// try {
		// fis = new FileInputStream(path);
		// ObjectInputStream ois = new ObjectInputStream(fis);
		//
		// // XMLDecoder r = new XMLDecoder(fis);
		// Hashtable<String, Share> temp_shares;
		// temp_shares = ((Hashtable<String, Share>) ois.readObject());
		// ois.close();
		// shares = temp_shares;
		// } catch (FileNotFoundException e) {
		// LoggerWrapper.logError("File not found: " + path);
		// } catch (ClassNotFoundException e) {
		// LoggerWrapper.logError("Class not found Exception - should never happen");
		// } catch (IOException e) {
		// LoggerWrapper.logError("Could not read file: " + path);
		// }

	}
}
