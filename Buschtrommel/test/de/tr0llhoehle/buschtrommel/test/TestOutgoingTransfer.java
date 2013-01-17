package de.tr0llhoehle.buschtrommel.test;

import static org.junit.Assert.*;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Array;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Arrays;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import de.tr0llhoehle.buschtrommel.LocalShareCache;
import de.tr0llhoehle.buschtrommel.models.FileRequestResponseMessage;
import de.tr0llhoehle.buschtrommel.models.FileRequestResponseMessage.ResponseCode;
import de.tr0llhoehle.buschtrommel.models.GetFileMessage;
import de.tr0llhoehle.buschtrommel.models.GetFilelistMessage;
import de.tr0llhoehle.buschtrommel.models.LocalShare;
import de.tr0llhoehle.buschtrommel.models.Message;
import de.tr0llhoehle.buschtrommel.models.Share;
import de.tr0llhoehle.buschtrommel.network.OutgoingTransfer;
import de.tr0llhoehle.buschtrommel.network.ITransferProgress.TransferStatus;
import de.tr0llhoehle.buschtrommel.test.mockups.FileContentMock;
import de.tr0llhoehle.buschtrommel.test.mockups.NetworkMock;

public class TestOutgoingTransfer {

	NetworkMock mock;
	OutgoingTransfer out;
	Socket sendingSocket;
	private LocalShareCache shares;
	static java.io.File readFile;
	
	@BeforeClass
	public static void beforeClass() throws IOException {
		readFile = new File("tmp");
		java.io.FileWriter writer = new FileWriter(readFile);
		writer.write("Hallo, das hier ist eine tolle Datei :)");
		writer.close();
	}
	
	@AfterClass
	public static void afterClass() {
		readFile.delete();
	}
	
	@Before
	public void setUp() throws Exception {
		mock = new NetworkMock(8080);
		shares = new LocalShareCache();
		shares.newShare(new LocalShare("ABC", 1024, -1, "this is a file", "meta", "fileA"));
		shares.newShare(new LocalShare("DEFGH", 512, 20, "this is a file, too", "meta2", "fileB"));
		FileContentMock.writeFileContent("fileA", FileContentMock.contentA);
	}

	@After
	public void tearDown() throws Exception {
		mock.close();
		if(out != null)
			out.cancel(); //ensures to close the stream
		if(sendingSocket != null)
			sendingSocket.close();
		(new java.io.File("fileA")).delete();
	}

	@Test
	public void testTransferFile() throws UnknownHostException, IOException, InterruptedException {
		GetFileMessage m = new GetFileMessage("ABC", 0, 512);
		
		sendingSocket = new Socket("localhost", mock.getPort());
		Thread.sleep(1000);
		out = new OutgoingTransfer(m, sendingSocket.getOutputStream(), shares, new InetSocketAddress("host", 123), 11);
		out.start();
		byte[] expectedResponseHead = new FileRequestResponseMessage(ResponseCode.OK, 512).Serialize().getBytes(Message.ENCODING);
		byte[] expectedResponseBody = Arrays.copyOfRange(FileContentMock.contentA.getBytes(Message.ENCODING), 0, 512);
		byte[] expectedBuffer = new byte[expectedResponseHead.length + expectedResponseBody.length];
		int i;
		for(i = 0; i < expectedResponseHead.length; i++) {
			expectedBuffer[i] = expectedResponseHead[i];
		}
		for(int j = 0; j < expectedResponseBody.length; j++) {
			expectedBuffer[i++] = expectedResponseBody[j];
		}
		byte[] buffer = new byte[expectedResponseHead.length + expectedResponseBody.length];
		Thread.sleep(1000);
		mock.receive(buffer);
		Thread.sleep(5000);
		String expect = new String(expectedBuffer, Message.ENCODING);
		String actual = new String(buffer, Message.ENCODING);
		assertEquals(out.getLength(), 512);
		assertArrayEquals(expectedBuffer, buffer);
	}
	
	@Test
	public void testGetFilelist() throws IOException, InterruptedException {
		//establish
		Socket s = new Socket("localhost", mock.getPort());
		OutgoingTransfer out = new OutgoingTransfer(new GetFilelistMessage(), s.getOutputStream(), shares, new InetSocketAddress("host", 123));
		out.start();
		Thread.sleep(100);
		
		//connect
		byte[] buffer = new byte[81];
		mock.receive(buffer);
		Thread.sleep(2000);
		
		//cmp
		assertEquals(TransferStatus.Finished, out.getStatus());
		assertArrayEquals(shares.getAllShares().getBytes(Message.ENCODING), buffer);
	}


}
