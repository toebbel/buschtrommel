package de.tr0llhoehle.buschtrommel.network;

import java.net.InetAddress;
import java.util.Hashtable;

import de.tr0llhoehle.buschtrommel.models.ByeMessage;
import de.tr0llhoehle.buschtrommel.models.File;
import de.tr0llhoehle.buschtrommel.models.FileAnnouncementMessage;
import de.tr0llhoehle.buschtrommel.models.Host;
import de.tr0llhoehle.buschtrommel.models.Message;
import de.tr0llhoehle.buschtrommel.models.PeerDiscoveryMessage;

public class NetCache implements IMessageObserver {

	protected Hashtable<InetAddress, Host> knownHosts;
	protected Hashtable<String, File> knownShares;

	@Override
	public void receiveMessage(Message message) {
		if (message instanceof FileAnnouncementMessage) {
			this.fileAnnouncmentHandler((FileAnnouncementMessage) message);
		} else if (message instanceof PeerDiscoveryMessage) {
			this.peerDiscoveryHandler((PeerDiscoveryMessage) message);
		} else if (message instanceof ByeMessage) {
			this.byeHandler((ByeMessage) message);
		}

	}
	
	private void fileAnnouncmentHandler(FileAnnouncementMessage message) {
		File file = message.getFile();
		Host host = new Host(message.getSource(), message.getSource().toString(), UDPAdapter.DEFAULT_PORT);
		int ttl = message.getFile().getTTL();
		String hash = message.getFile().getHash();

		boolean foundHost = false;
		for (InetAddress address : this.knownHosts.keySet()) {
			if (host.equals(this.knownHosts.get(address))) {
				host = this.knownHosts.get(address);
				host.Seen();
				foundHost = true;
				break;
			}
		}

		if (this.knownShares.containsKey(hash)) {
			file = this.knownShares.get(hash);
			if (foundHost) {
				if (file.getSources().contains(host)) {
					file.setTTL(ttl);
				} else {
					file.addHost(host);
					host.addFileToSharedFiles(file);
				}
			} else {
				this.knownHosts.put(host.getAddress(), host);
				file.addHost(host);
				host.addFileToSharedFiles(file);
			}

		} else {
			if (foundHost) {
				this.knownShares.put(hash, file);
				file.addHost(host);
				host.addFileToSharedFiles(file);
			} else {
				this.knownHosts.put(host.getAddress(), host);
				this.knownShares.put(hash, file);
				host.addFileToSharedFiles(file);
				file.addHost(host);
			}
		}
	}
	
	private void peerDiscoveryHandler(PeerDiscoveryMessage message) {
		
	}
	
	private void byeHandler(ByeMessage message) {
		
	}

}
