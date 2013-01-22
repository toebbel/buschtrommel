package de.tr0llhoehle.buschtrommel;

import de.tr0llhoehle.buschtrommel.models.ShareAvailability;
import de.tr0llhoehle.buschtrommel.models.Host;
import de.tr0llhoehle.buschtrommel.network.ITransferProgress;

/**
 * These are the callbacks that can be made, so the GUI can update in realtime
 *
 */
public interface IGUICallbacks {
	
	/**
	 * Whenever a host has been discovered
	 * @param host the new discovered host
	 */
	public void newHostDiscovered(Host host);
	
	/**
	 * Whenever a host goes offline
	 * @param host the host that went offline
	 */
	public void hostWentOffline(Host host);
	
	/**
	 * Whenever the ttl of a remoteShare expires
	 * @param file the concerning file
	 */
	public void removeShare(ShareAvailability file);
	
	/**
	 * 
	 * @param transfer
	 */
	public void newOutgoingTransferStarted(ITransferProgress transfer);
	
	
	/**
	 * A new file is available. It may was announced via UDP or was discovered via GetFileList
	 * @param file the file that is newly available
	 */
	public void newShareAvailable(ShareAvailability file);
	
	/**
	 * Whenever a ttl of any share has been updated by the update-timer
	 * @param file the update dile
	 */
	public void updatedTTL(ShareAvailability file);
}
