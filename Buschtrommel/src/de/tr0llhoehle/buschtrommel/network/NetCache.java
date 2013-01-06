package de.tr0llhoehle.buschtrommel.network;

import java.net.InetAddress;
import java.util.Hashtable;

import de.tr0llhoehle.buschtrommel.models.ByeMessage;
import de.tr0llhoehle.buschtrommel.models.File;
import de.tr0llhoehle.buschtrommel.models.FileAnnouncementMessage;
import de.tr0llhoehle.buschtrommel.models.Host;
import de.tr0llhoehle.buschtrommel.models.Message;
import de.tr0llhoehle.buschtrommel.models.PeerDiscoveryMessage;

/**
 * 
 * @author moritz
 * 
 */
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

		boolean foundHost = this.hostExists(host);

		if (this.knownShares.containsKey(hash)) {
			file = this.knownShares.get(hash);

			// case: host and share already cached
			if (foundHost) {

				// if the share is already associated to the host, just update
				// ttl
				if (file.getSources().contains(host)) {
					file.setTTL(ttl);
				}

				// if the share share isn't already associated, associate it to
				// the host
				else {
					file.addHost(host);
					host.addFileToSharedFiles(file);
				}

				// case: host cached but new share
			} else {
				this.knownHosts.put(host.getAddress(), host);
				file.addHost(host);
				host.addFileToSharedFiles(file);
			}

		} else {
			// case: share not cached, but host already cached
			if (foundHost) {
				this.knownShares.put(hash, file);
				file.addHost(host);
				host.addFileToSharedFiles(file);
			}

			// case: neither host nor share cached
			else {
				this.knownHosts.put(host.getAddress(), host);
				this.knownShares.put(hash, file);
				host.addFileToSharedFiles(file);
				file.addHost(host);
			}
		}
	}

	private void peerDiscoveryHandler(PeerDiscoveryMessage message) {
		Host host = new Host(message.getSource(), message.getAlias(), message.getPort());
		if (this.hostExists(host)) {
			// update values
			host.setDisplayName(message.getAlias());
			host.setPort(message.getPort());
		} else {
			// add new host
			this.knownHosts.put(host.getAddress(), host);
		}
	}

	private void byeHandler(ByeMessage message) {

	}

	/**
	 * Checks if the specified host already exists. Updates the last seen value
	 * of the host.
	 * 
	 * Changes the host to the host found in cache!
	 * 
	 * @param host
	 *            the specified host
	 * @return true if the specified host was found
	 */
	private boolean hostExists(Host host) {
		boolean foundHost = false;
		for (InetAddress address : this.knownHosts.keySet()) {
			if (host.equals(this.knownHosts.get(address))) {
				host = this.knownHosts.get(address);
				host.Seen();
				return true;
			}
		}
		return false;
	}

}
