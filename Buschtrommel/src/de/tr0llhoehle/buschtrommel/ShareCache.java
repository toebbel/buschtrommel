package de.tr0llhoehle.buschtrommel;

import java.beans.XMLDecoder;
import java.beans.XMLEncoder;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Iterator;

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

	protected Hashtable<String, Share> shares;

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
		Enumeration<Share> e = shares.elements();
		while (e.hasMoreElements()) {
			allShares.append(new FileAnnouncementMessage(e.nextElement()).Serialize());
		}

		return allShares.toString();
	}

	/**
	 * 
	 * @param share
	 */
	public void newShare(Share share) {
		if (share == null) {
			return;
		}
		// String hash = share.getHash();
		if (this.has(share.getHash())) {
			LoggerWrapper.logError("A Share with the given Hash: " + share.getHash()
					+ "has already be defined. It is now replaced with the new Share");
			this.shares.remove(share.getHash());
			this.shares.put(share.getHash(), share);
		}

	}

	public void saveToFile(String path) {
		if (path == null || !path.endsWith(".xml")) {
			LoggerWrapper.logError("the given path: " + path + " is not valid (must end with .xml)");
		}
		FileOutputStream fos;
		try {
			fos = new FileOutputStream(path);
			XMLEncoder e = new XMLEncoder(fos);
			e.writeObject(shares);
			e.close();
		} catch (FileNotFoundException e1) {
			LoggerWrapper.logError("the given path: " + path + " is not valid");

		}

	}

	@SuppressWarnings("unchecked")
	public void restoreFromFile(String path) {
		FileInputStream fis;
		try {
			fis = new FileInputStream(path);
			XMLDecoder r = new XMLDecoder(fis);
			Hashtable<String, Share> temp_shares;
			temp_shares = ((Hashtable<String, Share>) r.readObject());
			r.close();
			shares = temp_shares;
		} catch (FileNotFoundException e) {
			LoggerWrapper.logError("File not found: " + path);
		}

	}
}
