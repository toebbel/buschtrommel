package de.tr0llhoehle.buschtrommel;

import de.tr0llhoehle.buschtrommel.models.ShareAvailability;
import de.tr0llhoehle.buschtrommel.models.RemoteShare;
import de.tr0llhoehle.buschtrommel.models.Host;
import de.tr0llhoehle.buschtrommel.network.ITransferProgress;

public interface IGUICallbacks {
	public void newHostDiscovered(Host host);
	public void hostWentOffline(Host host);
	public void newShareAvailable(ShareAvailability file);
	public void updatedTTL(ShareAvailability file);
	public void removeShare(ShareAvailability file);
	public void fileTransferComplete(ITransferProgress transferProgress);
	public void fileTransferFailed(ITransferProgress transferProgress);
}
