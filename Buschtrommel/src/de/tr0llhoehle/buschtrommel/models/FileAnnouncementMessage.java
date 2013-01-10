package de.tr0llhoehle.buschtrommel.models;

public class FileAnnouncementMessage extends Message {

	public static final String TYPE_FIELD = "FILE";
	
	private LocalShare announcedFile;

	public FileAnnouncementMessage(LocalShare f) {
		type = TYPE_FIELD;
		announcedFile = f;
	}

	@Override
	public String Serialize() {
		return type + Message.FIELD_SEPERATOR + announcedFile.getHash()
				+ FIELD_SEPERATOR + announcedFile.getTTL() + FIELD_SEPERATOR
				+ announcedFile.getLength() + FIELD_SEPERATOR
				+ announcedFile.getDisplayName() + FIELD_SEPERATOR
				+ announcedFile.getMeta() + MESSAGE_SPERATOR;
	}
	
	public LocalShare getFile() {
		return this.announcedFile;
	}

}
