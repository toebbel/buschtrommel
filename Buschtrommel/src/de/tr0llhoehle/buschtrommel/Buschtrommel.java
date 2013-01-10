package de.tr0llhoehle.buschtrommel;

import java.io.IOException;
import java.net.InetAddress;
import java.util.Hashtable;

import de.tr0llhoehle.buschtrommel.models.ByeMessage;
import de.tr0llhoehle.buschtrommel.models.GetFileMessage;
import de.tr0llhoehle.buschtrommel.models.LocalShare;
import de.tr0llhoehle.buschtrommel.models.RemoteShare;
import de.tr0llhoehle.buschtrommel.models.Host;
import de.tr0llhoehle.buschtrommel.network.FileTransferAdapter;
import de.tr0llhoehle.buschtrommel.network.ITransferProgress;
import de.tr0llhoehle.buschtrommel.network.IncomingDownload;
import de.tr0llhoehle.buschtrommel.network.NetCache;
import de.tr0llhoehle.buschtrommel.network.UDPAdapter;

public class Buschtrommel {

	private IGUICallbacks gui;
	private FileTransferAdapter fileTransferAdapter;
	private UDPAdapter udpAdapter;
	private NetCache netCache;

	public Buschtrommel(IGUICallbacks gui) {
		if (!HashFuncWrapper.check()) {
			//cancel bootstrap: Hashfunction is not available!
			this.gui = gui;
			this.netCache = new NetCache(this.udpAdapter);
		}
	}
	
	public void start() throws IOException {
		//TODO: create new FileTransferAdapter
		this.udpAdapter = new UDPAdapter();
		this.udpAdapter.registerObserver(netCache);
	}
	
	public void stop() throws IOException {
		this.sendByeMessage();
		
		//TODO: destroy fileTransferAdapter
		this.udpAdapter.closeConnection();
		this.udpAdapter.removeObserver(netCache);
		this.udpAdapter = null;
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
	
	public void AddFileToShare(String path, String dspName, String meta) {
		
	}
	
	public void RemoveFileFromShare(LocalShare file) {
		
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
		return null;
	}
	
	public void CancelFileTransfer(ITransferProgress transferProgress) {
		
	}
	
	private void sendByeMessage() {
		try {
			this.udpAdapter.sendMulticast(new ByeMessage());
		} catch (IOException e) {
			LoggerWrapper.logError(e.getMessage());
		}
	}
}
