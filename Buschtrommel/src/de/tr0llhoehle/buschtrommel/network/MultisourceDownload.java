package de.tr0llhoehle.buschtrommel.network;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Timer;
import java.util.TimerTask;

import de.tr0llhoehle.buschtrommel.HashFuncWrapper;
import de.tr0llhoehle.buschtrommel.models.GetFileMessage;
import de.tr0llhoehle.buschtrommel.models.Host;

public class MultisourceDownload extends Transfer {

	IncomingDownload[] subTransfers;
	final static int MAX_DOWNLOADS = 5;
	IHostPortResolver hostResolver;
	java.io.File targetFile;
	HashMap<InetAddress, Host> hosts;
	HashMap<InetAddress, Integer> host_rank;
	boolean checkIntegrity;
	Timer watchDog;

	public MultisourceDownload(ArrayList<InetAddress> partners,
			GetFileMessage file, java.io.File targetFile,
			IHostPortResolver hostResolver) {
		super(null);
		assert partners.size() > 0;
		this.hash = file.getHash();
		this.hostResolver = hostResolver;
		this.targetFile = targetFile;
		hosts = new HashMap<>();
		watchDog = new Timer();
		checkIntegrity = true;
		logger = java.util.logging.Logger.getLogger("incoming multi " + hash);

		// check if ports for all hosts are known
		for (InetAddress i : partners) {
			Host tmp = hostResolver.getOrCreateHost(i);
			if (tmp.getPort() != Host.UNKNOWN_PORT) {
				hosts.put(i, tmp);
				host_rank.put(i, 0);
			}
		}


		// spawn subThreads
		subTransfers = spawnDownloads(hosts.size(), hash, file.getOffset(),
				file.getLength(), new ArrayList<Host>(hosts.values()),
				hostResolver);
		
		transferState = TransferStatus.Initialized;
	}

	private static IncomingDownload[] spawnDownloads(int num, String hash,
			long start_offset, long total_length, ArrayList<Host> hosts,
			IHostPortResolver hostResolver) {
		IncomingDownload[] subTransfers = new IncomingDownload[Math.min(
				MAX_DOWNLOADS, num)];
		long segmentSize = total_length / num;
		long length = segmentSize;
		long offset = start_offset;
		java.io.File tmpFile;
		int currentHost = 0;
		for (int i = 0; i < num; i++) {
			if (i == num - 1) // last segment may be bigger than others
				length = total_length - ((num - 1) * segmentSize);

			if (currentHost >= hosts.size())
				currentHost = 0;

			// allocate file and host
			tmpFile = new File(hash + "-" + num + ".tmp");
			Host host = hosts.get(currentHost++);

			// creat sub-Transfer
			subTransfers[i] = new IncomingDownload(new GetFileMessage(hash,
					offset, length), host, tmpFile, hostResolver);
			subTransfers[i].DisableIntegrityCheck();
		}
		return subTransfers;
	}

	@Override
	public void cancel() {
		watchDog.cancel();
		for (ITransferProgress t : subTransfers)
			t.cancel();
		transferState = TransferStatus.Canceled;
	}

	@Override
	public void reset() {
		watchDog.cancel();
		for (ITransferProgress t : subTransfers)
			t.reset();
		transferState = TransferStatus.Initialized;
	}

	@Override
	public void resumeTransfer() {
		for (ITransferProgress t : subTransfers)
			if (t.getStatus() == TransferStatus.TemporaryNotAvailable
					|| t.getStatus() == TransferStatus.ConnectionFailed
					|| t.getStatus() == TransferStatus.LostConnection)
				t.resumeTransfer();
		scheduleWatchDog();
	}

	@Override
	public void start() {
		for (ITransferProgress t : subTransfers) {
			if (t.getStatus() != TransferStatus.Initialized)
				throw new IllegalStateException(
						"can't start because a sub-transfer is not initialized");
			
			//update rank
			int new_rank = host_rank.get(t.getTransferPartner().getAddress()) - 1;
			host_rank.put(t.getTransferPartner().getAddress(), new_rank);
			t.start();
		}
		scheduleWatchDog();
	}

	private void scheduleWatchDog() {
		watchDog.scheduleAtFixedRate(new TimerTask() {
			@Override
			public void run() {
				boolean allTransferFinished = true;
				for (IncomingDownload t : subTransfers) {
					if (t.getStatus() != TransferStatus.Finished)
						allTransferFinished = false;
					if(t.getStatus() == TransferStatus.PermanentlyNotAvailable) {
						logger.info("One host said 'perm. not ava.' -> remove " + t.getTransferPartner());
						hosts.remove(t.getTransferPartner());
						host_rank.remove(t.getTransferPartner());
					}
					if(t.getStatus() == TransferStatus.ConnectionFailed || 
							t.getStatus() == TransferStatus.InvalidContent || 
							t.getStatus() == TransferStatus.LocalIOError || 
							t.getStatus() == TransferStatus.LostConnection || 
							t.getStatus() == TransferStatus.TemporaryNotAvailable || 
							t.getStatus() == TransferStatus.PermanentlyNotAvailable) {
						restartSegment(t);
					}
				}

				if (allTransferFinished)
					assembleParts();
			}
		}, 5000, 1000);
	}
	
	private ITransferProgress restartSegment(ITransferProgress t) {
		logger.info("restart setgment " + t);
		t.cancel();
		t.cleanup();
		Host host = null;
		int rank = Integer.MIN_VALUE;
		
		for(InetAddress a : hosts.keySet()) {
			if(host_rank.get(a) < rank) {
				rank = host_rank.get(a);
				host = hosts.get(a);
			}
		}
		
		if(host == null) {
			logger.info("No host available any more!");
			return null;
		}
		
		return new IncomingDownload(new GetFileMessage(t.getExpectedHash(), t.getOffset(), t.getLength()), host, new java.io.File(t.getTargetFile()), hostResolver);
	}

	private void assembleParts() {
		transferState = TransferStatus.AssembleParts;
		
		try {
			FileWriter writer = new FileWriter(targetFile);
			char[] buffer = new char[1024 * 1024];
			for (IncomingDownload t : subTransfers) {
				FileReader reader = new FileReader(t.getTargetFile());
				int bytesRead = -1;
				while (-1 != (bytesRead = reader.read(buffer, 0, buffer.length))) {
					writer.write(buffer, 0, bytesRead); //TODO build hash while copy
				}
				reader.close();
			}
			writer.close();
			checkIntegrity();
		} catch (IOException e) {
			logger.warning("could not assemble file: " + e.getMessage());
			transferState = TransferStatus.LocalIOError;
		}
	}
	
	private void checkIntegrity() {
		transferState = TransferStatus.CheckingHash;
		if(checkIntegrity) {
			try {
				if(HashFuncWrapper.hash(targetFile.getAbsolutePath()) != hash) {
					transferState = TransferStatus.InvalidContent;
					return;
				}
			} catch (IOException e) {
				logger.warning("Could not compute hash: " + e.getMessage());
				transferState = TransferStatus.InvalidContent;
				return;
			}
		}
		transferState = TransferStatus.Finished;
	}
	
	public void DisableIntegrityCheck() {
		checkIntegrity = false;
	}

	@Override
	public String getTargetFile() {
		return targetFile.getAbsolutePath();
	}

}
