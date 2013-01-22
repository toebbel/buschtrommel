package de.tr0llhoehle.buschtrommel.network;

import java.net.InetAddress;

import de.tr0llhoehle.buschtrommel.models.Host;

public interface IHostPortResolver {
	/**
	 * Checks if the specified host already exists. If yes, returns the host, if
	 * no, returns dummy host with port = Host.UNKNOWN_PORT.
	 * 
	 * @param address
	 *            the specified InetAddress
	 * @return the host
	 */
	public Host getOrCreateHost(InetAddress address);
}
