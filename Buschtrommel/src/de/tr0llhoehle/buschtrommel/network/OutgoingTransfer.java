package de.tr0llhoehle.buschtrommel.network;

import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.List;

import de.tr0llhoehle.buschtrommel.LoggerWrapper;
import de.tr0llhoehle.buschtrommel.ShareCache;
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
public class OutgoingTransfer extends Thread implements ITransferProgress {
	OutputStream net_out; // network stream to the requester
	java.io.InputStream ressourceStream; // stream from filelist / file
	ShareCache myShares; // all my shares

	private boolean keepThreadAlive;
	long offset; // number of bytes to skip in ressourceStream before send
	long numTransferedData; // number of bytes transfered
	long numDataToTransfer; // number of bytes to transfer
	long numAvailableData; // max number of bytes to send
	boolean sendHeader;

	TransferStatus transferState;
	boolean transferIsActive;
	private String hash;
	private InetSocketAddress partner;

	public OutgoingTransfer(Message m, OutputStream out, ShareCache myShares, InetSocketAddress partner) {
		assert m instanceof GetFilelistMessage || m instanceof GetFileMessage;
		this.net_out = out;
		this.myShares = myShares;
		this.partner = partner;
		
		transferIsActive = true;
		keepThreadAlive = true;
		transferState = TransferStatus.Initialized;
		numTransferedData = 0;
		
		if(m instanceof GetFileMessage) {
			sendHeader = true;
			hash = ((GetFileMessage)m).getHash();
		} else if (m instanceof GetFilelistMessage){
			sendHeader = false;
			hash = "filelist";
		}
		
		try {
			openInputStream(m);
		} catch (UnsupportedEncodingException e1) {
			LoggerWrapper.logError("Unsupported encoding");
			ressourceStream = null;
		}
		handleRequestedRanges(m);
		
		try {
			doTransfer();
		} catch (IOException e) { //catch *all* errors and do nothing, because we don't give a shit if someone doesn't get his candy
			LoggerWrapper.logError("Could not handle outgoing file transfer: " + e.getMessage());
			cancel();
		}
		transferIsActive = false;
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
		} else  if (m instanceof GetFileMessage){
			// do I know the file?
			if (!myShares.has(((GetFileMessage) m).getHash())) {
				LoggerWrapper.logInfo("Requested file is not in share cache");
				ressourceStream = null; // file not available
				return;
			}

			// does the file exist?
			java.io.File file = new java.io.File(myShares.get(((GetFileMessage) m).getHash()).getPath());
			if (!file.exists()) {
				LoggerWrapper.logInfo("Requested file is not found");
				ressourceStream = null;
				return;
			}
			numAvailableData = file.length();

			// open file for read
			LoggerWrapper.logInfo("Open file for outgoing file transfer");
			try {
				ressourceStream = new java.io.FileInputStream(file);
			} catch (FileNotFoundException e) {
				LoggerWrapper.logInfo("Requested file could not be opend");
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
			numDataToTransfer = numAvailableData;
		} else if (request instanceof GetFileMessage) {
			offset = ((GetFileMessage) request).getOffset();
			numDataToTransfer = ((GetFileMessage) request).getLength();
		}
	}

	private void doTransfer() throws IOException {
		if (ressourceStream == null) {
			if(sendHeader)
				net_out.write((new FileRequestResponseMessage(ResponseCode.NEVER_TRY_AGAIN, 0).Serialize()).getBytes());
			
			net_out.close();
		} else {
			if (offset > numAvailableData) { // offset not in file
				LoggerWrapper.logInfo("Requested offset is not valid: requested " + offset + ", length of file: "
						+ numAvailableData);
				
				if(sendHeader)
					net_out.write(new FileRequestResponseMessage(ResponseCode.OK, 0).Serialize().getBytes());
				
				net_out.close();
				transferState = TransferStatus.Finished;
				return;
			}

			if (offset + numDataToTransfer > numAvailableData) { // requested
																	// Length
																	// too
																	// large?
																	// Shorten
																	// it!
				LoggerWrapper.logInfo("Requested length of " + numDataToTransfer + " was too large, shortened  it to "
						+ numDataToTransfer);
				numDataToTransfer = numAvailableData - offset;
			}

			// send the file
			if(sendHeader)
				net_out.write((new FileRequestResponseMessage(ResponseCode.OK, numDataToTransfer).Serialize()).getBytes());
			
			int next; // Todo make buffer bigger
			ressourceStream.skip(offset);
			transferState = TransferStatus.Transfering;
			while (keepThreadAlive && numTransferedData < numDataToTransfer && (next = ressourceStream.read()) != -1) {
				net_out.write(next);
				numTransferedData++;
			}
			net_out.close();
			ressourceStream.close();

			if (numTransferedData == numDataToTransfer)
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
	public TransferType getType() {
		return TransferType.Outgoing;
	}

	@Override
	public long getLength() {
		return numDataToTransfer;
	}

	@Override
	public long getOffset() {
		return offset;
	}

	@Override
	public String getExpectedHash() {
		return hash;
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
	public long getTransferedAmount() {
		return numTransferedData;
	}

	@Override
	public TransferStatus getStatus() {
		return transferState;
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
	public boolean isActive() {
		return transferIsActive;
	}

	@Override
	public InetSocketAddress getTransferPartner() {
		return partner;
	}
}
