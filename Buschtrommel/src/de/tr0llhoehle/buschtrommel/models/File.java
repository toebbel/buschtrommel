package de.tr0llhoehle.buschtrommel.models;

public class File {
	
	private String hash;
	private long length;
	private int ttl;
	private String displayName;
	private String meta;
	
	
	
	private String getHash() {
		return hash;
	}
	
	private long getLength() {
		return length;
	}

	private int getTTL() {
		return ttl;
	}
	
	private void setTTL(int ttl) {
		this.ttl = ttl;
	}
	
	private String getDisplayName() {
		return displayName;
	}
	
	private String getMeta() {
		return meta;
	}
}
