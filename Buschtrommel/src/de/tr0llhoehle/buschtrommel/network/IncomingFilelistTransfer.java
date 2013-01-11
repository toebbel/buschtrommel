package de.tr0llhoehle.buschtrommel.network;

import java.io.IOException;
import java.io.InputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;

import de.tr0llhoehle.buschtrommel.models.GetFilelistMessage;
import de.tr0llhoehle.buschtrommel.models.Host;
import de.tr0llhoehle.buschtrommel.models.Message;

public class IncomingFilelistTransfer extends MessageMonitor implements ITransferProgress {

	int length = 0;
	int transferedAmount;
	private TransferStatus status;
	private boolean alive;
	private java.util.logging.Logger logger;
	private Host host;
	
	public IncomingFilelistTransfer(Host host) {
		assert host != null;
		this.host = host;
		logger = java.util.logging.Logger.getLogger("incoming filelist from " + host.toString());
	}
	
	@Override
	public TransferType getType() {
		return TransferType.Singlesource;
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
		return transferedAmount;
	}

	@Override
	public String getExpectedHash() {
		return "";
	}

	@Override
	public TransferStatus getStatus() {
		return status;
	}

	@Override
	public void cancel() {
		alive = false;
		
	}

	@Override
	public void reset() {
		throw new UnsupportedOperationException("incoming filelist-transfers can't be reset");
		
	}

	@Override
	public void resumeTransfer() {
		throw new UnsupportedOperationException("incoming filelist-transfers can't be resumed");
		
	}

	@Override
	public void start() {
		logger = java.util.logging.Logger.getLogger("getfilelist " + getTransferPartner().toString());
		(new Thread(new Runnable() {
			
			@Override
			public void run() {
				Socket s;
				alive = true;
				try {
					status = TransferStatus.Connecting;
					s = new Socket(host.getAddress(), host.getPort());
					status = TransferStatus.Transfering;
					s.getOutputStream().write(new GetFilelistMessage().Serialize().getBytes(Message.ENCODING));
					processFilestream(s.getInputStream());
					s.close();
				} catch (IOException e) {
					logger.log(Level.SEVERE, "Can't get filelist: " + e.getMessage());
				}
			}
		})).start();
	}
	
	private void processFilestream(InputStream in) throws IOException {
		int next = 0;
		int received = 0;
		char[] buffer = new char[512];
		while((next = in.read()) != -1 && alive) {
			if(next != Message.MESSAGE_SPERATOR) {
				buffer[received++] = (char) next;
				continue;
			} else {
				String raw = String.valueOf(buffer, 0, received) + Message.MESSAGE_SPERATOR;
				Message result = MessageDeserializer.Deserialize(raw);
				if(result != null) {
					sendMessageToObservers(result);
				} else {
					logger.log(Level.SEVERE, "could not deserialize message: " + raw);
				}
				received = 0;
			}
		}
		if(!alive)
			status = TransferStatus.Canceled;
		else
			status = TransferStatus.Finished;
		alive = false;
	}

	@Override
	public boolean isActive() {
		return alive;
	}

	@Override
	public InetSocketAddress getTransferPartner() {
		return new InetSocketAddress(host.getAddress(), host.getPort());
	}

	@Override
	public String getTargetFile() {
		return "";
	}

	@Override
	public List<ITransferProgress> getSubTransfers() {
		return Collections.emptyList();
	}

	@Override
	public void RegisterLogHander(Handler h) {
		logger.addHandler(h);
	}

	@Override
	public void RemoveLogHander(Handler h) {
		logger.removeHandler(h);		
	}

	
}
