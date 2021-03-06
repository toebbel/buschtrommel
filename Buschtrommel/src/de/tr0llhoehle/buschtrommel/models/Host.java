package de.tr0llhoehle.buschtrommel.models;

import java.util.Date;
import java.util.Hashtable;
import java.util.logging.Logger;

import de.tr0llhoehle.buschtrommel.LoggerWrapper;

public class Host {
	public static final int UNKNOWN_PORT = -1;
	private Date lastSeen;
	private Date firstSeen;
	private java.net.InetAddress address;
	private String displayName;
	private Hashtable<String, ShareAvailability> shares;
	private int port;
	private Logger logger;
	
	/**
	 * Creates an instance of Host with an emtpy share
	 * @param address address of the host (IPv4 / IPv6)
	 * @param displayName human readable Displayname. Can be empty (IP address will be used). Message and Field seperator will be replaced with spaces.
	 */
	public Host(java.net.InetAddress address, String displayName, int port) {
		logger = java.util.logging.Logger.getLogger(this.getClass().getName());
		firstSeen = new Date();
		lastSeen = firstSeen;
		this.port = port;
		this.address = address;
		this.displayName = displayName.replace(Message.FIELD_SEPERATOR, ' ').replace(Message.MESSAGE_SPERATOR, ' ');
		if(this.displayName.trim().equals(""))
			this.displayName = address.toString();
		shares = new Hashtable<String, ShareAvailability>();
	}
	
	public Date getLastSeen() {
		return lastSeen;
	}
	
	public Date getFirstSeen() {
		return firstSeen;
	}
	
	public String getDisplayName() {
		return displayName;
	}
	
	public java.net.InetAddress getAddress() {
		return address;
	}
	
	/**
	 * Returns a *clone* of the share-table
	 * @return clone.
	 */
	@SuppressWarnings("unchecked")
	public Hashtable<String, ShareAvailability> getSharedFiles() {
		return (Hashtable<String, ShareAvailability>) shares.clone();
	}
	
	/**
	 * Adds or replaces a offered file of this host
	 * @param f the file to add
	 */
	public void addFileToSharedFiles(ShareAvailability share) {
		logger.info("Add file '" + share.getFile() + "' to " + this.toString());
		shares.put(share.getFile().getHash(), share);
	}
	
	/**
	 * Removes a file from the currently offered files of this host.
	 * @param hash hash of the file in upper-case and without leading 0x.
	 */
	public void removeFileFromSharedFiles(String hash) {
		logger.info("Remove file with hash '" + hash + "' from " + this.toString());
		shares.remove(hash);
	}
	
	/**
	 * Updates the LastSeen field to current time
	 */
	public void Seen() {
		logger.info("Seen host " + this.toString());
		lastSeen = new Date();
	}
	
	@Override
	public String toString() {
		return "Host " + address.toString() + " (" + displayName + ")";
	}
	
	@Override
	public boolean equals(Object obj) {
		if(!(obj instanceof Host))
			return false;
		return ((Host) obj).getAddress().equals(address);
	}

	public int getPort() {
		return port;
	}

	public void setDisplayName(String displayName) {
		this.displayName = displayName;
	}

	public void setPort(int port) {
		this.port = port;
	}
}
