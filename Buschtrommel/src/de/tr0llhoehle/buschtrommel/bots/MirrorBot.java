package de.tr0llhoehle.buschtrommel.bots;

import java.io.IOException;
import java.util.Hashtable;
import java.util.Timer;
import java.util.TimerTask;
import java.util.Vector;

import de.tr0llhoehle.buschtrommel.Buschtrommel;
import de.tr0llhoehle.buschtrommel.IGUICallbacks;
import de.tr0llhoehle.buschtrommel.models.Host;
import de.tr0llhoehle.buschtrommel.models.ShareAvailability;
import de.tr0llhoehle.buschtrommel.network.ITransferProgress;
import de.tr0llhoehle.buschtrommel.network.ITransferProgress.TransferStatus;

public class MirrorBot implements IGUICallbacks {

	Buschtrommel buschtrommel;
	Vector<String> downloaded;
	Hashtable<String, ITransferProgress> downloading;
	Vector<String> queue;
	private static final int MAX_NUM_DOWNLOADS = 3;
	Timer downloadStarter, statusChecker;

	public MirrorBot() throws IOException {
		buschtrommel = new Buschtrommel(this, "mirror-bot");
		downloading = new Hashtable<String, ITransferProgress>();
		queue = new Vector<String>();
		downloaded = new Vector<String>();
		buschtrommel.start();
		downloadStarter = new Timer("download starter");
		downloadStarter.scheduleAtFixedRate(new DownloadStarter(), 5000, 5000);
		statusChecker = new Timer("download status checker");
		statusChecker.scheduleAtFixedRate(new DownloadStatusCheck(), 10000, 10000);
	}
	
	public void printStatus() {
		System.out.println("downloaded: " + downloaded.size());
		System.out.println("downloading: " + downloading.size());
		System.out.println("queue: " + queue.size());
	}

	
	
	class DownloadStarter extends TimerTask {
		
		@Override
		public void run() {
			int tries = 0;
			while(downloading.size() < MAX_NUM_DOWNLOADS && queue.size() > 0 && tries++ < MAX_NUM_DOWNLOADS * 2) {
				String curr = queue.get(0);
				queue.remove(curr);
				System.out.println("download " + curr);
				ITransferProgress transfer = buschtrommel.DownloadFile(curr, curr);
				if(transfer != null)
					downloading.put(curr, transfer);
			}
		}
	}
	
	class DownloadStatusCheck extends TimerTask {

		@Override
		public void run() {
			for(String hash : downloading.keySet()) {
				ITransferProgress transfer = downloading.get(hash);
				if(transfer.getStatus() == TransferStatus.Finished) {
					downloading.remove(hash);
					buschtrommel.cleanIncomingTransfer(hash);
					System.out.println("File '" + hash + "' finished");
					addFileToShare(hash);
				} else if (transfer.getStatus() != TransferStatus.PermanentlyNotAvailable && //in some error state
						transfer.getStatus() != TransferStatus.Transfering && 
						transfer.getStatus() != TransferStatus.Connecting) {
					System.out.println("Transfer '" + hash + "' is in state " + transfer.getStatus().toString() + " - restarting");
					buschtrommel.cleanIncomingTransfer(hash);
					downloading.remove(hash);
					queue.add(hash);
				}
			}
		}
		
		void addFileToShare(String hash) {
			try {
				buschtrommel.AddFileToShare(hash, "Copy of " + hash, "mirrored by a bot");
			} catch (IOException e) {
				e.printStackTrace();
				System.out.println("File '" + hash + "' does not exist. ReQueue");
				queue.add(hash);
			}
		}
	}

	public void cancel() throws IOException {
		downloadStarter.cancel();
		statusChecker.cancel();
		buschtrommel.stop();
	}

	public void newHostDiscovered(Host host) {
	}

	public void hostWentOffline(Host host) {
	}

	public void removeShare(ShareAvailability file) {
	}


	public synchronized void newShareAvailable(ShareAvailability file) {
		String hash = file.getFile().getHash();
		System.out.println("new share available: " + hash);
		if(!downloaded.contains(hash) && !downloading.contains(hash) && !queue.contains(hash))
			queue.add(hash);
	}

	public void updatedTTL(ShareAvailability file) {
	}

	/**
	 * @param args
	 * @throws IOException
	 */
	public static void main(String[] args) throws IOException {
		MirrorBot bot = new MirrorBot();
		while(System.in.read() != (int)'c') {
			bot.printStatus();
		}
		bot.cancel();
	}

}
