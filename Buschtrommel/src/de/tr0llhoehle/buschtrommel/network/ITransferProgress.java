package de.tr0llhoehle.buschtrommel.network;

import java.util.List;

public interface ITransferProgress {
	public TransferType getType();
	public long getLength();
	public long getTotalLength();
	public long getOffset();
	public long getExpectedTransferVolume();
	public String getExpectedHash();
	public void cancel();
	public List<ITransferProgress> getSubTransfers();
	
	public enum TransferType {
		Multisource,
		Singlesource,
		Outgoing
	}
}

