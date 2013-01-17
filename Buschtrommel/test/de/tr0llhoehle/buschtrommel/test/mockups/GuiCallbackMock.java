package de.tr0llhoehle.buschtrommel.test.mockups;

import java.util.Vector;

import de.tr0llhoehle.buschtrommel.IGUICallbacks;
import de.tr0llhoehle.buschtrommel.models.Host;
import de.tr0llhoehle.buschtrommel.models.ShareAvailability;
import de.tr0llhoehle.buschtrommel.network.ITransferProgress;

public class GuiCallbackMock implements IGUICallbacks {

	public Vector<Host> newHostDiscoveryMessages, hostWentOfflineMessages;
	public Vector<ShareAvailability> removeShareMessages, newShareAvailableMessages, updatedTTLMessages;
	public Vector<ITransferProgress> fileTransferCompleteMessages, fileTransferFailedMessages;
	
	public GuiCallbackMock() {
		newHostDiscoveryMessages = new Vector<>();
		hostWentOfflineMessages = new Vector<>();
		removeShareMessages = new Vector<>();
		newShareAvailableMessages = new Vector<>();
		updatedTTLMessages = new Vector<>();
		fileTransferCompleteMessages = new Vector<>();
		fileTransferFailedMessages = new Vector<>();
	}
	
	@Override
	public void newHostDiscovered(Host host) {
		newHostDiscoveryMessages.add(host);

	}

	@Override
	public void hostWentOffline(Host host) {
		hostWentOfflineMessages.add(host);
	}

	@Override
	public void removeShare(ShareAvailability file) {
		removeShareMessages.add(file);

	}

	@Override
	public void fileTransferComplete(ITransferProgress transferProgress) {
		fileTransferCompleteMessages.add(transferProgress);
	}

	@Override
	public void fileTransferFailed(ITransferProgress transferProgress) {
		fileTransferFailedMessages.add(transferProgress);

	}

	@Override
	public void newShareAvailable(ShareAvailability file) {
		newShareAvailableMessages.add(file);

	}

	@Override
	public void updatedTTL(ShareAvailability file) {
		updatedTTLMessages.add(file);
	}

}
