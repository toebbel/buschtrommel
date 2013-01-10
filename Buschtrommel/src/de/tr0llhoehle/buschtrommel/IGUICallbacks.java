package de.tr0llhoehle.buschtrommel;

import de.tr0llhoehle.buschtrommel.models.RemoteShare;
import de.tr0llhoehle.buschtrommel.models.Host;
import de.tr0llhoehle.buschtrommel.network.ITransferProgress;

public interface IGUICallbacks {
	public void NewHostDiscovered(Host host);
	public void HostWentOffline(Host host);
	public void NewFileAvailable(RemoteShare file);
	public void FileTransferComplete(ITransferProgress transferProgress);
	public void FileTransferFailed(ITransferProgress transferProgress);
}
