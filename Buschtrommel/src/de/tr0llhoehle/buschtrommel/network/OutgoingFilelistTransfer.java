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
	boolean active;

	public OutgoingFilelistTransfer(OutputStream out, ShareCache share) {
		data = share.getAllShares().getBytes();
		status = TransferStatus.Initialized;
		transferedData = 0;
		keepAlive = true;
		this.out = out;
		active = true;
		start();
	}

	@Override
	public void run() {
		status = TransferStatus.Transfering;
		try {
			while (transferedData < data.length && keepAlive) {
				out.write(data[transferedData++]);
			}
		} catch (IOException e) {
			status = TransferStatus.LostConnection;
			active = false;
			return;
		} 
		if(keepAlive)
			status = TransferStatus.Finished;
		else
			status = TransferStatus.Canceled;
		active = false;
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
	public long getOffset() {
		return 0;
	}


	@Override
	public String getExpectedHash() {
		return "own filelist";
	}

	@Override
	public void cancel() {
		keepAlive = false;
		status = TransferStatus.Canceled;
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

	@Override
	public void reset() {
		throw new UnsupportedOperationException("Outgoing filelist transfers can't be reset");
		
	}

	@Override
	public void resumeTransfer() {
		throw new UnsupportedOperationException("Outgoing filelist transfers can't be resumed");
	}

	@Override
	public boolean isActive() {
		return active;
	}

}
