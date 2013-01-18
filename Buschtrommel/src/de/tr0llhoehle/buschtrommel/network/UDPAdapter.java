package de.tr0llhoehle.buschtrommel.network;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.Inet4Address;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.MulticastSocket;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Vector;

import de.tr0llhoehle.buschtrommel.LoggerWrapper;
import de.tr0llhoehle.buschtrommel.models.Host;
import de.tr0llhoehle.buschtrommel.models.Message;

/**
 * The UDP adapter holds the line to the multicast group. It receives UDP
 * multicast- and unicast messages from the group and is able to send messages
 * like file annoucements, HI, YO and BYE messages.
 * 
 * @author Moritz Winter
 * 
 */
public class UDPAdapter extends MessageMonitor {
	public final static int DEFAULT_PORT = 4747;
	private final static String MULTICAST_ADDRESS_V4 = "239.255.0.113";
	private final static String MULTICAST_ADDRESS_V6 = "ff05::7171";

	private Vector<InetAddress> localAddresses;
	private Inet4Address multicastv4Group;
	private Inet6Address multicastv6Group;
	private MulticastSocket multicastSocket;
	private boolean running;
	private int receive_port;
	private int send_port;

	private Thread receiveThread;

	/**
	 * Creates an instance of UDP adapter, that listens on the default port and uses IPv4 and IPv6.
	 * @throws IOException
	 */
	public UDPAdapter() throws IOException {
		this(DEFAULT_PORT, DEFAULT_PORT);
	}

	/**
	 * Creates an instance of UDP adapter, that listens on given ports and uses IPv4 and IPv6
	 * @param listenPort the port to listen to
	 * @param sendPort the port to send on (can be the same as listen port)
	 * @throws IOException
	 */
	public UDPAdapter(int listenPort, int sendPort) throws IOException {
		this(listenPort, sendPort, true, true);
	}

	/**
	 * Creates an instance of UDP adapter, that listens on the given ports and uses IPv4 and/or IPv6
	 * @param listenPort the port to listen to
	 * @param sendPort the port to send in (can be the same as listen port)
	 * @param ipv4 true to use IPv4. Either IPv4 or IPv6 or both have to be set.
	 * @param ipv6 true to use IPv6. Either IPv4 or IPv6 or both have to be set.
	 * @throws IOException
	 */
	public UDPAdapter(int listenPort, int sendPort, boolean ipv4, boolean ipv6) throws IOException {
		assert (ipv4 || ipv6);
		if (ipv4)
			this.multicastv4Group = (Inet4Address) Inet4Address.getByName(MULTICAST_ADDRESS_V4);
		if (ipv6)
			this.multicastv6Group = (Inet6Address) Inet6Address.getByName(MULTICAST_ADDRESS_V6);
		this.receive_port = listenPort;
		this.send_port = sendPort;
		this.localAddresses = this.getAllLocalAddresses();
		this.openConnection();

		receiveThread = new Thread(new Runnable() {

			@Override
			public void run() {
				try {
					startReceiving();
				} catch (IOException e) {
					LoggerWrapper.logError(e.getMessage());
				}
			}
		});

		this.running = true;
		receiveThread.start();
	}

	private void openConnection() throws IOException {
		this.multicastSocket = new MulticastSocket(receive_port);
		if (multicastv4Group != null)
			this.multicastSocket.joinGroup(multicastv4Group);
		if (multicastv6Group != null)
			this.multicastSocket.joinGroup(multicastv6Group);
	}

	/**
	 * Stops listening for messages and closes the socket.
	 * 
	 * @throws IOException
	 */
	public void closeConnection() throws IOException {
		this.running = false;
		if (multicastv4Group != null)
			this.multicastSocket.leaveGroup(multicastv4Group);
		if (multicastv6Group != null)
			this.multicastSocket.leaveGroup(multicastv6Group);
		this.multicastSocket.close();
	}

	private void startReceiving() throws IOException {
		byte[] buffer;
		DatagramPacket receivePacket;
		Message message;
		while (this.running) {
			buffer = new byte[512];
			receivePacket = new DatagramPacket(buffer, buffer.length);
			multicastSocket.receive(receivePacket);
			if (!this.localAddresses.contains(receivePacket.getAddress())) {
				message = MessageDeserializer.Deserialize(new String(receivePacket.getData(), Message.ENCODING));
				if (message != null) {
					message.setSource(new InetSocketAddress(receivePacket.getAddress(), receivePacket.getPort()));
					this.sendMessageToObservers(message);
				}
			}
		}
	}

	/**
	 * Sends the specified message to the multicast group via ipv4 and ipv6.
	 * 
	 * @param message
	 *            the specified message
	 * @throws IOException
	 */
	public void sendMulticast(Message message) throws IOException {
		String data = message.Serialize();
		if (multicastv4Group != null) {
			DatagramPacket v4Packet = new DatagramPacket(data.getBytes(), data.length(), this.multicastv4Group,
					send_port);
			this.multicastSocket.send(v4Packet);
		}

		if (multicastv6Group != null) {
			DatagramPacket v6Packet = new DatagramPacket(data.getBytes(), data.length(), this.multicastv6Group,
					send_port);
			this.multicastSocket.send(v6Packet);
		}
	}

	/**
	 * Sends the specified message to the specified host via unicast.
	 * 
	 * The given port-value in the host model will be ignored, because it is for
	 * TCP communication only. Using the UDP-Default port instead.
	 * 
	 * @param message
	 *            the specified message
	 * @param host
	 *            the specified host.
	 * @throws IOException
	 */
	public void sendUnicast(Message message, InetAddress host) throws IOException {
		String data = message.Serialize();
		DatagramPacket packet = new DatagramPacket(data.getBytes(Message.ENCODING), data.length(), host, send_port);

		this.multicastSocket.send(packet);
	}

	private Vector<InetAddress> getAllLocalAddresses() throws SocketException {
		Vector<InetAddress> addresses = new Vector<InetAddress>();
		for (Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces(); en.hasMoreElements();) {
			NetworkInterface intf = en.nextElement();
			for (Enumeration<InetAddress> enumIpAddr = intf.getInetAddresses(); enumIpAddr.hasMoreElements();) {
				addresses.addAll(Collections.list(enumIpAddr));
			}
		}
		return addresses;
	}
}
