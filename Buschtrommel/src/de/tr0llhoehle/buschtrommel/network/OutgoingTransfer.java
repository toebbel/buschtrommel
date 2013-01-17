package de.tr0llhoehle.buschtrommel.network;

import java.io.ByteArrayInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

import de.tr0llhoehle.buschtrommel.LocalShareCache;
import de.tr0llhoehle.buschtrommel.models.GetFileMessage;
import de.tr0llhoehle.buschtrommel.models.FileRequestResponseMessage;
import de.tr0llhoehle.buschtrommel.models.GetFilelistMessage;
import de.tr0llhoehle.buschtrommel.models.Message;
import de.tr0llhoehle.buschtrommel.models.FileRequestResponseMessage.ResponseCode;

/**
 * Implements any outgoing transfer from this host to other hosts.
 * 
 * The transfer can be a file or a filelist. The hash of a filelist is "filelist". Depending on the requested data this class will send a FileRequestResponse-header or not.
 * @author tobi
 *
 */
public class OutgoingTransfer extends Transfer implements ITransferProgress {
	OutputStream net_out; // network stream to the requester
	java.io.InputStream ressourceStream; // stream from filelist / file
	LocalShareCache myShares; // all my shares

	private boolean keepThreadAlive;
	long numAvailableData; // max number of bytes to send
	boolean sendHeader;
	String filename;
	Message requestMessage;
	
	

	public OutgoingTransfer(Message m, OutputStream out, LocalShareCache myShares, InetSocketAddress partner) {
		super(partner);
		assert m instanceof GetFilelistMessage || m instanceof GetFileMessage;
		assert partner != null;
		requestMessage = m;
		this.net_out = out;
		this.myShares = myShares;
		this.transferType = TransferType.Outgoing;
		
		
		keepTransferAlive = true;
		keepThreadAlive = true;
		transferState = TransferStatus.Initialized;
		totalTransferedVolume = 0;
		
		if(m instanceof GetFileMessage) {
			sendHeader = true;
			hash = ((GetFileMessage)m).getHash();
		} else if (m instanceof GetFilelistMessage){
			sendHeader = false;
			hash = "filelist";
		}
	}

	/**
	 * Creates a stream that holds the data to send (filecontent or filelist) or
	 * null, if the requested ressource is not available
	 * 
	 * @param m
	 *            the request message
	 * @throws UnsupportedEncodingException 
	 */
	private void openInputStream(Message m) throws UnsupportedEncodingException {
		if (m instanceof GetFilelistMessage) {
			byte[] fileList = myShares.getAllShares().getBytes(Message.ENCODING);
			numAvailableData = fileList.length;
			ressourceStream = new ByteArrayInputStream(fileList);
			filename = "filelist";
		} else  if (m instanceof GetFileMessage){
			// do I know the file?
			if (!myShares.has(((GetFileMessage) m).getHash())) {
				logger.log(Level.INFO, "Requested file is not in share cache");
				ressourceStream = null; // file not available
				return;
			}

			// does the file exist?
			java.io.File file = new java.io.File(myShares.get(((GetFileMessage) m).getHash()).getPath());
			filename = file.getName();
			if (!file.exists()) {
				logger.log(Level.INFO, "Requested file is not found");
				ressourceStream = null;
				return;
			}
			numAvailableData = file.length();

			// open file for read
			logger.log(Level.INFO, "Open file for outgoing file transfer");
			try {
				ressourceStream = new java.io.FileInputStream(file);
			} catch (FileNotFoundException e) {
				logger.log(Level.INFO, "Requested file could not be opend");
				ressourceStream = null;
			}
			return;
		}
	}

	/**
	 * Handles the requested ranges of data
	 * 
	 * @param inStream
	 * @param request
	 */
	private void handleRequestedRanges(Message request) {
		if (request instanceof GetFilelistMessage) {
			offset = 0;
			expectedTransferVolume = numAvailableData;
		} else if (request instanceof GetFileMessage) {
			offset = ((GetFileMessage) request).getOffset();
			expectedTransferVolume = ((GetFileMessage) request).getLength();
		}
	}

	private void doTransfer() throws IOException {
		if (ressourceStream == null) {
			if(sendHeader)
				net_out.write((new FileRequestResponseMessage(ResponseCode.NEVER_TRY_AGAIN, 0).Serialize()).getBytes());
			
			net_out.close();
		} else {
			if (offset > numAvailableData) { // offset not in file
				logger.log(Level.INFO, "Requested offset is not valid: requested " + offset + ", length of file: "
						+ numAvailableData);
				
				if(sendHeader)
					net_out.write(new FileRequestResponseMessage(ResponseCode.OK, 0).Serialize().getBytes());
				
				net_out.close();
				transferState = TransferStatus.Finished;
				return;
			}

			if (offset + expectedTransferVolume > numAvailableData) { // requested
																	// Length
																	// too
																	// large?
																	// Shorten
																	// it!
				logger.log(Level.INFO, "Requested length of " + expectedTransferVolume + " was too large, shortened  it to "
						+ expectedTransferVolume);
				expectedTransferVolume = numAvailableData - offset;
			}

			// send the file
			if(sendHeader)
				net_out.write((new FileRequestResponseMessage(ResponseCode.OK, expectedTransferVolume).Serialize()).getBytes());
			
			int next; // Todo make buffer bigger
			ressourceStream.skip(offset);
			transferState = TransferStatus.Transfering;
			while (keepThreadAlive && totalTransferedVolume < expectedTransferVolume && (next = ressourceStream.read()) != -1) {
				net_out.write(next);
				totalTransferedVolume++;
			}
			net_out.close();
			ressourceStream.close();

			if (totalTransferedVolume == expectedTransferVolume)
				transferState = TransferStatus.Finished;
			else {
				if (!keepThreadAlive)
					transferState = TransferStatus.Canceled;
				else
					transferState = TransferStatus.LostConnection;
			}
		}
	}


	@Override
	public void cancel() {
		keepThreadAlive = false;
		transferState = TransferStatus.Canceled;
		try {
			net_out.close();
		} catch (IOException e) {
			// ignore
		}
	}

	@Override
	public List<ITransferProgress> getSubTransfers() {
		return new ArrayList<>();
	}


	@Override
	public void reset() {
		throw new UnsupportedOperationException("Outgoing transfers can't be reset");
	}

	@Override
	public void resumeTransfer() {
		throw new UnsupportedOperationException("Outgoing transfers can't be resumed");
	}

	@Override
	public String getTargetFile() {
		return filename;
	}

	@Override
	public void start() {
		(new Thread(new Runnable() {
			
			@Override
			public void run() {
				try {
					openInputStream(requestMessage);
				} catch (UnsupportedEncodingException e1) {
					logger.log(Level.SEVERE, "Unsupported encoding");
					ressourceStream = null;
				}
				handleRequestedRanges(requestMessage);
				try {
					doTransfer();
					keepTransferAlive = false;
				} catch (IOException e) { //catch *all* errors and do nothing, because we don't give a shit if someone doesn't get his candy
					logger.log(Level.SEVERE, "Could not handle outgoing file transfer: " + e.getMessage());
					cancel();
				}
			}
		})).start();
	}
}
