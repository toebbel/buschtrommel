package de.tr0llhoehle.buschtrommel.models;

public abstract class Share {

	public static int TTL_INFINITY = -1;
	
	private String hash;
	private long length;	

	public Share(String hash, long length) {
		
		
		if(length < 0)
			throw new IllegalArgumentException("Length is < 0");
		
		this.hash = hash;
		this.length = length;

	}
	
	public String getHash() {
		return hash;
	}

	public long getLength() {
		return length;
	}
	

	
}
