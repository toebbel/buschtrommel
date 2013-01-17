package de.tr0llhoehle.buschtrommel.network;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Handler;
import java.util.logging.Level;

import de.tr0llhoehle.buschtrommel.HashFuncWrapper;
import de.tr0llhoehle.buschtrommel.models.FileRequestResponseMessage;
import de.tr0llhoehle.buschtrommel.models.GetFileMessage;
import de.tr0llhoehle.buschtrommel.models.Host;
import de.tr0llhoehle.buschtrommel.models.Message;

/**
 * This class represents a download from a host to this client (single source).
 * It runs in an independend thread, restarts the process if neccesaray and
 * creates all local resources (such as target file and a new socket).
 * 
 * @author Tobias Sturm
 * 
 */
public class IncomingDownload extends Transfer implements ITransferProgress {

	boolean checkIntegrity;
	File targetFile;
	GetFileMessage sourceFile;
	FileOutputStream targetFilestream;
	java.net.Socket socket;
	Thread self;
	
	public IncomingDownload(GetFileMessage sourceFile, Host host, java.io.File target) {
		super(new InetSocketAddress(host.getAddress(), host.getPort()));
		this.offset = sourceFile.getOffset();
		this.sourceFile = sourceFile;
		this.hash = sourceFile.getHash();
		sourceFile.getLength();
		expectedTransferVolume = sourceFile.getLength();
		this.totalTransferedVolume = 0;
		checkIntegrity = true;
		this.targetFile = target;
		transferState = TransferStatus.Initialized;
		this.partner = new InetSocketAddress(host.getAddress(), host.getPort());
		logger = java.util.logging.Logger.getLogger("incoming " + hash + " " + partner.toString());
		transferType  = TransferType.Singlesource;
	}

	public void DisableIntegrityCheck() {
		hash = ""; //TODO use checkIntegrity = false instead
	}


	protected void doTransfer() throws UnsupportedEncodingException {
		logger.log(Level.INFO, "Starting download");

		if (transferState == TransferStatus.Initialized) {
			try {
				logger.log(Level.INFO, "open stream to '" + targetFile.getPath() + "'");
				targetFilestream = new FileOutputStream(targetFile, false);
			} catch (FileNotFoundException e) {
				logger.log(Level.SEVERE, "Could not create target filestream: " + e.getMessage());
				transferState = TransferStatus.LocalIOError;
				targetFilestream = null;
				return;
			}
		}

		// connect and send request
		transferState = TransferStatus.Connecting;
		logger.log(Level.INFO, "Connecting to " + partner.getAddress() + ":" + partner.getPort());
		expectedTransferVolume = sourceFile.getLength() - totalTransferedVolume;
		assert socket == null;
		InputStream in = null;
		OutputStream out = null;
		try {
			socket = new Socket();
			socket.connect(new InetSocketAddress(partner.getAddress(), partner.getPort()));

			out = socket.getOutputStream();
			in = socket.getInputStream();
			out.write(new GetFileMessage(sourceFile.getHash(), offset + totalTransferedVolume, sourceFile.getLength()
					- totalTransferedVolume).Serialize().getBytes(Message.ENCODING));
			out.flush();
		} catch (IOException e) {
			logger.log(Level.SEVERE, "could not connect to host '" + e.getMessage() + "'");
			transferState = TransferStatus.ConnectionFailed;
		}

		// check response
		FileRequestResponseMessage rsp = handleResponse(in);
		if (rsp != null && transferState == TransferStatus.Connecting) {
			rsp.setSource(new InetSocketAddress(partner.getAddress(), partner.getPort()));
			sendMessageToObservers(rsp);
		} else {
			closeSocket();
			return;
		}

		// not ready for transfer? abort
		switch (rsp.getResponseCode()) {
		case NEVER_TRY_AGAIN:
			transferState = TransferStatus.PermanentlyNotAvailable;
			return;
		case TRY_AGAIN_LATER:
			transferState = TransferStatus.TemporaryNotAvailable;
			return;
		default:
		}

		// check returned expected transfer volume
		if (rsp.getExpectedVolume() > expectedTransferVolume) {
			logger.log(Level.INFO, "Host sent a bigger transferVolume than expected ");
			transferState = TransferStatus.InvalidContent;
			closeSocket();
			return;
		} else if (rsp.getExpectedVolume() < expectedTransferVolume) {
			logger.log(Level.INFO, "Host announced that file will be smaller than expected: " + rsp.getExpectedVolume()
					+ " bytes");
			expectedTransferVolume = rsp.getExpectedVolume();
		}

		// Transfer bytes
		transferState = TransferStatus.Transfering;
		int next; // TODO make buffer bigger than 1 byte
		keepTransferAlive = true;
		long transferedVolume = 0;
		try {
			while (transferedVolume < expectedTransferVolume && (next = in.read()) != -1 && keepTransferAlive) {
				totalTransferedVolume++;
				transferedVolume++;
				try {
					targetFilestream.write(next);
				} catch (IOException e) {
					logger.log(Level.SEVERE, "Could not write into target file: " + e.getMessage());
					transferState = TransferStatus.LocalIOError;
					closeSocket();
					closeFile(true);
					return;
				}
			}
			if (transferedVolume < expectedTransferVolume) {
				logger.log(Level.SEVERE, "Lost connection");
				transferState = TransferStatus.LostConnection;
				closeSocket();
				return;
			} else { // download completed
				transferState = TransferStatus.CheckingHash;
				logger.log(Level.INFO, "finished data transfer");
				closeFile(false);
				closeSocket();
			}
		} catch (IOException e2) {
			logger.log(Level.SEVERE, "Other side closed connection");
			transferState = TransferStatus.LostConnection;
			closeSocket();
			return;
		}

		// integrity check
		if (transferState == TransferStatus.CheckingHash) {
			integrityCheck(targetFile);
		}
	}

