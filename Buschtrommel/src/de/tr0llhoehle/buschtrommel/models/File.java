package de.tr0llhoehle.buschtrommel.models;

public class File {

	public static int TTL_INFINITY = -1;

	private String hash;
	private long length;
	private int ttl;
	private String displayName;
	private String meta;

	/**
	 * Creates an instance of file that is eigher local or on a remote client
	 * 
	 * @param hash
	 *            the hash of the file as uppercase String, without leading 0x
	 * @param length
	 *            number of bytes in the file
	 * @param ttl
	 *            TimeToLive in seconds.
	 * @param displayName
	 *            human readable string that represents the filename
	 * @param meta
	 *            any information about the file (description or path)
	 */
	public File(String hash, long length, int ttl, String displayName,
			String meta) {
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
}
