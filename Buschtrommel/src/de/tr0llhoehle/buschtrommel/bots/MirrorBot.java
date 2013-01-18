package de.tr0llhoehle.buschtrommel.bots;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.util.Hashtable;
import java.util.Vector;

import de.tr0llhoehle.buschtrommel.Buschtrommel;
import de.tr0llhoehle.buschtrommel.IGUICallbacks;
import de.tr0llhoehle.buschtrommel.models.Host;
import de.tr0llhoehle.buschtrommel.models.LocalShare;
import de.tr0llhoehle.buschtrommel.models.RemoteShare;
import de.tr0llhoehle.buschtrommel.models.ShareAvailability;
import de.tr0llhoehle.buschtrommel.network.ITransferProgress;
import de.tr0llhoehle.buschtrommel.network.ITransferProgress.TransferStatus;

public class MirrorBot extends Thread implements IGUICallbacks {

	Buschtrommel buschtrommel;
	Vector<String> downloaded;
	Vector<String> downloading;
	Vector<String> queue;
	private static final int DEFAULT_TTL = 60;
	private static final int MAX_NUM_DOWNLOADS = 3;
	boolean keepAlive;

	public MirrorBot() throws IOException {
		buschtrommel = new Buschtrommel(this, "mirror-bot");
		downloading = new Vector<>();
		queue = new Vector<>();
		downloaded = new Vector<>();
		buschtrommel.start();
		this.start();
	}
	
	public void printStatus() {
		System.out.println("downloaded: " + downloaded.size());
		System.out.println("downloading: " + downloading.size());
		System.out.println("queue: " + queue.size());
	}

	@Override
	public void run() {
		keepAlive = true;
		while (keepAlive) {
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
			}
			int tries = 0;
			while(downloading.size() < MAX_NUM_DOWNLOADS && queue.size() > 0 && tries++ < MAX_NUM_DOWNLOADS * 2) {
				String curr = queue.get(0);
				queue.remove(curr);
				System.out.println("download " + curr);
				if(buschtrommel.DownloadFile(curr, curr) != null)
					downloading.add(curr);
			}
		}
	}

	public void cancel() throws IOException {
		buschtrommel.stop();
		keepAlive = false;
	}

	@Override
	public void newHostDiscovered(Host host) {
	}

	@Override
	public void hostWentOffline(Host host) {
	}

	@Override
	public void removeShare(ShareAvailability file) {
	}


	@Override
	public synchronized void newShareAvailable(ShareAvailability file) {
		String hash = file.getFile().getHash();
		System.out.println("new share available: " + hash);
		if(!downloaded.contains(hash) && !downloading.contains(hash) && !queue.contains(hash))
			queue.add(hash);
	}

	@Override
	public void updatedTTL(ShareAvailability file) {
	}

	/**
	 * @param args
	 * @throws IOException
	 */
	public static void main(String[] args) throws IOException {
		MirrorBot bot = new MirrorBot();
		while(System.in.read() != 'c') {
			bot.printStatus();
		}
		bot.cancel();
	}

}
