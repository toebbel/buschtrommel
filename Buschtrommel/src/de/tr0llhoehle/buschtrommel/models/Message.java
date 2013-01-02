/**
 * 
 */
package de.tr0llhoehle.buschtrommel.models;

/**
 * @author tobi
 * 
 */
public abstract class Message {
	public static char FIELD_SEPERATOR = 31;
	public static char MESSAGE_SPERATOR = 30;

	protected String type;

	public abstract String Serialize();
	
	@Override
	public String toString() {
		return Serialize();
	}
	
	@Override
	public boolean equals(Object obj) {
		if(!(obj instanceof Message))
			return false;
		return ((Message) obj).Serialize().equals(Serialize());
	}
}
