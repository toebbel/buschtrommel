package de.tr0llhoehle.buschtrommel.test.mockups;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.Inet4Address;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Vector;

public class NetworkUDPMock {
	private final static String MULTICAST_ADDRESS_V4 = "239.255.0.113";
	private final static String MULTICAST_ADDRESS_V6 = "ff05::7171";

	private Inet4Address multicastv4Group;
	private Inet6Address multicastv6Group;
	private MulticastSocket multicastSocket;
	private Vector<InetAddress> localAddresses;
	private boolean running;
	private int receive_port;
	private int send_port;

	private Thread receiveThread;
	
	public Vector<DatagramPacket> receivedMessages;

	/**
	 * Creates an instance of UDP adapter, that listens on the given ports and uses IPv4 and/or IPv6
	 * @param listenPort the port to listen to
	 * @param sendPort the port to send in (can be the same as listen port)
	 * @param ipv4 true to use IPv4. Either IPv4 or IPv6 or both have to be set.
	 * @param ipv6 true to use IPv6. Either IPv4 or IPv6 or both have to be set.
	 * @throws IOException
	 */
	public NetworkUDPMock(int listenPort, int sendPort, boolean ipv4, boolean ipv6) throws IOException {
		assert (ipv4 || ipv6);
		if (ipv4)
			multicastv4Group = (Inet4Address) Inet4Address.getByName(MULTICAST_ADDRESS_V4);
		if (ipv6)
			multicastv6Group = (Inet6Address) Inet6Address.getByName(MULTICAST_ADDRESS_V6);
		this.receive_port = listenPort;
		this.send_port = sendPort;
		receivedMessages = new Vector<>();
		
		localAddresses = this.getAllLocalAddresses();

		multicastSocket = new MulticastSocket(receive_port);
		if (multicastv4Group != null)
			multicastSocket.joinGroup(multicastv4Group);
		if (multicastv6Group != null)
			multicastSocket.joinGroup(multicastv6Group);

		receiveThread = new Thread(new Runnable() {

			@Override
			public void run() {
				try {
					startReceiving();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		});

		this.running = true;
		receiveThread.start();
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
		while (this.running) {
			buffer = new byte[512];
			receivePacket = new DatagramPacket(buffer, buffer.length);
			multicastSocket.receive(receivePacket);
			if (!this.localAddresses.contains(receivePacket.getAddress())) {
				receivedMessages.add(receivePacket);
			}
		}
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

	public void sendMulticast(byte[] data) throws IOException {
		if (multicastv4Group != null) {
			DatagramPacket v4Packet = new DatagramPacket(data, data.length, this.multicastv4Group,
					send_port);
			multicastSocket.send(v4Packet);
		}

		if (multicastv6Group != null) {
			DatagramPacket v6Packet = new DatagramPacket(data, data.length, this.multicastv6Group,
					send_port);
			multicastSocket.send(v6Packet);
		}
	}

	public void sendUnicast(byte[] data, InetAddress host) throws IOException {
		DatagramPacket packet = new DatagramPacket(data, data.length, host, send_port);
		multicastSocket.send(packet);
	}
}
