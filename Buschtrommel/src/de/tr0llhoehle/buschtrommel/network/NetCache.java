package de.tr0llhoehle.buschtrommel.network;

import java.io.IOException;
import java.net.InetAddress;
import java.util.Hashtable;
import java.util.Timer;
import java.util.TimerTask;
import java.util.logging.Logger;

import de.tr0llhoehle.buschtrommel.Config;
import de.tr0llhoehle.buschtrommel.IGUICallbacks;
import de.tr0llhoehle.buschtrommel.LoggerWrapper;
import de.tr0llhoehle.buschtrommel.models.ByeMessage;
import de.tr0llhoehle.buschtrommel.models.PeerDiscoveryMessage.DiscoveryMessageType;
import de.tr0llhoehle.buschtrommel.models.RemoteShare;
import de.tr0llhoehle.buschtrommel.models.FileAnnouncementMessage;
import de.tr0llhoehle.buschtrommel.models.Host;
import de.tr0llhoehle.buschtrommel.models.Message;
import de.tr0llhoehle.buschtrommel.models.PeerDiscoveryMessage;
import de.tr0llhoehle.buschtrommel.models.Share;
import de.tr0llhoehle.buschtrommel.models.ShareAvailability;

/**
 * This class manages the state of the network. All known hosts and shares of
 * other hosts are stored here. As soon as a new host is discovered, this object
 * will start a filelist transfer and digest the results via the loose
 * observer-interface.
 * 
 * @author Moritz Winter
 * 
 */
public class NetCache implements IMessageObserver {

	/**
	 * FileAnnouncements that can't be assigned to a host are useless. Therefor
	 * NetCache sends a HI unicast to those hosts to discover their port. The
	 * FileAnnouncement is delayed by this value (in seconds), so the host has
	 * time to send a YO message.
	 */
	private static final int DELAY_FILE_MESSAGE_AFTER_HOST_DISCOVER = 5;

	private static int TTL_REFRESH_RATE = 5;

	protected Hashtable<InetAddress, Host> knownHosts;
	protected Hashtable<String, RemoteShare> knownShares;

	protected IGUICallbacks guiCallbacks;
	protected UDPAdapter udpAdapter;
	protected FileTransferAdapter fileTransferAdapter;
	protected Timer ttlChecker;
	private long lastDiscoveryMulticast;

	private Logger logger;

	public NetCache(UDPAdapter udpAdapter, FileTransferAdapter fileAdapter, IGUICallbacks guiCallbacks) {
		logger = java.util.logging.Logger.getLogger(this.getClass().getName());
		this.udpAdapter = udpAdapter;
		this.guiCallbacks = guiCallbacks;

		this.knownHosts = new Hashtable<InetAddress, Host>();
		this.knownShares = new Hashtable<String, RemoteShare>();

		this.fileTransferAdapter = fileAdapter;

		this.ttlChecker = new Timer();
		TimerTask task = new TimerTask() {
			public void run() {
				checkTTL();
			}
		};
		ttlChecker.scheduleAtFixedRate(task, TTL_REFRESH_RATE * 1000, TTL_REFRESH_RATE * 1000);

		lastDiscoveryMulticast = System.currentTimeMillis();
	}

	/**
	 * Returns the remoteShare with the given hash or null if the share is
	 * unknown
	 * 
	 * @param hash
	 *            of the share
	 * @return null or the share
	 */
	public RemoteShare getShare(String hash) {
		return knownShares.get(hash);
	}

	@Override
	public void receiveMessage(Message message) {
		if (message instanceof FileAnnouncementMessage) {
			logger.info("NetCache receives File availibility message '" + message + "' from" + message.getSource());
			this.fileAnnouncmentHandler((FileAnnouncementMessage) message, false);
		} else if (message instanceof PeerDiscoveryMessage) {
			logger.info("NetCache receives peer discovery message '" + message + "' from" + message.getSource());
			this.peerDiscoveryHandler((PeerDiscoveryMessage) message);
		} else if (message instanceof ByeMessage) {
			logger.info("NetCache receives bye message '" + message + "' from" + message.getSource());
			this.byeHandler((ByeMessage) message);
		}

	}

