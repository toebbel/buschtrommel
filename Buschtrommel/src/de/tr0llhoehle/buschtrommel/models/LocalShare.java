package de.tr0llhoehle.buschtrommel.models;

public class LocalShare extends Share {

	private String path;
	private int ttl;
	private String displayName;
	private String meta;

	public LocalShare(String hash, long length, int ttl, String displayName, String meta, String path) {
		super(hash, length);
		
		displayName = displayName.replace(Message.FIELD_SEPERATOR, ' ');
		displayName = displayName.replace(Message.MESSAGE_SPERATOR, ' ');
		meta = meta.replace(Message.FIELD_SEPERATOR, ' ');
		meta = meta.replace(Message.MESSAGE_SPERATOR, ' ');
		
		this.path = path;
		this.displayName = displayName;
		this.meta = meta;
		if (!(ttl >= 0 || ttl == TTL_INFINITY))
			throw new IllegalArgumentException("TTL is invalid (" + ttl + ")");
		this.ttl = ttl;
	}

	public String getPath() {
		return this.path;
	}
	
	public int getTTL() {
		return this.ttl;
	}
	
	public void setTTL(int newTTL) {
		assert newTTL >= 0 || newTTL == TTL_INFINITY;
		this.ttl = newTTL;
	}
	
	public String getDisplayName() {
		return displayName;
	}

	public String getMeta() {
		return meta;
	}
	
	@Override
	public boolean equals(Object obj) {
		if(!(obj instanceof LocalShare))
			return false;
		return ((LocalShare)obj).getHash() == getHash();
	}
	

}
