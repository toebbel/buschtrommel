/**
 * 
 */
package de.tr0llhoehle.buschtrommel.models;

/**
 * @author tobi
 * 
 */
public class PeerDiscoveryMessage extends Message {

	private int port;
	private String alias;

	/**
	 * Creates a HI or YO message.
	 * 
	 * @param type
	 *            type of message
	 * @param alias
	 *            the human readable alias of the peer
	 * @param transferPort
	 *            the open port of this peer for file transfers
	 */
	public PeerDiscoveryMessage(DiscoveryMessageType type, String alias,
			int transferPort) {
		switch (type) {
		case HI:
			this.type = "HI";
			break;
		case YO:
			this.type = "YO";
			break;
		}

		this.alias = alias;
		this.port = transferPort;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.tr0llhoehle.buschtrommel.models.Message#Serialize()
	 */
	@Override
	public String Serialize() {
		return type + FIELD_SEPERATOR + port + FIELD_SEPERATOR + alias
				+ MESSAGE_SPERATOR;
	}

	public enum DiscoveryMessageType {
		HI, YO
	}
}