	/**
	 * Closes the socket and logs exceptions
	 */
	private void closeSocket() {
		try {
			socket.close();
		} catch (IOException e) {
			logger.log(Level.SEVERE, "Could not close network socket: " + e.getMessage());
		}
		socket = null;
	}

	/**
	 * Closes the target filestream, if it is not null. If there occurs an
	 * exception, the status is set to localIOError. Optional deletes the file
	 * (if it exists).
	 * 
	 * @param delete
	 *            wether to delete the target file or not
	 */
	private void closeFile(boolean delete) {
		if (targetFilestream != null) {
			try {
				targetFilestream.close();
				targetFilestream = null;
			} catch (IOException e1) {
				logger.log(Level.SEVERE, "Could not close target file");
				transferState = TransferStatus.LocalIOError;
				return;
			}
		}

		if (delete && targetFile.exists()) {
			targetFile.delete();
		}
	}

	/**
	 * Checks the hash of the transfered file.
	 * 
	 * Skips checks if the expected hash is empty. If the file can't be read for
	 * hashing, the state will set to LocalIOError
	 * 
	 * @param targetPath
	 */
	protected void integrityCheck(File targetPath) {
		if (getExpectedHash() == "") {
			transferState = TransferStatus.Finished;
		} else {
			try {
				if (HashFuncWrapper.hash(targetPath.getAbsolutePath()) == getExpectedHash()) {
					logger.log(Level.INFO, "File has expected hash");
					transferState = TransferStatus.Finished;
				} else {
					logger.log(Level.INFO, "File has invalid hash");
					transferState = TransferStatus.InvalidContent;
				}
			} catch (IOException e) {
				logger.log(Level.SEVERE, "Could not check file integrity: " + e.getMessage());
				transferState = TransferStatus.LocalIOError;
			}
		}
	}

