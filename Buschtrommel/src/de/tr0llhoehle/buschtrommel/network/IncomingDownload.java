package de.tr0llhoehle.buschtrommel.network;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

import de.tr0llhoehle.buschtrommel.HashFuncWrapper;
import de.tr0llhoehle.buschtrommel.LoggerWrapper;
import de.tr0llhoehle.buschtrommel.models.GetFileMessage;
import de.tr0llhoehle.buschtrommel.models.Host;
import de.tr0llhoehle.buschtrommel.models.Message;

public class IncomingDownload implements ITransferProgress {

	private String hash;
	private long offset;
	private long expectedTransferVolume;
	private long transferedVolume;
	private TransferStatus status;
	private boolean keepAlive;
	public static int MAX_RETRIES = 3;
	private int retries;
	private java.io.File targetPath;

	public IncomingDownload(String hash, Host host, long offset, long length) {
		this.offset = offset;
		this.hash = hash;
		this.expectedTransferVolume = length;
		this.transferedVolume = 0;
		retries = 0;
		this.status = TransferStatus.establishing;
		keepAlive = true;

		while (keepAlive && retries < MAX_RETRIES) {
			java.net.Socket s = null;
			InputStream in = null;
			OutputStream out = null;
			try {
				s = new Socket(host.getAddress(), host.getPort());
				s.connect(new InetSocketAddress(host.getAddress(), host.getPort()));
				out = s.getOutputStream();
				out.write(new GetFileMessage(hash, offset, length).Serialize().getBytes());
				in = s.getInputStream();
				byte[] buffer = new byte[38];
				in.read(buffer, 0, 26);
				String responseHeader = String.valueOf(in).trim();
				switch (responseHeader.substring(0, 26).toUpperCase()) {
				case "FILE TRANSFER RESPONSE|OK|":
					String str_expectedTransferVolume = "";
					int next;
					while((next = in.read())!= -1 && next != Message.MESSAGE_SPERATOR)
						str_expectedTransferVolume += String.valueOf(next);
					expectedTransferVolume = Long.valueOf(str_expectedTransferVolume);
					java.io.FileOutputStream fs = new java.io.FileOutputStream(targetPath);
					while((next = in.read()) != -1 && transferedVolume < expectedTransferVolume) {
						fs.write(next);
					}
					if(transferedVolume == expectedTransferVolume) {
						if(HashFuncWrapper.hash(targetPath.getAbsolutePath()) == getExpectedHash())
							status = TransferStatus.finished;
						else
							status = TransferStatus.invalidChecksum;
					} else {
						status = TransferStatus.canceled;
					}
					break;
				case "FILE TRANSFER RESPONSE|NEV":
					retries = MAX_RETRIES;
					break;
				case "FILE TRANSFER RESPONSE|TRY":
					Thread.currentThread().sleep(5000);
					retries++;
					break;
				}

				s.close();
				out.close();
				in.close();
			} catch (IOException e) {
				LoggerWrapper.logError("could not connect to host " + e.getMessage());
				status = TransferStatus.error;
				return;
			} catch (InterruptedException e) {
				LoggerWrapper.logError("InterruptedException while waiting for another rettry of file download");
				try {
					s.close();
				} catch (IOException e1) {}
				return;
			}
		}

	}

	@Override
	public TransferType getType() {
		return TransferType.Singlesource;
	}

	@Override
	public long getLength() {
		return expectedTransferVolume;
	}

	@Override
	public long getTotalLength() {
		return getLength();
	}

	@Override
	public long getOffset() {
		return offset;
	}

	@Override
	public long getExpectedTransferVolume() {
		return expectedTransferVolume;
	}

	@Override
	public long getTransferedAmount() {
		return transferedVolume;
	}

	@Override
	public String getExpectedHash() {
		return hash;
	}

	@Override
	public TransferStatus getStatus() {
		return status;
	}

	@Override
	public void cancel() {
		keepAlive = false;
	}

	@Override
	public List<ITransferProgress> getSubTransfers() {
		return new ArrayList<>();
	}

}
