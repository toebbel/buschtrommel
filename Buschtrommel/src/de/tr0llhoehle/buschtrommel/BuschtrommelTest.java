package de.tr0llhoehle.buschtrommel;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.Inet4Address;
import java.net.Inet6Address;
import java.net.MulticastSocket;
import java.net.UnknownHostException;
import java.util.Timer;
import java.util.TimerTask;

import de.tr0llhoehle.buschtrommel.network.UDPAdapter;

public class BuschtrommelTest {

	private final static int PORT = 7474;
	private final static String MULTICAST_ADDRESS_V4 = "239.255.0.113";
	private final static String MULTICAST_ADDRESS_V6 = "ff05::7171";

	/**
	 * @param args
	 * @throws IOException
	 */
	public static void main(String[] args) throws IOException {
		Timer ttlChecker;
		ttlChecker = new Timer();
		TimerTask task = new TimerTask() {
			public void run() {
				System.out.println("check!");
			}
		};
		ttlChecker.scheduleAtFixedRate(task, 5000, 5000);
	}

}
