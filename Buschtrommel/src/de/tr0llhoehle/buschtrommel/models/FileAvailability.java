package de.tr0llhoehle.buschtrommel.models;

public class FileAvailability {
	
	
	public static int TTL_DEFAULT;

	private Host host;
	private RemoteShare file;
	private int ttl;
	private String displayName;
	private String meta;
	
	public FileAvailability(Host host, RemoteShare file, int ttl, String displayName, String meta) {
		this.host = host;
		this.file = file;
		this.ttl = ttl;
		
		
		displayName = displayName.replace(Message.FIELD_SEPERATOR, ' ');
		displayName = displayName.replace(Message.MESSAGE_SPERATOR, ' ');
		meta = meta.replace(Message.FIELD_SEPERATOR, ' ');
		meta = meta.replace(Message.MESSAGE_SPERATOR, ' ');
		
		this.displayName = displayName;
		this.meta = meta;
		
		if (!(ttl >= 0 || ttl == Share.TTL_INFINITY))
			throw new IllegalArgumentException("TTL is invalid (" + ttl + ")");
		
	}

	public Host getHost() {
		return host;
	}

	public RemoteShare getFile() {
		return file;
	}

	public int getTtl() {
		return ttl;
	}
	
	/**
	 * Sets the TTL to given value. Must be >= 0 or Infinity
	 * 
	 * @param ttl
	 *            new ttl
	 */
	public void setTTL(int ttl) {
		if (!(ttl >= 0 || ttl == Share.TTL_INFINITY))
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
