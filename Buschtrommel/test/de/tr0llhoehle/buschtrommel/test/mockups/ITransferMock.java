package de.tr0llhoehle.buschtrommel.test.mockups;

import java.net.InetSocketAddress;
import java.util.List;
import java.util.logging.Handler;

import de.tr0llhoehle.buschtrommel.network.ITransferProgress;

public class ITransferMock implements ITransferProgress {

	public String name;


	public boolean active;
	public long length;
	public long transfered;
	
	
	public ITransferMock(String name, boolean active, long length, long transfered) {
		super();
		this.name = name;
		this.active = active;
		this.length = length;
		this.transfered = transfered;
	}

	@Override
	public TransferType getType() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public long getLength() {
		// TODO Auto-generated method stub
		return length;
	}

	@Override
	public long getOffset() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public long getTransferedAmount() {
		// TODO Auto-generated method stub
		return transfered;
	}

	@Override
	public String getExpectedHash() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public TransferStatus getStatus() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void cancel() {
		// TODO Auto-generated method stub

	}

	@Override
	public void reset() {
		// TODO Auto-generated method stub

	}

	@Override
	public void resumeTransfer() {
		// TODO Auto-generated method stub

	}

	@Override
	public void start() {
		// TODO Auto-generated method stub

	}

	@Override
	public boolean isActive() {
		// TODO Auto-generated method stub
		return active;
	}

	@Override
	public InetSocketAddress getTransferPartner() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getTargetFile() {
		// TODO Auto-generated method stub
		return name;
	}

	@Override
	public List<ITransferProgress> getSubTransfers() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void RegisterLogHander(Handler h) {
		// TODO Auto-generated method stub

	}

	@Override
	public void RemoveLogHander(Handler h) {
		// TODO Auto-generated method stub

	}

}
