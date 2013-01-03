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
			File file = ((FileAnnouncementMessage) message).getFile();
			Host host = new Host(message.getSource(), message.getSource().toString(), UDPAdapter.DEFAULT_PORT);
			int ttl = ((FileAnnouncementMessage) message).getFile().getTTL();
			String hash = ((FileAnnouncementMessage) message).getFile().getHash();

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
		} else if (message instanceof PeerDiscoveryMessage) {

		} else if (message instanceof ByeMessage) {

		}

	}

}
