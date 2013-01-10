package de.tr0llhoehle.buschtrommel.test.mockups;

import java.util.Vector;

import de.tr0llhoehle.buschtrommel.models.Message;
import de.tr0llhoehle.buschtrommel.network.IMessageObserver;

public class MessageObserverMock implements IMessageObserver {

	Vector<Message> messages = new Vector<>();
	
	@Override
	public void receiveMessage(Message message) {
		messages.add(message);
	}
	
	public Vector<Message> getMessages() {
		return messages;
	}

}
