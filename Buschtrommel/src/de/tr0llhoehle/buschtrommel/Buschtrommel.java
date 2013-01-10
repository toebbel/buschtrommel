package de.tr0llhoehle.buschtrommel;

import java.io.IOException;
import java.net.InetAddress;
import java.util.Hashtable;

import de.tr0llhoehle.buschtrommel.models.ByeMessage;
import de.tr0llhoehle.buschtrommel.models.LocalShare;
import de.tr0llhoehle.buschtrommel.models.RemoteShare;
import de.tr0llhoehle.buschtrommel.models.Host;
import de.tr0llhoehle.buschtrommel.network.FileTransferAdapter;
import de.tr0llhoehle.buschtrommel.network.ITransferProgress;
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
	
	public ITransferProgress DownloadFile(String hash, Host host) {
		return null;
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
