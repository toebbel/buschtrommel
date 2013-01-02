package de.tr0llhoehle.buschtrommel.network;

import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import de.tr0llhoehle.buschtrommel.ShareCache;

public class OutgoingFilelistTransfer extends Thread implements ITransferProgress {

	byte[] data;
	private boolean keepAlive;
	private int transferedData;
	private TransferStatus status;
	OutputStream out;

	public OutgoingFilelistTransfer(OutputStream out, ShareCache share) {
		data = share.getAllShares().getBytes();
		status = TransferStatus.establishing;
		transferedData = 0;
		keepAlive = true;
		this.out = out;
		run();
	}

	@Override
	public void run() {
		status = TransferStatus.transfering;
		try {
			while (transferedData < data.length && keepAlive) {
				out.write(data[transferedData++]);
			}
		} catch (IOException e) {
			status = TransferStatus.otherSideCanceled;
			return;
		} 
		if(keepAlive)
			status = TransferStatus.finished;
		else
			status = TransferStatus.canceled;
	}

	@Override
	public TransferType getType() {
		return TransferType.Outgoing;
	}

	@Override
	public long getLength() {
		return data.length;
	}

	@Override
	public long getTotalLength() {
		return getLength();
	}

	@Override
	public long getOffset() {
		return 0;
	}

	@Override
	public long getExpectedTransferVolume() {
		return getLength();
	}

	@Override
	public String getExpectedHash() {
		return "own filelist";
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
		return status;
	}

}
