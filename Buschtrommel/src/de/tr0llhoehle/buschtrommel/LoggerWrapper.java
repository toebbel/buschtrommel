package de.tr0llhoehle.buschtrommel;

import java.util.logging.Logger;

/**
 * Wrapper for the java logging utils.
 * 
 * Provides a single logger for the complete application. Use methods logInfo and logError for convenient use.
 * 
 * @author Tobias Stum
 *
 */
public class LoggerWrapper {
	
	public final static Logger LOGGER = Logger.getLogger("buschtrommel");
	
	/**
	 * Writes info message into the logger
	 * @param message the message to log
	 */
	public final static void logInfo(String message) {
		LOGGER.info(message);
	}
	
	/**
	 * Writes warning message into the logger.
	 * @param message the message to log
	 */
	public final static void logError(String message) {
		LOGGER.warning(message);
	}
}
