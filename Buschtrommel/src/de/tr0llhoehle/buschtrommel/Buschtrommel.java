package de.tr0llhoehle.buschtrommel;

import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.util.Hashtable;

import de.tr0llhoehle.buschtrommel.models.ByeMessage;
import de.tr0llhoehle.buschtrommel.models.FileAnnouncementMessage;
import de.tr0llhoehle.buschtrommel.models.LocalShare;
import de.tr0llhoehle.buschtrommel.models.RemoteShare;
import de.tr0llhoehle.buschtrommel.models.Host;
import de.tr0llhoehle.buschtrommel.models.Share;
import de.tr0llhoehle.buschtrommel.network.FileTransferAdapter;
import de.tr0llhoehle.buschtrommel.network.ITransferProgress;
import de.tr0llhoehle.buschtrommel.network.NetCache;
import de.tr0llhoehle.buschtrommel.network.UDPAdapter;

public class Buschtrommel {

	private IGUICallbacks guiCallbacks;
	private FileTransferAdapter fileTransferAdapter;
	private UDPAdapter udpAdapter;
	private NetCache netCache;
	private LocalShareCache shareCache;

	public Buschtrommel(IGUICallbacks gui) {
		if (!HashFuncWrapper.check()) {
			// cancel bootstrap: Hashfunction is not available!
			this.guiCallbacks = gui;
			this.netCache = new NetCache(this.udpAdapter, guiCallbacks);
		}
		this.guiCallbacks = gui;
		this.netCache = new NetCache(this.udpAdapter, this.guiCallbacks);
		this.shareCache = new LocalShareCache();
	}

	public void start() throws IOException {
		// TODO: create new FileTransferAdapter
		this.udpAdapter = new UDPAdapter();
		this.udpAdapter.registerObserver(netCache);
	}

	public void stop() throws IOException {
		this.sendByeMessage();

		// TODO: destroy fileTransferAdapter
		this.udpAdapter.closeConnection();
		this.udpAdapter.removeObserver(netCache);
		this.udpAdapter = null;
	}

	public ITransferProgress DownloadFile(String hash, Host host) {
		return null;
	}
	
	/***
	 * Downloads a file from a specific host
	 * @param hash hash of the file to download
	 * @param targetFile target file to save
	 * @param host the host to download this file from
	 * @return progress-indicator or null if the hash could not be resolved
	 */
	public ITransferProgress DownloadFile(String hash, String targetFile, Host host) {
		RemoteShare s = netCache.getShare(hash);
		if(s == null)
			return null;
		return fileTransferAdapter.DownloadFile(hash, host, s.getLength(), new java.io.File(targetFile));
	}
	
	/**
	 * Download a file from any available host.
	 * @param hash hash of the file to download
	 * @param targetFile target file to save
	 * @return progress-indicator or null if the hash could not be resolved
	 */
	public ITransferProgress DownloadFile(String hash, String targetFile) {
		RemoteShare s = netCache.getShare(hash);
		if(s == null)
			return null;
		return fileTransferAdapter.DownloadFile(hash, s.getHostList(), s.getLength(), new java.io.File(targetFile));
	}
	
	/**
	 * Creates a local share with the default TTL an announces it via multicast.
	 * @param path path to file to share
	 * @param dspName display name 
	 * @param meta meta information about the file
	 * @throws IOException when there was an error, generating the hash
	 * @throws IllegalArgumentException when the path is not a filepath or non-existent or when the TTL is invalid
	 */
	public void AddFileToShare(String path, String dspName, String meta) throws IOException, IllegalArgumentException {
		AddFileToShare(path, dspName, meta, Config.defaultTTL);
	}
	
	/**
	 * Creates a share and announces it via multicast
	 * @param path path to the file to share
	 * @param dspName display name
	 * @param meta meta information about the file
	 * @param ttl the time to live in seconds. -1 for infinity
	 * @throws IllegalArgumentException when the path is not a filepath or non-existent or when the TTL is invalid
	 */
	public void AddFileToShare(String path, String dspName, String meta, int ttl) throws IOException, IllegalArgumentException {
		File file = new File(path);
		if(!file.exists())
			throw new IllegalArgumentException("path does not exist!");
		if(!file.isFile())
			throw new IllegalArgumentException("path is not a file!");
		if(ttl < 0 && ttl != Share.TTL_INFINITY)
			throw new IllegalArgumentException("TTL is invalid");
		
		String hash = HashFuncWrapper.hash(path);
		LocalShare share = new LocalShare(hash, file.length(), ttl, dspName, meta, path);
		shareCache.newShare(share);
		udpAdapter.sendMulticast(new FileAnnouncementMessage(share));
	}

	/**
	 * Removes a file from the shares and announces, that it is not available any more via multicast
	 * @param file the file to remove
	 */
	public void RemoveFileFromShare(String hash) {
		LocalShare share = shareCache.get(hash);
		if(share == null)
			return;
		shareCache.remove(hash);
	}

	public Hashtable<String, ITransferProgress> getIncomingTransfers() {
		return null;
	}

	public Hashtable<InetAddress, ITransferProgress> getOutgoingTransfers() {
		return null;
	}

	public Hashtable<String, RemoteShare> getRemoteShares() {
		return this.netCache.getShares();
	}

	public Hashtable<InetAddress, Host> getHosts() {
		return this.netCache.getHosts();
	}

	public void CancelFileTransfer(ITransferProgress transferProgress) {

	}

	private void sendByeMessage() {
		try {
			if (this.udpAdapter != null) {
				this.udpAdapter.sendMulticast(new ByeMessage());
			} else {
				LoggerWrapper.logError("Could not find UDP Adapter");
			}
		} catch (IOException e) {
			LoggerWrapper.logError(e.getMessage());
		}
	}
}
