package de.tr0llhoehle.buschtrommel.models;

public class ByeMessage extends Message {

	public ByeMessage() {
		type = "BYE";
	}

	@Override
	public String Serialize() {
		return type + MESSAGE_SPERATOR;
	}

}
