package de.tr0llhoehle.buschtrommel;

import de.tr0llhoehle.buschtrommel.models.File;
import de.tr0llhoehle.buschtrommel.models.Host;

public interface IGUICallbacks {
	public void NewHostDiscovered(Host host);
	public void HostWentOffline(Host host);
	public void NewFileAvailable(File file);
	public void FileTransferComplete(ITransferProgress transferProgress);
	public void FileTransferFailed(ITransferProgress transferProgress);
}
