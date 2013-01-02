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
		if(message instanceof FileAnnouncementMessage) {
			String hash = ((FileAnnouncementMessage) message).getFile().getHash();
			File file = ((FileAnnouncementMessage) message).getFile();
			int ttl = ((FileAnnouncementMessage) message).getFile().getTTL();
			Host host = new Host(message.getSource(), message.getSource().toString(), -1);
			if(this.knownShares.containsKey(hash)) {
				if(this.knownShares.get(hash).getSources().contains(host)) {
					this.knownShares.get(hash).setTTL(ttl);
				} else {
					boolean found = false;
					for(InetAddress address : this.knownHosts.keySet()) {
						if(host.equals(this.knownHosts.get(address))) {
							host = this.knownHosts.get(address);
							found = true;
							break;
						}
					}
					if(!found) {
						//TODO: add additional information to host
						host.addFileToSharedFiles(file);
						this.knownHosts.put(host.getAddress(), host);
					}
					this.knownShares.get(hash).addHost(host);
				}
			} else {
				boolean found = false;
				for(InetAddress address : this.knownHosts.keySet()) {
					if(host.equals(this.knownHosts.get(address))) {
						host = this.knownHosts.get(address);
						file.addHost(host);
						found = true;
						break;
					}
				}
				//TODO: add additional information to host
				host.addFileToSharedFiles(file);
				if(!found) {
					this.knownHosts.put(host.getAddress(), host);
				}
				
				this.knownShares.put(hash, file);
			}
		} else if(message instanceof PeerDiscoveryMessage) {
			
		} else if(message instanceof ByeMessage) {
			
		}

	}

}
