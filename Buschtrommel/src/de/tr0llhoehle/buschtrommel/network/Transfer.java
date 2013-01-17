package de.tr0llhoehle.buschtrommel.network;

import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Handler;

import de.tr0llhoehle.buschtrommel.network.ITransferProgress.TransferStatus;

public abstract class Transfer extends MessageMonitor implements ITransferProgress {

	protected InetSocketAddress partner;
	protected java.util.logging.Logger logger;
	protected TransferType transferType;
	protected String hash;
	protected TransferStatus transferState;
	protected long expectedTransferVolume;
	protected long offset;
	protected long totalTransferedVolume;
	protected boolean keepTransferAlive;

	public Transfer(InetSocketAddress partner) {
		this.partner = partner;
	}

	@Override
	public TransferType getType() {
		return transferType;
	}

	@Override
	public long getLength() {
		return expectedTransferVolume;
	}

	@Override
	public long getOffset() {
		return offset;
	}

	@Override
	public long getTransferedAmount() {
		return totalTransferedVolume;
	}

	@Override
	public String getExpectedHash() {
		return hash;
	}

	@Override
	public TransferStatus getStatus() {
		return transferState;
	}

	@Override
	public List<ITransferProgress> getSubTransfers() {
		return new ArrayList<>();
	}

	@Override
	public InetSocketAddress getTransferPartner() {
		return partner;
	}

	@Override
	public void RegisterLogHander(Handler h) {
		logger.addHandler(h);
	}

	@Override
	public void RemoveLogHander(Handler h) {
		logger.removeHandler(h);
	}
	
	@Override
	public boolean isActive() {
		return keepTransferAlive;
	}
}
