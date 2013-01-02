package de.tr0llhoehle.buschtrommel.network;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.Inet4Address;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.SocketException;
import java.net.UnknownHostException;

import de.tr0llhoehle.buschtrommel.LoggerWrapper;
import de.tr0llhoehle.buschtrommel.models.Host;
import de.tr0llhoehle.buschtrommel.models.Message;

public class UDPAdapter extends MessageMonitor implements Runnable {
	private final static int PORT = 7474;
	private final static String MULTICAST_ADDRESS_V4 = "239.255.0.113";
	private final static String MULTICAST_ADDRESS_V6 = "ff05::7171";

	private Inet4Address multicastv4Group;
	private Inet6Address multicastv6Group;
	private MulticastSocket multicastSocket;
	private boolean running;

	public UDPAdapter() {
		try {
			this.multicastv4Group = (Inet4Address) Inet4Address.getByName(MULTICAST_ADDRESS_V4);
			this.multicastv6Group = (Inet6Address) Inet6Address.getByName(MULTICAST_ADDRESS_V6);
			this.openConnection();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		this.running = true;
	}

	private void openConnection() throws IOException {
		this.multicastSocket = new MulticastSocket(PORT);
		this.multicastSocket.joinGroup(multicastv4Group);
		this.multicastSocket.joinGroup(multicastv6Group);
	}

	public void closeConnection() throws IOException {
		this.running = false;
		this.multicastSocket.leaveGroup(multicastv4Group);
		this.multicastSocket.leaveGroup(multicastv6Group);
		this.multicastSocket.close();
	}

	@Override
	public void run() {
		byte[] buffer;
		DatagramPacket receivePacket;
		Message message;
		while (this.running) {
			buffer = new byte[512];
			receivePacket = new DatagramPacket(buffer, buffer.length);
			try {
				multicastSocket.receive(receivePacket);
				message = MessageDeserializer.Deserialize(new String(receivePacket.getData()));
				if (message != null) {
					this.sendMessageToObservers(message);
				}
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

	}

	/**
	 * Sends the specified message to the multicast group via ipv4 and ipv6.
	 * 
	 * @param message
	 *            the specified message
	 */
	public void send(Message message) {
		String data = message.Serialize();
		DatagramPacket v4Packet = new DatagramPacket(data.getBytes(), data.length(), this.multicastv4Group, PORT);
		DatagramPacket v6Packet = new DatagramPacket(data.getBytes(), data.length(), this.multicastv6Group, PORT);
		try {
			this.multicastSocket.send(v4Packet);
			this.multicastSocket.send(v6Packet);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**
	 * Sends the specified message to the specified host via unicast.
	 * 
	 * @param message
	 *            the specified message
	 * @param host
	 *            the specified host
	 */
	public void send(Message message, Host host) {
		String data = message.Serialize();
		DatagramPacket packet = new DatagramPacket(data.getBytes(), data.length(), host.getAddress(), PORT);
		try {
			this.multicastSocket.send(packet);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
