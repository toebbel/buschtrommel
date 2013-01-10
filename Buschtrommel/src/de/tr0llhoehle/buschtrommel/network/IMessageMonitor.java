package de.tr0llhoehle.buschtrommel.network;

/**
 * Interface for the Observer-Patter.
 * 
 * Monitors will call all registered observers, as soon as a message arrives. Some implementations will do this asynchronously.
 * 
 * @author tobi
 *
 */
public interface IMessageMonitor {

	/**
	 * Register new observer.
	 * @param observer
	 */
	public abstract void registerObserver(IMessageObserver observer);

	/**
	 * Remove observer.
	 * @param observer
	 */
	public abstract void removeObserver(IMessageObserver observer);

}