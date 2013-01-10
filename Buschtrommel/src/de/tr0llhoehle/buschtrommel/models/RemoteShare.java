package de.tr0llhoehle.buschtrommel.models;

import java.util.Vector;


public class RemoteShare extends Share {

	private Vector<ShareAvailability> sources;

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
	public RemoteShare(String hash, long length) {
		super(hash, length);		
	}
	
	public Vector<ShareAvailability> getSources() {
		return this.sources;
	}
	
	public ShareAvailability addFileSource(Host host, int ttl, String displayName, String meta) {
		ShareAvailability tmp = new ShareAvailability(host, this, ttl, displayName, meta);
		this.sources.add(tmp);
		return tmp;
	}
	
	public void removeFileSource(ShareAvailability fileAvailability) {
		this.sources.remove(fileAvailability);
	}
	
	public boolean noSourcesAvailable() {
		return this.sources.isEmpty();
	}
	
	/**
	 * Returns the highest ttl among all known sources
	 * @return the highest ttl
	 */
	public int getMaxTTL() {
		int ttl = 0;
		for(ShareAvailability tmp : this.sources) {
			if(tmp.getTtl() > ttl) {
				ttl = tmp.getTtl();
			}
		}
		return ttl;
	}
}
