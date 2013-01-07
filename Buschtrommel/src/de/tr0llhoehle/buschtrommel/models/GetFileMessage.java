package de.tr0llhoehle.buschtrommel.models;

public class GetFileMessage extends Message {

	public static final String TYPE_FIELD = "GET FILE";
	
	private String hash;
	private long offset;
	private long length;

	/**
	 * Creates an instance of GetFileMessage
	 * 
	 * @param hash
	 *            the SHA1-Hash of the desired file as upper-case String,
	 *            without the leading 0x.
	 * @param offset
	 *            Number of bytes to skip from the beginning of the file
	 * @param length
	 *            Number of bytes to transfer
	 */
	public GetFileMessage(String hash, long offset, long length) {
		this.hash = hash;
		this.offset = offset;
		this.length = length;
		type = TYPE_FIELD;
	}

	public String getHash() {
		return hash;
	}
	
	public long getOffset() {
		return offset;
	}
	
	public long getLength() {
		return length;
	}
	
	@Override
	public String Serialize() {
		return type + FIELD_SEPERATOR + hash + FIELD_SEPERATOR + offset + FIELD_SEPERATOR + length  + MESSAGE_SPERATOR;
	}

}