	/**
	 * Reads the FileRequestResponse Message from a stream.
	 * 
	 * Parses the message and returns it. If there is a number-exception the
	 * response will be treated as TRY AGAIN. If the message can't be parsed, it
	 * will be treated as TRY AGAIN.
	 * 
	 * May update the state, if the connection is lost.
	 * 
	 * @param in
	 *            the network stream
	 * @return FileRequestResponseMessage or null
	 * @throws UnsupportedEncodingException
	 *             if needed encoding is not available
	 */
	protected FileRequestResponseMessage handleResponse(InputStream in) throws UnsupportedEncodingException {
		FileRequestResponseMessage try_again = new FileRequestResponseMessage(
				FileRequestResponseMessage.ResponseCode.TRY_AGAIN_LATER, 0);
		int probe_len = FileRequestResponseMessage.TYPE_FIELD.length() + 4;

		byte[] buffer = new byte[probe_len];
		try {
			in.read(buffer, 0, probe_len);
		} catch (IOException e) {
			logger.log(Level.SEVERE, "Could not read response stream: '" + e.getMessage()
					+ "' - assuming 'NEVER TRY AGAIN'");
			return try_again;
		}
		String responseHeader = new String(buffer, Message.ENCODING).trim();
		switch (responseHeader.toUpperCase()) {
		case FileRequestResponseMessage.TYPE_FIELD + Message.FIELD_SEPERATOR + "OK":
			String str_expectedTransferVolume = "";
			int next;
			try {
				while ((next = in.read()) != -1 && next != Message.MESSAGE_SPERATOR)
					str_expectedTransferVolume += new String(new byte[] { (byte) next }, Message.ENCODING);
			} catch (IOException e) {
				logger.log(Level.SEVERE, "Could not understand 'expected transfer volume' in response");
				transferState = TransferStatus.LostConnection;
				return null;
			}
			try {
				long expectedTransferVolume = Long.valueOf(str_expectedTransferVolume);
				return new FileRequestResponseMessage(FileRequestResponseMessage.ResponseCode.OK,
						expectedTransferVolume);
			} catch (NumberFormatException ex) {
				logger
						.log(Level.SEVERE, "Response contained invalid 'expected transfer volume' - assuming 'TRY AGAIN LATER'");
				return try_again;
			}
		case FileRequestResponseMessage.TYPE_FIELD + Message.FIELD_SEPERATOR + "TRY":
			logger.log(Level.INFO, "Received 'try again later from host'");
			return try_again;
		case FileRequestResponseMessage.TYPE_FIELD + Message.FIELD_SEPERATOR + "NEV":
			logger.log(Level.INFO, "Received 'never try agiain from host'");
			return new FileRequestResponseMessage(FileRequestResponseMessage.ResponseCode.NEVER_TRY_AGAIN, 0);
		default:
			logger.log(Level.INFO, "Received garbage: '" + responseHeader + "'");
			return try_again;
		}
	}

	@Override
	public void reset() {
		if(isActive()) {
			logger.log(Level.SEVERE, "Can't reset an active transfer. Cancel it before");
			return;
		}
		closeSocket();
		closeFile(targetFile.exists());
		transferState = TransferStatus.Initialized;
	}

	@Override
	public void resumeTransfer() {
		if(isActive()) {
			logger.log(Level.SEVERE, "Can't resume an active transfer. Cancel it before");
			return;
		}
		logger.log(Level.INFO, "Resume transfer");
		if (socket != null) {
			logger.log(Level.SEVERE, "Can't resume transfer if socket still exists");
			return;
		}

		if (targetFilestream == null) {
			logger.log(Level.SEVERE, "target filestream doesn't exist anymore!");
			transferState = TransferStatus.LocalIOError;
		}
		
		self = getCreateOwnThread();
		self.start();
	}
	
	
	public void start() {
		self = getCreateOwnThread();
		self.start();
	}
	
	private Thread getCreateOwnThread() {
		Thread t = new Thread(new Runnable() {

			@Override
			public void run() {
				try {
					if(transferState == TransferStatus.AssembleParts || transferState == TransferStatus.CheckingHash || transferState == TransferStatus.Connecting || transferState == TransferStatus.Finished || transferState == TransferStatus.Transfering)
						logger.log(Level.SEVERE, "Can't start download, state is " + transferState);
					else {
						keepTransferAlive = true;
						doTransfer();
					}
				} catch (UnsupportedEncodingException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				keepTransferAlive = false;
				self = null;
			}
		});
		return t;
	}
	

	@Override
	public void cancel() {
		keepTransferAlive = false;
	}

	@Override
	public String getTargetFile() {
		return targetFile.getName();
	}


}
