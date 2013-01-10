package de.tr0llhoehle.buschtrommel.test;

import static org.junit.Assert.*;

import java.io.IOException;
import java.net.Socket;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import de.tr0llhoehle.buschtrommel.ShareCache;
import de.tr0llhoehle.buschtrommel.models.Message;
import de.tr0llhoehle.buschtrommel.models.Share;
import de.tr0llhoehle.buschtrommel.network.OutgoingFilelistTransfer;
import de.tr0llhoehle.buschtrommel.network.ITransferProgress.TransferStatus;
import de.tr0llhoehle.buschtrommel.test.mockups.NetworkMock;

public class TestOutgoingFileListTransfer {

	NetworkMock mock;
	ShareCache shares;
	
	@Before
	public void setUp() throws Exception {
		mock = new NetworkMock(8080);
		shares = new ShareCache();
		shares.newShare(new Share("ABC", 10, -1, "this is a file", "meta", "/fileA"));
		shares.newShare(new Share("DEFGH", 512, 20, "this is a file, too", "meta2", "/fileB"));
	}

	@After
	public void tearDown() throws Exception {
		mock.close();
	}

	@Test
	public void test() throws IOException, InterruptedException {
		//establish
		Socket s = new Socket("localhost", mock.getPort());
		OutgoingFilelistTransfer out = new OutgoingFilelistTransfer(s.getOutputStream(), shares);
		Thread.sleep(100);
		
		//connect
		byte[] buffer = new byte[79];
		mock.receive(buffer);
		Thread.sleep(2000);
		
		//cmp
		assertEquals(TransferStatus.Finished, out.getStatus());
		assertArrayEquals(shares.getAllShares().getBytes(Message.ENCODING), buffer);
		
	}
	

}
