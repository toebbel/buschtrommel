package de.tr0llhoehle.buschtrommel.models;

import java.util.Vector;


public class File {

	public static int TTL_INFINITY = -1;

	private String hash;
	private long length;
	private int ttl;
	private String displayName;
	private String meta;
	private Vector<Host> sources;

	/**
	 * Creates an instance of file that is either local or on a remote client
	 * 
	 * @param hash
	 *            the hash of the file as uppercase String, without leading 0x
	 * @param length
	 *            number of bytes in the file. Has to be >= 0
	 * @param ttl
	 *            TimeToLive in seconds. Has to be >= 0 or TTL_INFINITY
	 * @param displayName
	 *            human readable UTF8-string that represents the filename. MUST NOT contain any character with ascii code < 32 (space). Any of these characters will be replaced with spaces
	 * @param meta
	 *            any information about the file (description or path) as UTF8-String. MUST NOT contain any character with ascii code < 32 (space). Any of these characters will be replaced with spaces
	 */
	public File(String hash, long length, int ttl, String displayName,
			String meta) {
		
		displayName = displayName.replace(Message.FIELD_SEPERATOR, ' ');
		displayName = displayName.replace(Message.MESSAGE_SPERATOR, ' ');
		meta = meta.replace(Message.FIELD_SEPERATOR, ' ');
		meta = meta.replace(Message.MESSAGE_SPERATOR, ' ');
		
		if (!(ttl >= 0 || ttl == TTL_INFINITY))
			throw new IllegalArgumentException("TTL is invalid (" + ttl + ")");
		if(length < 0)
			throw new IllegalArgumentException("Length is < 0");
		
		this.hash = hash;
		this.length = length;
		this.ttl = ttl;
		this.displayName = displayName;
		this.meta = meta;
	}

	public String getHash() {
		return hash;
	}

	public long getLength() {
		return length;
	}

	public int getTTL() {
		return ttl;
	}

	/**
	 * Sets the TTL to given value. Must be >= 0 or Infinity
	 * 
	 * @param ttl
	 *            new ttl
	 */
	public void setTTL(int ttl) {
		if (!(ttl >= 0 || ttl == TTL_INFINITY))
			throw new IllegalArgumentException("TTL is invalid (" + ttl + ")");
		this.ttl = ttl;
	}

	public String getDisplayName() {
		return displayName;
	}

	public String getMeta() {
		return meta;
	}
	
	public Vector<Host> getSources() {
		return this.sources;
	}
	
	public void addHost(Host host) {
		this.sources.add(host);
	}
}
