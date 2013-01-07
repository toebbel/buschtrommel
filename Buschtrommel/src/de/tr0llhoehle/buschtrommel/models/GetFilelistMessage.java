package de.tr0llhoehle.buschtrommel.models;

public class GetFilelistMessage extends Message {

	public static final String TYPE_FIELD = "GET FILELIST";
	
	public GetFilelistMessage() {
		type = TYPE_FIELD;
	}

	@Override
	public String Serialize() {
		return type + MESSAGE_SPERATOR;
	}

}
