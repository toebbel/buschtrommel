package de.tr0llhoehle.buschtrommel.network;

import java.net.InetSocketAddress;
import java.util.List;

/**
 * This interface gives acces to the progress of a transfer and some controll methods like cancel and resume.
 * 
 * All implementations MUST make these controll methods async.
 * 
 *
 */
public interface ITransferProgress {
	/**
	 * Type of the download. 
	 * Incoming are transfers from other hosts to this host.
	 * Outgoing are transfers from this host to other hosts
	 * Multisource are incoming transfers from multiple hosts, containing sub-transfers
	 * @return the type of this transfer
	 */
	public TransferType getType();
	
	/**
	 * Singlesource: The length of this (sub) transfer
	 * Multisource: The total length of all transfers.
	 * @return number of bytes of this transfer
	 */
	public long getLength();
	
	/**
	 * The initial offset of the transfer.
	 * 
	 * If the transfer is resumed, this value won't change.
	 * @return offset (in number of bytes)
	 */
	public long getOffset();
	
	/**
	 * Number of bytes transfered so far.
	 * 
	 * MultisourcE: sum of all sub-transfers
	 * Won't reset after resume()
	 * @return number of bytes already transfered
	 */
	public long getTransferedAmount();
	
	/**
	 * The expected hash of the file as uppercase-string without leading 0x or empty String, if not hashcheck will be done
	 * @return hash as uppercase-string or empty string
	 */
	public String getExpectedHash();
	
	/**
	 * Returns the current status of the Transfer.
	 * 
	 * Singlesource: as they are ment
	 * Multisource: Initialized = no sub-transfer has started yet
	 * 				Connecting = no sub-transfer is transfering, at least one sub-transfer is connecting
	 * 				Transfering = one or more sub-transfers are transfering
	 * 				Assemble Parts = All sub-transfers finished. Copying parts into one file
	 * 				Temporary not available = No host sent OK as response, but at least one sent TRY AGAIN LATER
	 * 				Permanent not available = One or more hosts sent Permanent not available. Connection to all other hosts failed
	 * 				Connection failed = All connections to all hosts failed
	 * 				Finished = Resulting file has correct hash
	 * 				Invalid Content = Resulting file has incorrect hash
	 * 				LocalIO-Error = Could not assemlbe parts or all sub-transfers are stuck in LocalIO-Error state
	 * @return state of transfer
	 */
	public TransferStatus getStatus();
	
	/**
	 * Cancels the download. It can't be resumed, but reset afterwards.
	 * Only active transfers can be canceled.
	 * This method call blocks until the transfer is canceled.
	 */
	public void cancel();
	
	/**
	 * Deletes the local file, terminates existing connections and reconnects to hosts to restart the transfer.
	 * 
	 * The transfer has to be inactive to be reseted
	 */
	public void reset();
	
	/**
	 * Resumes the transfer from the byte on, it stopped.
	 * 
	 * This method is async
	 * The transfer has to be inactive to be started
	 */
	public void resumeTransfer();
	
	/**
	 * Starts the transfer.
	 * 
	 * The transfer has to be inactive to be started. 
	 * This method is async.
	 */
	public void start();
	
	/**
	 * Indicates whether the transfer is active or not.
	 * 
	 * Active transfers can't be resumed, resetted or started.
	 * @return true if transfer is active
	 */
	public boolean isActive();
	
	/**
	 * Returns the other host, this transfer is associated to.
	 * @return the transfer partner or null, if this is a multisource download
	 */
	public InetSocketAddress getTransferPartner();
	
	/**
	 * incoming transfer: location of the file, which is downloading, or "filelist"
	 * outgoing transfer: filename of the file or "filelist"
	 * 
	 * @return
	 */
	public String getTargetFile();
	
	/**
	 * List of all currently active and inactive sub-transfers
	 * @return
	 */
	public List<ITransferProgress> getSubTransfers();
	
	public void RegisterLogHander(java.util.logging.Handler h);
	public void RemoveLogHander(java.util.logging.Handler h);
	
	public enum TransferType {
		Multisource,
		Singlesource,
		Outgoing
	}
	
	public enum TransferStatus {
		Initialized,
		Connecting,
		Transfering,
		AssembleParts,
		CheckingHash,
		Finished,
		TemporaryNotAvailable,
		PermanentlyNotAvailable,
		ConnectionFailed,
		LostConnection,
		LocalIOError,
		InvalidContent,
		Canceled, 
		Cleaned
	}

	/**
	 * Frees all ressources from the transfer and changes state to 'Cleaned'.
	 * Target files are removed after cleanup.
	 * Don't call this method, if the transfer could be used by others.
	 */
	public void cleanup();
}

