package de.tr0llhoehle.buschtrommel.network;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import de.tr0llhoehle.buschtrommel.LoggerWrapper;
import de.tr0llhoehle.buschtrommel.ShareCache;
import de.tr0llhoehle.buschtrommel.models.File;
import de.tr0llhoehle.buschtrommel.models.FileRequestResponseMessage;
import de.tr0llhoehle.buschtrommel.models.GetFileMessage;
import de.tr0llhoehle.buschtrommel.models.Message;
import de.tr0llhoehle.buschtrommel.models.FileRequestResponseMessage.ResponseCode;
import de.tr0llhoehle.buschtrommel.network.ITransferProgress.TransferStatus;

public class OutgoingFileTransfer extends Thread implements ITransferProgress{
	GetFileMessage m;
	OutputStream out;
	ShareCache myShares;
	long realLength;
	private boolean keepAlive;
	long transferedData;
	TransferStatus status;
	
	public OutgoingFileTransfer(GetFileMessage m, OutputStream out, ShareCache myShares) {
		this.m = m;
		status = TransferStatus.establishing;
		this.out = out;
		this.myShares = myShares;
		realLength = m.getLength();
		transferedData = 0;
		try {
			keepAlive = true;
			handleGetFile();
		} catch (IOException e) {
			
		}
	}
	
	private void handleGetFile() throws IOException {
		if(!myShares.has(m.getHash())) {
			out.write((new FileRequestResponseMessage(ResponseCode.NEVER_TRY_AGAIN, 0).Serialize() + Message.MESSAGE_SPERATOR).getBytes());
			out.close();
		} else {
			File f = myShares.get(m.getHash());
			if(m.getOffset() > f.getLength()) { //offset not in file
				LoggerWrapper.logInfo("Requested offset is not valid");
				out.write(new FileRequestResponseMessage(ResponseCode.OK, 0).Serialize().getBytes());
				out.close();
				status = TransferStatus.canceled;
				return;
			} 
			
			if (m.getOffset() + realLength > f.getLength()) { //requested Length too large? Shorten it!
				realLength = f.getLength() - m.getOffset();
				LoggerWrapper.logInfo("Requested length of " + m.getLength() + " was too large, shortened  it to " + realLength);
			}
			
			java.io.FileInputStream fileStream = new FileInputStream(myShares.get(m.getHash()).getPath());
			
			//send the file
			out.write((new FileRequestResponseMessage(ResponseCode.OK, realLength).Serialize() + Message.FIELD_SEPERATOR).getBytes());
			int next;
			fileStream.skip(m.getOffset());
			while(keepAlive && transferedData < realLength && (next = fileStream.read()) != -1) {
				out.write(next);
				transferedData++;
				status = TransferStatus.transfering;
			}
			out.close();
			fileStream.close();
			
			if(transferedData == realLength)
				status = TransferStatus.finished;
			else {
				if(!keepAlive)
					status = TransferStatus.canceled;
				else
					status = TransferStatus.error;
			}
		}
	}

	@Override
	public TransferType getType() {
		return TransferType.Outgoing;
	}

	@Override
	public long getLength() {
		return realLength;
	}

	@Override
	public long getTotalLength() {
		return getLength();
	}

	@Override
	public long getOffset() {
		return m.getOffset();
	}

	@Override
	public long getExpectedTransferVolume() {
		return getLength();
	}

	@Override
	public String getExpectedHash() {
		return m.getHash();
	}

	@Override
	public void cancel() {
		keepAlive = false;
	}

	@Override
	public List<ITransferProgress> getSubTransfers() {
		return new ArrayList<>();
	}

	@Override
	public long getTransferedAmount() {
		return transferedData;
	}

	@Override
	public TransferStatus getStatus() {
		// TODO Auto-generated method stub
		return null;
	}
}
