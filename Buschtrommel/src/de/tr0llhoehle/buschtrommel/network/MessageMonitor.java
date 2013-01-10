package de.tr0llhoehle.buschtrommel.network;

import java.util.Vector;

import de.tr0llhoehle.buschtrommel.models.Message;

/**
 * This class represents an implementation of the MessageMonitor, which notifies IMessageObserver-instances.
 * 
 * This implementation will create a Thread for every notification.
 * 
 * @author Moritz Winter
 *
 */
public class MessageMonitor {
	Vector<IMessageObserver> observers = new Vector<>();
	
	/**
	 * Registers a new observer. No observer will be added twice.
	 * @param observer adds the obsrever
	 */
	public void registerObserver(IMessageObserver observer) {
		this.observers.add(observer);
	}
	
	/**
	 * Removes the observer, if it hast been registered before.
	 * @param observer the obsrever to remove.
	 */
	public void removeObserver(IMessageObserver observer) {
		this.observers.remove(observer);
	}
	
	/**
	 * Send the specified message to each registered observer. Each operation gets his own thread.
	 * @param message the specified message
	 */
	protected void sendMessageToObservers(Message message) {
		for(IMessageObserver observer : observers) {
			new MessageThread(message, observer).start();
		}
	}
}

/**
 * A thread for the notification deliverance
 * @author Moritz Winter
 *
 */
class MessageThread extends Thread {
	private Message message;
	private IMessageObserver observer;
	public MessageThread(Message message, IMessageObserver observer) {
		this.message = message;
		this.observer = observer;
	}
	
	public void run() {
		this.observer.receiveMessage(message);
	}
}
