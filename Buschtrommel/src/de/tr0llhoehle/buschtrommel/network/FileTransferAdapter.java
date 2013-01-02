package de.tr0llhoehle.buschtrommel.network;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;

import de.tr0llhoehle.buschtrommel.LoggerWrapper;
import de.tr0llhoehle.buschtrommel.ShareCache;
import de.tr0llhoehle.buschtrommel.models.File;
import de.tr0llhoehle.buschtrommel.models.FileRequestResponseMessage;
import de.tr0llhoehle.buschtrommel.models.FileRequestResponseMessage.ResponseCode;
import de.tr0llhoehle.buschtrommel.models.GetFileMessage;
import de.tr0llhoehle.buschtrommel.models.GetFilelistMessage;
import de.tr0llhoehle.buschtrommel.models.Message;

public class FileTransferAdapter extends MessageMonitor {
	private ShareCache myShares;
	private int port = -1;
	private ServerSocket listeningSocket;
	private Thread receiveThread;
	private boolean keepAlive;

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
	public FileTransferAdapter(ShareCache s, int port) throws IOException {
		this.port = port;
		myShares = s;
		startListening();
	}

	public FileTransferAdapter(ShareCache s) throws IOException {
		myShares = s;
		startListening();
	}

	private void startListening() throws IOException {
		keepAlive = true;
		if (port == -1) {
			listeningSocket = new ServerSocket();
			port = listeningSocket.getLocalPort();
		} else {
			listeningSocket = new ServerSocket(port);
		}
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
				InputStream in = s.getInputStream();
				byte[] raw_message = new byte[512];
				final Message m = MessageDeserializer.Deserialize(new String(raw_message).trim());
				if(m != null)
					sendMessageToObservers(m);
				m.setSource(s.getInetAddress());
				final OutputStream out = s.getOutputStream();
				if (m instanceof GetFileMessage) {
					new OutgoingFileTransfer((GetFileMessage) m, out, myShares).start();
				} else if (m instanceof GetFilelistMessage) {
					new Thread(new Runnable() {

						@Override
						public void run() {
							try {
								handleGetFileList((GetFilelistMessage) m, out);
							} catch (IOException e) {
								LoggerWrapper.logError("could not handle GET FILELIST:" + e.getMessage());
							}
						}
					}).start();
				}
			} catch (IOException e) {
				LoggerWrapper.logError(e.getMessage());
			}
		}
	}

	

	private void handleGetFileList(GetFilelistMessage m, OutputStream out) throws IOException {
		out.write(myShares.getAllShares().getBytes());
		out.close();
	}

	/**
	 * Returns the TCP port that this adapter listens on
	 * 
	 * @return the port
	 */
	public int getPort() {
		return port;
	}

	public void close() {

	}
}
