package de.tr0llhoehle.buschtrommel.network;

import java.io.File;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Timer;
import java.util.TimerTask;

import javax.net.ssl.SSLEngineResult.Status;

import de.tr0llhoehle.buschtrommel.models.GetFileMessage;
import de.tr0llhoehle.buschtrommel.models.Host;

public class MultisourceDownload extends Transfer {

	IncomingDownload[] subTransfers;
	final static int MAX_DOWNLOADS = 5;
	IHostPortResolver hostResolver;
	java.io.File targetFile;
	HashMap<InetAddress, Host> hosts;
	Timer watchDog;
	
	public MultisourceDownload(ArrayList<InetSocketAddress> partners, GetFileMessage file, java.io.File targetFile, IHostPortResolver hostResolver) {
		super(null);
		assert partners.size() > 0;
		this.hash = file.getHash();
		this.hostResolver = hostResolver;
		this.targetFile = targetFile;
		hosts = new HashMap<>();
		watchDog = new Timer();
		
		//check if ports for all hosts are known
		for(InetSocketAddress i : partners) {
			Host tmp = hostResolver.getOrCreateHost(i.getAddress());
			if(tmp.getPort() != Host.UNKNOWN_PORT)
				hosts.put(i.getAddress(), tmp);
		}
		
		//spawn subThreads
		subTransfers = spawnDownloads(hosts.size(), hash, file.getOffset(), file.getLength(), new ArrayList<Host>(hosts.values()), hostResolver);
		transferState = TransferStatus.Initialized;
	}
	
	private static IncomingDownload[] spawnDownloads(int num, String hash, long start_offset, long total_length, ArrayList<Host> hosts, IHostPortResolver hostResolver) {
		IncomingDownload[] subTransfers = new IncomingDownload[Math.min(MAX_DOWNLOADS, num)];
		long segmentSize = total_length / num;
		long length = segmentSize;
		long offset = start_offset;
		java.io.File tmpFile;
		int currentHost = 0;
		for(int i = 0; i < num; i++) {
			if(i == num - 1)  //last segment may be bigger than others
				length = total_length - ((num - 1) * segmentSize);
			
			if(currentHost >= hosts.size())
				currentHost = 0;
			
			//allocate file and host
			tmpFile = new File(hash + "-" + num + ".tmp");
			Host host = hosts.get(currentHost++);
			
			//creat sub-Transfer
			subTransfers[i] = new IncomingDownload(new GetFileMessage(hash, offset, length), host, tmpFile, hostResolver);
		}
		return subTransfers;
	}

	@Override
	public void cancel() {
		watchDog.cancel();
		for(Transfer t : subTransfers)
			t.cancel();
		transferState = TransferStatus.Canceled;
	}

	@Override
	public void reset() {
		watchDog.cancel();
		for(Transfer t : subTransfers)
			t.reset();
		transferState = TransferStatus.Initialized;
	}

	@Override
	public void resumeTransfer() {
		for(Transfer t : subTransfers)
			if(t.getStatus() == TransferStatus.TemporaryNotAvailable || 
			t.getStatus() == TransferStatus.ConnectionFailed ||
			t.getStatus() == TransferStatus.LostConnection)
				t.resumeTransfer();
		scheduleWatchDog();
	}

	@Override
	public void start() {
		for(Transfer t : subTransfers) {
			if(t.getStatus() != TransferStatus.Initialized)
				throw new IllegalStateException("can't start because a sub-transfer is not initialized");
			t.start();
		}
		scheduleWatchDog();
	}
	
	private  void scheduleWatchDog(){
		watchDog.scheduleAtFixedRate(new TimerTask() {
			@Override
			public void run() {
				//TODO continue
			}
		}, 5000, 500);
	}

	@Override
	public String getTargetFile() {
		return targetFile.getAbsolutePath();
	}

}
