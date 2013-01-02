package de.tr0llhoehle.buschtrommel.network;

import java.util.Vector;

import de.tr0llhoehle.buschtrommel.models.Message;

public class MessageMonitor {
	Vector<IMessageObserver> observers;
	
	public void registerObserver(IMessageObserver observer) {
		this.observers.add(observer);
	}
	
	public void removeObserver(IMessageObserver observer) {
		this.observers.remove(observer);
	}
	
	protected void sendMessageToObservers(Message message) {
		for(IMessageObserver observer : observers) {
			new MessageThread(message, observer).start();
		}
	}
}

class MessageThread extends Thread {
	private Message message;
	private IMessageObserver observer;
	public MessageThread(Message message, IMessageObserver observer) {
		this.message = message;
		this.observer = observer;
	}
	
	public void run() {
		this.observer.incomingMessage(message);
	}
}