	/**
	 * Adds the message to the netCache if the host is known.
	 * 
	 * If the host is unknown, a HI Unicast is sent to the host and the
	 * FileAnnouncemessage is delayed, so the host can answer with a YO message.
	 * By this we know it's port and we can properly save the Share in
	 * combination with a valid port
	 * 
	 * @param message
	 *            the message to handle
	 * @param delayed
	 *            whether the message is a delayed one or not
	 */
	private void fileAnnouncmentHandler(FileAnnouncementMessage message, boolean delayed) {
		int ttl = message.getFile().getTTL();
		String hash = message.getFile().getHash();

		Host host = this.getOrCreateHost(message.getSource().getAddress());

		if (host.getPort() != Host.UNKNOWN_PORT) {
			logger.info("The sender of the file announcement (" + message.getSource() + ") is unknown");
			if (this.knownShares.containsKey(hash)) {
				RemoteShare tempShare = this.knownShares.get(hash);

				// if the share is already associated to the host, just
				// update
				// ttl
				if (host.getSharedFiles().containsKey(hash)) {
					logger.info("announced file " + hash + " already known for this host. Refreshing ttl");
					host.getSharedFiles().get(hash).setTTL(ttl);
					if (this.guiCallbacks != null) {
						this.guiCallbacks.updatedTTL(host.getSharedFiles().get(hash));
					}
				}

				// if the share share isn't already associated, associate it;
				// to
				// the host
				else {
					logger.info("announced file " + hash + " is unknown for the current host.");
					host.addFileToSharedFiles(tempShare.addFileSource(host, ttl, message.getFile().getDisplayName(),
							message.getFile().getMeta()));
					if (this.guiCallbacks != null) {
						this.guiCallbacks.newShareAvailable(host.getSharedFiles().get(hash));
					} else {
						logger.warning("GUI callback is null - can't announce file");
					}
				}

			} else {
				// case: share not cached, but host already cached
				logger.info("announced file " + hash + " is unknown for the current host.");
				RemoteShare tmp = new RemoteShare(hash, message.getFile().getLength());
				this.knownShares.put(hash, tmp);
				host.addFileToSharedFiles(tmp.addFileSource(host, ttl, message.getFile().getDisplayName(), message
						.getFile().getMeta()));
				if (this.guiCallbacks != null) {
					this.guiCallbacks.newShareAvailable(host.getSharedFiles().get(hash));
				}

			}
		} else {
			logger.info("FileAnnouncement will be delayed");
			try {
				if (this.udpAdapter != null && !delayed) {
					this.udpAdapter.sendUnicast(new PeerDiscoveryMessage(PeerDiscoveryMessage.DiscoveryMessageType.HI,
							Config.alias, fileTransferAdapter.getPort()), host.getAddress());
					handleFileAnnouncementMessageLater(message, DELAY_FILE_MESSAGE_AFTER_HOST_DISCOVER);
				}

			} catch (IOException e) {
				logger.warning(e.getMessage());
			}

		}
	}

	/**
	 * Starts a thread, that will call receive message with the given message
	 * after a given amount of time
	 * 
	 * @param m
	 *            message to re-receive
	 * @param wait
	 *            number of seconds to wait
	 */
	private void handleFileAnnouncementMessageLater(FileAnnouncementMessage m, int wait) {
		FileAnnouncementDelay task = new FileAnnouncementDelay(m);
		(new Timer()).schedule(task, wait * 1000);
	}

	class FileAnnouncementDelay extends TimerTask {
		FileAnnouncementMessage message;

		public FileAnnouncementDelay(FileAnnouncementMessage m) {
			message = m;
		}

		@Override
		public void run() {
			fileAnnouncmentHandler(message, true);
		}
	}

