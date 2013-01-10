package de.tr0llhoehle.buschtrommel.network;

import de.tr0llhoehle.buschtrommel.models.Message;

/**
 * Message observers will be notified, as soon as a message was received, that could be deserialized, regardless which type of message.
 *
 */
public interface IMessageObserver {
	public void receiveMessage(Message message);
}
