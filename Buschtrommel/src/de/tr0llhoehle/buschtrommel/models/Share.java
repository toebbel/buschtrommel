package de.tr0llhoehle.buschtrommel.models;

public class Share extends File {

	private String path;
	
	public Share(String hash, long length, int ttl, String displayName, String meta, String path) {
		super(hash, length, ttl, displayName, meta);
		this.path = path;
	}
	
	public String getPath(){
		return this.path;
	}

}
