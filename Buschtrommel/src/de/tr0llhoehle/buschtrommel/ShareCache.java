package de.tr0llhoehle.buschtrommel;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.util.Hashtable;

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
	
	public String getAllShares(){
		StringBuilder allShares = new StringBuilder("");
		for (Share share : shares.elements()){
			
		}
		
		
		return allShares;
	}

	/**
	 * 
	 * @param share
	 */
	public void newShare(Share share){
		if(share == null){
			return;
		}
		String hash = share.getHash();
		if(this.has(share.getHash())){
			this.shares.remove(key)
		}
		
		
	}
}
