package de.tr0llhoehle.buschtrommel.test.mockups;

import java.net.InetSocketAddress;
import java.util.List;
import java.util.logging.Handler;
import java.util.logging.Logger;

import de.tr0llhoehle.buschtrommel.network.ITransferProgress;

public class ITransferMock implements ITransferProgress {

	public String name;

	public boolean active;
	public long length;
	public long transfered;

	public TransferStatus status;

	public ITransferMock(String name, boolean active, long length, long transfered, TransferStatus status) {
		super();
		this.name = name;
		this.active = active;
		this.length = length;
		this.transfered = transfered;
		this.status = status;
	}

	@Override
	public TransferType getType() {
		return null;
	}

	@Override
	public long getLength() {
		return length;
	}

	@Override
	public long getOffset() {
		return 0;
	}

	@Override
	public long getTransferedAmount() {
		return transfered;
	}

	@Override
	public String getExpectedHash() {
		return null;
	}

	@Override
	public TransferStatus getStatus() {
		return status;
	}

	@Override
	public void cancel() {

	}

	@Override
	public void reset() {

	}

	@Override
	public void resumeTransfer() {
	}

	@Override
	public void start() {
	}

	@Override
	public boolean isActive() {
		return active;
	}

	@Override
	public InetSocketAddress getTransferPartner() {
		return null;
	}

	@Override
	public String getTargetFile() {
		return name;
	}

	@Override
	public List<ITransferProgress> getSubTransfers() {
		return null;
	}

	@Override
	public void RegisterLogHander(Handler h) {
	}

	@Override
	public void RemoveLogHander(Handler h) {
	}

	@Override
	public void cleanup() {

	}

	@Override
	public void SetLoggerParent(Logger l) {
	}

}
