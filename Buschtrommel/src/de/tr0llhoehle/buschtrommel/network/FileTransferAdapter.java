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
import java.util.Vector;
import java.util.logging.ConsoleHandler;
import java.util.logging.Logger;

import de.tr0llhoehle.buschtrommel.IGUICallbacks;
import de.tr0llhoehle.buschtrommel.LoggerWrapper;
import de.tr0llhoehle.buschtrommel.LocalShareCache;
import de.tr0llhoehle.buschtrommel.models.GetFileMessage;
import de.tr0llhoehle.buschtrommel.models.GetFilelistMessage;
import de.tr0llhoehle.buschtrommel.models.Host;
import de.tr0llhoehle.buschtrommel.models.Message;
import de.tr0llhoehle.buschtrommel.network.ITransferProgress.TransferStatus;

public class FileTransferAdapter extends MessageMonitor {
	private LocalShareCache myShares;
	private int port = -1;
	private ServerSocket listeningSocket;
	private Thread receiveThread;
	private boolean keepAlive;
	private Vector<Transfer> outgoingTransfers;
	private Hashtable<String, Transfer> incomingTransfers;
	private Logger logger;
	private IGUICallbacks gui;
	private IHostPortResolver hosts;

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
	public FileTransferAdapter(LocalShareCache s, IGUICallbacks guiCallback, IHostPortResolver hostResolver, int port)
			throws IOException {
		logger = java.util.logging.Logger.getLogger(this.getClass().getName());
		this.port = port;
		assert guiCallback != null;
		gui = guiCallback;
		myShares = s;
		hosts = hostResolver;
		incomingTransfers = new Hashtable<>();
		outgoingTransfers = new Vector<>();
		startListening();
	}

	public FileTransferAdapter(LocalShareCache s, IGUICallbacks guiCallback, IHostPortResolver hostResolver)
			throws IOException {
		this(s, guiCallback, hostResolver, 0);
	}

	private void startListening() throws IOException {
		keepAlive = true;

		listeningSocket = new ServerSocket(port);
		port = listeningSocket.getLocalPort();

		listeningSocket.setReuseAddress(true);
		receiveThread = new Thread(new Runnable() {

			@Override
			public void run() {
				logger.info("Start Listening thread");
				handleIncomingConnections();
				logger.info("Stop Listening thread");
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
					logger.warning("interrupted exception while wainting for incoming connection" + e.getMessage());
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
				int bufferSize = -1;
				bufferSize = s.getSendBufferSize();

				if (m instanceof GetFileMessage) {
					OutgoingTransfer transfer = new OutgoingTransfer((GetFileMessage) m, out, myShares,
							new InetSocketAddress(s.getInetAddress(), s.getPort()), bufferSize);
					transfer.SetLoggerParent(logger);
					transfer.start();
					p = transfer;
				} else if (m instanceof GetFilelistMessage) {
					OutgoingTransfer transfer = new OutgoingTransfer((GetFilelistMessage) m, out, myShares,
							new InetSocketAddress(s.getInetAddress(), s.getPort()), bufferSize);
					transfer.SetLoggerParent(logger);
					transfer.start();
					p = transfer;
				} else {
					logger.info("Received garbage on fileTransferAdapter");
				}

				if (p != null) {
					outgoingTransfers.add(p);
					gui.newOutgoingTransferStarted(p);
				}

			} catch (IOException e) {
				logger.warning(e.getMessage());
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
		if (incomingTransfers.containsKey(hash)) {
			logger.info("The file to download was/is already downloaded/downloading - cancel & clean it");
			if (incomingTransfers.get(hash).getStatus() != TransferStatus.Cleaned) {
				incomingTransfers.get(hash).cancel();
				incomingTransfers.get(hash).cleanup();
			}
		}

		Transfer result = new IncomingDownload(new GetFileMessage(hash, 0, length), host, target, hosts);
		incomingTransfers.put(hash, result);
		result.SetLoggerParent(logger);
		result.start();
		return result;
	}

	/**
	 * Removes an incomging filetransfer.
	 * 
	 * If the download has not already been cleaned up, it is canceled and
	 * cleaned. The transfer is removed from the list of incoming downloads.
	 * 
	 * @param hash
	 *            the hash of the file.
	 */
	public void cleanDownloadedTransfer(String hash) {
		if (incomingTransfers.containsKey(hash)) {
			if (incomingTransfers.get(hash).getStatus() != TransferStatus.Cleaned) {
				incomingTransfers.get(hash).cancel();
				incomingTransfers.get(hash).cleanup();
			}
			incomingTransfers.remove(hash);
		}
	}

	/**
	 * Removes all outgoing transfers, that are in the cleaned state
	 */
	public void removeCleanedOutgoingDownloads() {
		Vector<ITransferProgress> candidates = new Vector<>();
		for (ITransferProgress t : outgoingTransfers) {
			if (t.getStatus() == TransferStatus.Cleaned) {
				candidates.add(t);
			}
		}
		outgoingTransfers.removeAll(candidates);
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
		// TODO implement multisource
		return DownloadFile(hash, hosts.get(0), length, target);
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
		incomingTransfers.put("filelist from" + host.toString(), result);

		for (IMessageObserver observer : observers)
			result.registerObserver(observer);
		result.start();
		return result;
	}
}
