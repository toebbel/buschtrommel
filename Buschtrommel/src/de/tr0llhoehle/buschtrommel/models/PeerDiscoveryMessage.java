/**
 * 
 */
package de.tr0llhoehle.buschtrommel.models;

/**
 * @author tobi
 * 
 */
public class PeerDiscoveryMessage extends Message {

	public static final String TYPE_FIELD_HI = "HI";
	public static final String TYPE_FIELD_YO = "YO";
	
	private int port;
	private String alias;
	private DiscoveryMessageType docoveryTypetype;

	/**
	 * Creates a HI or YO message.
	 * 
	 * @param messageType
	 *            type of message
	 * @param alias
	 *            the human readable alias of the peer. Illegal Chacaters (field and message seperator) will be removed with spaces
	 * @param transferPort
	 *            the open port of this peer for file transfers
	 */
	public PeerDiscoveryMessage(DiscoveryMessageType messageType, String alias, int transferPort) {
		this.docoveryTypetype = messageType;
		switch (messageType) {
		case HI:
			this.type = TYPE_FIELD_HI;
			break;
		case YO:
			this.type = TYPE_FIELD_YO;
			break;
		}

		if (transferPort < 0)
			throw new IllegalArgumentException("port is negative");
		alias = alias.replace(MESSAGE_SPERATOR, ' ');
		alias = alias.replace(FIELD_SEPERATOR, ' ');
		
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
		return type + FIELD_SEPERATOR + port + FIELD_SEPERATOR + alias + MESSAGE_SPERATOR;
	}

	public enum DiscoveryMessageType {
		HI, YO
	}

	public int getPort() {
		return port;
	}

	public String getAlias() {
		return alias;
	}
	
	public DiscoveryMessageType getDiscoveryMessageType() {
		return docoveryTypetype;
	}
}
