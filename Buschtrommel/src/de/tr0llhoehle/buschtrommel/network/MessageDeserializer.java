package de.tr0llhoehle.buschtrommel.network;

import de.tr0llhoehle.buschtrommel.LoggerWrapper;
import de.tr0llhoehle.buschtrommel.models.*;

/**
 * This class deserializes messages. @see Deserialize()
 * @author tobi
 *
 */
public class MessageDeserializer {
	
	/**
	 * Creates an instance of a subclass of Message that matches the given context.
	 * 
	 * The Deserializer can handle HI, YO, GET FILE, GET FILELIST, and FILE messages.
	 * 
	 * @param raw Message as String.
	 * @return instance or null if no matching message could be found
	 */
	public static Message Deserialize(String raw) {
		LoggerWrapper.logInfo("deserialize '" + raw + "'");
		int typeSeperator = -1;
		typeSeperator = raw.indexOf(Message.FIELD_SEPERATOR);
		if(typeSeperator == -1) //some messages don't contain any field seperator, because they have only a type field
			typeSeperator = raw.indexOf(Message.MESSAGE_SPERATOR);
		if(typeSeperator == -1) {
			LoggerWrapper.logError("Can't deserialize raw message '" + raw + "'");
			return null;
		}
		
		String typeField = raw.substring(0, typeSeperator);
		switch(typeField.toUpperCase()) {
			case PeerDiscoveryMessage.TYPE_FIELD_HI:
				return DeserializePeerDiscoveryMessage(PeerDiscoveryMessage.DiscoveryMessageType.HI, raw.substring(typeSeperator + 1));
			case ByeMessage.TYPE_FIELD:
				return new ByeMessage();
			case PeerDiscoveryMessage.TYPE_FIELD_YO:
				return DeserializePeerDiscoveryMessage(PeerDiscoveryMessage.DiscoveryMessageType.YO, raw.substring(typeSeperator + 1));
			case FileAnnouncementMessage.TYPE_FIELD:
				return DeserializeFileMessage(raw.substring(typeSeperator + 1));
			case GetFileMessage.TYPE_FIELD:
				return DeserializeGetFileMessage(raw.substring(typeSeperator + 1));
			case GetFilelistMessage.TYPE_FIELD:
				return new GetFilelistMessage();
			default:
				LoggerWrapper.logError("I don't unterstand the following message: '" + raw + "' :(");
				return null;
		}
	}

	private static Message DeserializeGetFileMessage(String msgContent) {
		String[] fields = msgContent.split(String.valueOf(Message.FIELD_SEPERATOR));
		Message result = null;
		
		if(fields.length != 3) {
			LoggerWrapper.logError("Invlaid number of fields in GetFileMessage body: '" + msgContent + "'");
			return result;
		}
		try {
			return new GetFileMessage(fixBrokenHash(fields[0]), Long.valueOf(fields[1]), Long.valueOf(fields[2].substring(0, fields[2].length() - 1)));
		} catch(IllegalArgumentException e) { //NumberFormatException is subtype
			LoggerWrapper.logError("Could not parse the GetFileMessage body: '" + fields[0] + "|" + fields[1] + "|" + fields[2] + "\\. Excpetion: " + e.getMessage());
		}
		
		return result;
	}
	
	private static String fixBrokenHash(String in) {
		return in.toLowerCase().startsWith("0x") ? in.substring(2).toUpperCase() : in.toUpperCase();
	}

	private static Message DeserializeFileMessage(String msgContent) {
		String[] fields = msgContent.split(String.valueOf(Message.FIELD_SEPERATOR));
		Message result = null;
		
		if(fields.length != 5) {
			LoggerWrapper.logError("Invalid number of fields in FileAnnouncementMessage body: '" + msgContent + "'");
			return result;
		}
		
		try {
			int ttl = Integer.valueOf(fields[1]);
			long length = Long.valueOf(fields[2]);

			String meta = fields[4].length() > 1 ? fields[4].substring(0, fields[4].length() - 1) : ""; //if meta contains only message seperator -> "". Otherwise: cut off sperator
			String hash = fixBrokenHash(fields[0]);
			
			
			result = new FileAnnouncementMessage(new LocalShare(hash, length, ttl, fields[3], meta, ""));
		} catch(IllegalArgumentException e) { //NumberFormatException is subtype
			LoggerWrapper.logError("Could not parse the FileAnnouncementMessage body: '" + msgContent + "'. Excpetion: " + e.getMessage());
		}
		
		return result;
	}

	private static Message DeserializePeerDiscoveryMessage(PeerDiscoveryMessage.DiscoveryMessageType t, String msgContent) {
		String[] fields = msgContent.split(String.valueOf(Message.FIELD_SEPERATOR));
		Message result = null;
		
		if(fields.length != 2) {
			LoggerWrapper.logError("Invlaid number of fields in HI/YO message body: '" + msgContent + "'");
			return result;
		}
		
		try{
			int port = Integer.valueOf(fields[0]);
			
			String alias = fields[1].length() > 1 ? fields[1] : ""; //cut off message seperator
			
			
			if(alias.length() > 1){ //alias is contains more than message seperator
				alias = alias.substring(0, alias.length() - 1);
			} else {
				alias = "";
			}
			
			result = new PeerDiscoveryMessage(t, alias, port);
		} catch(IllegalArgumentException e) { //NumberFormatException is subtype
			LoggerWrapper.logError("Could not parse the Hi/Yo message body: '" + msgContent + "'. Excpetion: " + e.getMessage());
		}
		
		return result;
	}

}
