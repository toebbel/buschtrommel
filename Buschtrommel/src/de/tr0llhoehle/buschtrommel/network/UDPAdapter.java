package de.tr0llhoehle.buschtrommel.network;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.Inet4Address;
import java.net.Inet6Address;
import java.net.InetSocketAddress;
import java.net.MulticastSocket;

import de.tr0llhoehle.buschtrommel.LoggerWrapper;
import de.tr0llhoehle.buschtrommel.models.Host;
import de.tr0llhoehle.buschtrommel.models.Message;

/**
 * The UDP adapter holds the line to the multicast group. It receives UDP multicast- and unicast messages from the group and is able to send messages like file annoucements, HI, YO and BYE messages.
 * 
 * @author Moritz Winter
 *
 */
public class UDPAdapter extends MessageMonitor {
	public final static int DEFAULT_PORT = 4747;
	private final static String MULTICAST_ADDRESS_V4 = "239.255.0.113";
	private final static String MULTICAST_ADDRESS_V6 = "ff05::7171";

	private Inet4Address multicastv4Group;
	private Inet6Address multicastv6Group;
	private MulticastSocket multicastSocket;
	private boolean running;
	private int receive_port;
	private int send_port;

	private Thread receiveThread;

	public UDPAdapter(int listenPort, int sendPort) throws IOException {

		this.multicastv4Group = (Inet4Address) Inet4Address.getByName(MULTICAST_ADDRESS_V4);
		this.multicastv6Group = (Inet6Address) Inet6Address.getByName(MULTICAST_ADDRESS_V6);
		this.receive_port = listenPort;
		this.send_port = sendPort;
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
	
	public UDPAdapter() throws IOException {
		this(DEFAULT_PORT, DEFAULT_PORT);
	}

	private void openConnection() throws IOException {
		this.multicastSocket = new MulticastSocket(receive_port);
		this.multicastSocket.joinGroup(multicastv4Group);
		this.multicastSocket.joinGroup(multicastv6Group);
	}

	/**
	 * Stops listening for messages and closes the socket.
	 * 
	 * @throws IOException
	 */
	public void closeConnection() throws IOException {
		this.running = false;
		this.multicastSocket.leaveGroup(multicastv4Group);
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
			message = MessageDeserializer.Deserialize(new String(receivePacket.getData(), Message.ENCODING));
			if (message != null) {
				message.setSource(new InetSocketAddress(receivePacket.getAddress(), receivePacket.getPort()));
				this.sendMessageToObservers(message);
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
		DatagramPacket v4Packet = new DatagramPacket(data.getBytes(), data.length(), this.multicastv4Group,
				send_port);
		DatagramPacket v6Packet = new DatagramPacket(data.getBytes(), data.length(), this.multicastv6Group,
				send_port);

		this.multicastSocket.send(v4Packet);
		this.multicastSocket.send(v6Packet);
	}

	/**
	 * Sends the specified message to the specified host via unicast.
	 * 
	 * The given port-value in the host model will be ignored, because it is for TCP communication only. Using the UDP-Default port instead.
	 * 
	 * @param message
	 *            the specified message
	 * @param host
	 *            the specified host.
	 * @throws IOException 
	 */
	public void sendUnicast(Message message, Host host) throws IOException {
		String data = message.Serialize();
		DatagramPacket packet = new DatagramPacket(data.getBytes(Message.ENCODING), data.length(), host.getAddress(), send_port);

		this.multicastSocket.send(packet);
	}
}