	private void peerDiscoveryHandler(PeerDiscoveryMessage message) {
		Host host = this.getOrCreateHost(message.getSource().getAddress());
		boolean found = host.getPort() != Host.UNKNOWN_PORT;
		host.setDisplayName(message.getAlias());
		host.setPort(message.getPort());
		if (!found) {
			// add new host
			this.knownHosts.put(host.getAddress(), host);
			if (this.guiCallbacks != null) {
				this.guiCallbacks.newHostDiscovered(host);
			}
		}
		
		if (udpAdapter == null || fileTransferAdapter == null) {
			logger.warning("Can't respond to peer discovery because adapter is not initialized!");
			return;
		}
		
		switch (message.getType()) {
		
		case PeerDiscoveryMessage.TYPE_FIELD_HI:
			PeerDiscoveryMessage rsp = new PeerDiscoveryMessage(DiscoveryMessageType.YO, host.getDisplayName(),
					fileTransferAdapter.getPort());
			try {
				Thread.sleep((int) (Math.random() * Config.maximumYoResponseTime));
				if (System.currentTimeMillis() - lastDiscoveryMulticast > Config.minDiscoveryMulticastIddle) {
					lastDiscoveryMulticast = System.currentTimeMillis();
					udpAdapter.sendMulticast(rsp);
				} else {
					udpAdapter.sendUnicast(rsp, message.getSource().getAddress());
				}

				// autostart filelist download
				fileTransferAdapter.downloadFilelist(new Host(message.getSource().getAddress(),
						((PeerDiscoveryMessage) message).getAlias(), ((PeerDiscoveryMessage) message).getPort()));
			} catch (IOException e) {
				logger.warning("Could not response to HI message: " + e.getMessage());
			} catch (InterruptedException e1) {
				logger.warning("Error while waiting before sending YO response: " + e1.getMessage());
			}
			break;
			
		case PeerDiscoveryMessage.TYPE_FIELD_YO:
			
			
			break;
		default:
		}
	}

	private void byeHandler(ByeMessage message) {
		Host host = this.getOrCreateHost(message.getSource().getAddress());
		if (host.getPort() != Host.UNKNOWN_PORT) {
			RemoteShare tmp;
			if (this.guiCallbacks != null) {
				this.guiCallbacks.hostWentOffline(host);
			}
			for (String hash : host.getSharedFiles().keySet()) {
				tmp = this.knownShares.get(hash);
				tmp.removeFileSource(host.getSharedFiles().get(hash));
				if (tmp.noSourcesAvailable()) {
					this.knownShares.remove(hash);
				}
				this.knownHosts.remove(host.getAddress());
			}
		}
	}

	public Hashtable<String, RemoteShare> getShares() {
		return this.knownShares;
	}

	public Hashtable<InetAddress, Host> getHosts() {
		return this.knownHosts;
	}

	/**
	 * Checks if the specified host already exists. If yes, returns the host, if
	 * no, returns dummy host with port = Host.UNKNOWN_PORT.
	 * 
	 * Changes the host to the host found in cache!
	 * 
	 * @param address
	 *            the specified InetAddress
	 * @return the host
	 */
	public Host getOrCreateHost(InetAddress address) {
		Host host = new Host(address, "foo", Host.UNKNOWN_PORT);
		if (this.knownHosts.containsKey(address)) {

			host = this.knownHosts.get(address);
			host.Seen();
			return host;

		}
		return host;
	}

	/**
	 * Testing purposes
	 * 
	 * @param host
	 */
	public void removeHost(Host host) {
		this.knownHosts.remove(host.getAddress());
	}

	public boolean shareExists(String hash) {
		return knownShares.containsKey(hash);
	}

	private void checkTTL() {
		Host host;
		ShareAvailability shareAvailability;
		int tmpTTL = 0;
		for (InetAddress address : knownHosts.keySet()) {
			host = knownHosts.get(address);
			for (String hash : host.getSharedFiles().keySet()) {
				shareAvailability = host.getSharedFiles().get(hash);
				tmpTTL = shareAvailability.getTtl();
				if (tmpTTL != Share.TTL_INFINITY && tmpTTL - TTL_REFRESH_RATE <= 0) {
					host.removeFileFromSharedFiles(hash);
					shareAvailability.getFile().removeFileSource(shareAvailability);
					if (shareAvailability.getFile().noSourcesAvailable()) {
						knownShares.remove(hash);
					}
					if (guiCallbacks != null) {
						guiCallbacks.removeShare(shareAvailability);
					}
				} else if (tmpTTL != Share.TTL_INFINITY) {
					if (tmpTTL - TTL_REFRESH_RATE > 0) {
						shareAvailability.setTTL(tmpTTL - TTL_REFRESH_RATE);
					}
				}
			}
		}

	}

	public boolean hostExists(InetAddress address) {
		return this.knownHosts.containsKey(address);
	}

}
