package de.tr0llhoehle.buschtrommel.network;

import java.util.List;

public interface ITransferProgress {
	public TransferType getType();
	public long getLength();
	public long getTotalLength();
	public long getOffset();
	public long getExpectedTransferVolume();
	public long getTransferedAmount();
	public String getExpectedHash();
	public TransferStatus getStatus();
	public void cancel();
	public List<ITransferProgress> getSubTransfers();
	
	public enum TransferType {
		Multisource,
		Singlesource,
		Outgoing
	}
	
	public enum TransferStatus {
		establishing,
		transfering,
		otherSideCanceled,
		canceled,
		error,
		finished
	}
}

