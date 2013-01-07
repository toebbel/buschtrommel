package de.tr0llhoehle.buschtrommel.models;

public class ByeMessage extends Message {

	public static final String TYPE_FIELD = "BYE";
	
	public ByeMessage() {
		type = TYPE_FIELD;
	}

	@Override
	public String Serialize() {
		return type + MESSAGE_SPERATOR;
	}

}
