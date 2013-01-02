package de.tr0llhoehle.buschtrommel.network;

import de.tr0llhoehle.buschtrommel.models.Message;

public interface IMessageObserver {
	public void receiveMessage(Message message);
}
