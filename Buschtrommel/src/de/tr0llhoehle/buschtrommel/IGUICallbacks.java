package de.tr0llhoehle.buschtrommel;

import de.tr0llhoehle.buschtrommel.models.RemoteShare;
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
	public void NewHostDiscovered(Host host);
	
	/**
	 * Whenever a host goes offline
	 * @param host the host that went offline
	 */
	public void HostWentOffline(Host host);
	
	/**
	 * A new file is available. It may was announced via UDP or was discovered via GetFileList
	 * @param file
	 */
	public void NewFileAvailable(RemoteShare file);
	
	/**
	 * Whenever a transfer was completed successful
	 * @param transferProgress the transfer that succeeded
	 */
	public void FileTransferComplete(ITransferProgress transferProgress);
	
	/**
	 * Whenever a transfer failed and needs user interaction
	 * @param transferProgress the failed transfer
	 */
	public void FileTransferFailed(ITransferProgress transferProgress);
}
