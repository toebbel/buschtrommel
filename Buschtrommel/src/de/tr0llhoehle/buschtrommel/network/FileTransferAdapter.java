package de.tr0llhoehle.buschtrommel.network;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.logging.ConsoleHandler;

import de.tr0llhoehle.buschtrommel.LoggerWrapper;
import de.tr0llhoehle.buschtrommel.LocalShareCache;
import de.tr0llhoehle.buschtrommel.models.GetFileMessage;
import de.tr0llhoehle.buschtrommel.models.GetFilelistMessage;
import de.tr0llhoehle.buschtrommel.models.Host;
import de.tr0llhoehle.buschtrommel.models.Message;

public class FileTransferAdapter extends MessageMonitor {
	private LocalShareCache myShares;
	private int port = -1;
	private ServerSocket listeningSocket;
	private Thread receiveThread;
	private boolean keepAlive;
	private ArrayList<Transfer> outgoingTransfers;
	private Hashtable<String, Transfer> incomingTransfers;
	protected static final int DEFAULT_BUFFER_SIZE = 512;

	/**
	 * Creates an instance of FileTransferAdapter and opens a listening TCP Port
	 * on the given port.
	 * 
	 * @param s
	 *            the manager for all own shares to serve GET FILE and GET FILE
	 *            MESSAGE
	 * @param port
	 *            the tcp port to use.
	 * @throws IOException
	 */
	public FileTransferAdapter(LocalShareCache s, int port) throws IOException {
		this.port = port;
		myShares = s;
		incomingTransfers = new Hashtable<>();
		outgoingTransfers = new ArrayList<>();
		startListening();
	}

	public FileTransferAdapter(LocalShareCache s) throws IOException {
		this(s, 0);
	}

	private void startListening() throws IOException {
		keepAlive = true;

		listeningSocket = new ServerSocket(port);
		port = listeningSocket.getLocalPort();

		listeningSocket.setReuseAddress(true);
		receiveThread = new Thread(new Runnable() {

			@Override
			public void run() {
				LoggerWrapper.logInfo("Start Listening thread");
				handleIncomingConnections();
				LoggerWrapper.logInfo("Stop Listening thread");
			}
		});
		receiveThread.start();
	}

	/**
	 * Called by receiveThread
	 * 
	 * @throws IOException
	 */
	private void handleIncomingConnections() {
		while (keepAlive) {
			Socket s;
			try {
				s = listeningSocket.accept();
				try {
					Thread.sleep(500);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				InputStream networkInputStream = s.getInputStream();
				byte[] buffer = new byte[512];
				int next = 0;
				int bytesRead = 0;
				int iddleLoops = 0;
				while ((next = networkInputStream.read()) != Message.MESSAGE_SPERATOR && iddleLoops < 3) {
					if (next == -1) {
						try {
							iddleLoops++;
							Thread.sleep(100);
						} catch (InterruptedException e) {
						}
					} else {
						buffer[bytesRead++] = (byte) next;
					}
				}
				if (iddleLoops == 3)
					return;
				buffer[bytesRead++] = Message.MESSAGE_SPERATOR;
				final Message m = MessageDeserializer.Deserialize(new String(buffer, 0, bytesRead));
				if (m != null) {
					m.setSource(new InetSocketAddress(s.getInetAddress(), s.getPort()));
					sendMessageToObservers(m);
				}

				final OutputStream out = s.getOutputStream();
				Transfer p = null;
				if (m instanceof GetFileMessage) {
					s.setReceiveBufferSize(DEFAULT_BUFFER_SIZE);
					OutgoingTransfer transfer = new OutgoingTransfer((GetFileMessage) m, out, myShares,
							new InetSocketAddress(s.getInetAddress(), s.getPort()), DEFAULT_BUFFER_SIZE);
					transfer.RemoveLogHander(new ConsoleHandler());
					transfer.start();
					p = transfer;
				} else if (m instanceof GetFilelistMessage) {
					s.setReceiveBufferSize(DEFAULT_BUFFER_SIZE);
					OutgoingTransfer transfer = new OutgoingTransfer((GetFilelistMessage) m, out, myShares,
							new InetSocketAddress(s.getInetAddress(), s.getPort()),DEFAULT_BUFFER_SIZE);
					transfer.RemoveLogHander(new ConsoleHandler());
					transfer.start();
					p = transfer;
				} else {
					LoggerWrapper.logInfo("Received garbage on fileTransferAdapter");
				}

				if (p != null)
					outgoingTransfers.add(p);

			} catch (IOException e) {
				LoggerWrapper.logError(e.getMessage());
			}
		}
	}

	/**
	 * Creates <b>and starts</b> a download from the given host
	 * 
	 * @param hash
	 *            the hash of the file to download
	 * @param host
	 *            the source host
	 * @param length
	 *            of the file
	 * @param target
	 *            local target file
	 * @return progress interface instance, that is connected with this download
	 */
	public Transfer DownloadFile(String hash, Host host, long length, java.io.File target) {
		Transfer result = new IncomingDownload(new GetFileMessage(hash, 0, length), host, target, DEFAULT_BUFFER_SIZE);
		incomingTransfers.put(hash, result);
		result.RemoveLogHander(new ConsoleHandler());
		result.start();
		return result;
	}

	/**
	 * Starts a multisource download
	 * 
	 * @param hash
	 *            hash of requested file
	 * @param hosts
	 *            hosts that are offering the file
	 * @param length
	 *            expected length of download
	 * @return one ITransferProgress that may contain multiple children.
	 */
	public Transfer DownloadFile(String hash, List<Host> hosts, long length, java.io.File target) {
		assert hosts.size() > 0;
		return DownloadFile(hash, hosts.get(0), length, target); // TODO
																	// implement
																	// multisource
	}

	/**
	 * Returns all outgoing Transfers that have been made. This is a clone of
	 * the internal data structure.
	 * 
	 * @return all outgoing transfers
	 */
	@SuppressWarnings("unchecked")
	public ArrayList<ITransferProgress> getOutgoingTransfers() {
		return (ArrayList<ITransferProgress>) outgoingTransfers.clone();
	}

	/**
	 * Returns all incoming Transfers that have been made. This is a copy of the
	 * internal data structure
	 * 
	 * @return all incoming transfers
	 */
	@SuppressWarnings("unchecked")
	public Hashtable<String, ITransferProgress> getIncomingTransfers() {
		return (Hashtable<String, ITransferProgress>) incomingTransfers.clone();
	}

	/**
	 * Returns the TCP port that this adapter listens on
	 * 
	 * @return the port
	 */
	public int getPort() {
		return port;
	}

	/**
	 * Stops the listening thread and all running transfers.
	 * 
	 * @throws IOException
	 */
	public void close() throws IOException {
		keepAlive = false;
		receiveThread.interrupt();
		for (ITransferProgress t : outgoingTransfers)
			t.cancel();
		for (String k : incomingTransfers.keySet())
			incomingTransfers.get(k).cancel();
		listeningSocket.close();
	}

	public Transfer downloadFilelist(Host host) {
		IncomingFilelistTransfer result = new IncomingFilelistTransfer(host);

		for (IMessageObserver observer : observers)
			result.registerObserver(observer);
		result.start();
		return result;
	}
}
