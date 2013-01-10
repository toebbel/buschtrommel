package de.tr0llhoehle.buschtrommel.network;

import java.net.InetAddress;
import java.util.Hashtable;

import de.tr0llhoehle.buschtrommel.models.ByeMessage;
import de.tr0llhoehle.buschtrommel.models.LocalShare;
import de.tr0llhoehle.buschtrommel.models.RemoteShare;
import de.tr0llhoehle.buschtrommel.models.FileAnnouncementMessage;
import de.tr0llhoehle.buschtrommel.models.Host;
import de.tr0llhoehle.buschtrommel.models.Message;
import de.tr0llhoehle.buschtrommel.models.PeerDiscoveryMessage;
import de.tr0llhoehle.buschtrommel.models.Share;

/**
 * 
 * @author moritz
 * 
 */
public class NetCache implements IMessageObserver {

	protected Hashtable<InetAddress, Host> knownHosts;
	protected Hashtable<String, RemoteShare> knownShares;

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
		Host host = new Host(message.getSource(), message.getSource().toString(), UDPAdapter.DEFAULT_PORT);
		int ttl = message.getFile().getTTL();
		String hash = message.getFile().getHash();

		boolean foundHost = this.hostExists(host);

		if (this.knownShares.containsKey(hash)) {
			RemoteShare tempShare = this.knownShares.get(hash);

			// case: host and share already cached
			if (foundHost) {

				// if the share is already associated to the host, just update
				// ttl
				if (host.getSharedFiles().containsKey(hash)) {
					host.getSharedFiles().get(hash).setTTL(ttl);
				}

				// if the share share isn't already associated, associate it to
				// the host
				else {
					host.addFileToSharedFiles(tempShare.addFileSource(host, ttl,
							message.getFile().getDisplayName(), message.getFile().getMeta()));
				}

				// case: share cached but new host
			} else {
				this.knownHosts.put(host.getAddress(), host);
				host.addFileToSharedFiles(tempShare.addFileSource(host, ttl,
						message.getFile().getDisplayName(), message.getFile().getMeta()));
			}

		} else {
			// case: share not cached, but host already cached
			if (foundHost) {
				RemoteShare tmp = new RemoteShare(hash, message.getFile().getLength());
				this.knownShares.put(hash, tmp);
				host.addFileToSharedFiles(tmp.addFileSource(host, ttl, message.getFile().getDisplayName(), message
						.getFile().getMeta()));
			}

			// case: neither host nor share cached
			else {
				this.knownHosts.put(host.getAddress(), host);
				RemoteShare tmp = new RemoteShare(hash, message.getFile().getLength());
				this.knownShares.put(hash, tmp);
				host.addFileToSharedFiles(tmp.addFileSource(host, ttl, message.getFile().getDisplayName(), message
						.getFile().getMeta()));
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
		Host host = new Host(message.getSource(), message.getSource().toString(), UDPAdapter.DEFAULT_PORT);
		if (this.hostExists(host)) {

		}
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
