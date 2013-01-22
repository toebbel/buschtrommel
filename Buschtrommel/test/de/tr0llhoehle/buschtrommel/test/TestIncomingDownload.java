package de.tr0llhoehle.buschtrommel.test;

import static org.junit.Assert.*;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.Arrays;
import java.util.logging.ConsoleHandler;

import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

import de.tr0llhoehle.buschtrommel.LoggerWrapper;
import de.tr0llhoehle.buschtrommel.models.FileRequestResponseMessage;
import de.tr0llhoehle.buschtrommel.models.FileRequestResponseMessage.ResponseCode;
import de.tr0llhoehle.buschtrommel.models.GetFileMessage;
import de.tr0llhoehle.buschtrommel.models.Host;
import de.tr0llhoehle.buschtrommel.models.Message;
import de.tr0llhoehle.buschtrommel.network.IncomingDownload;
import de.tr0llhoehle.buschtrommel.network.MessageDeserializer;
import de.tr0llhoehle.buschtrommel.network.ITransferProgress.TransferStatus;
import de.tr0llhoehle.buschtrommel.test.mockups.FileContentMock;
import de.tr0llhoehle.buschtrommel.test.mockups.MessageObserverMock;
import de.tr0llhoehle.buschtrommel.test.mockups.NetworkMock;

public class TestIncomingDownload {

	NetworkMock mock;
	
	@BeforeClass
	public static void beforeClass() {
		//LoggerWrapper.LOGGER.addHandler(new ConsoleHandler());
	}
	
	@Before
	public void setUp() throws Exception {
		
	}
	
	@After
	public void tearDown() throws Exception {
		if(mock != null)
			mock.close();
	}

	@Ignore
	public void testHandleResponse() throws UnsupportedEncodingException {
		//create messages that shall be handled and convert them to InputStreams
		FileRequestResponseMessage ok = new FileRequestResponseMessage(ResponseCode.OK, 1012);
		FileRequestResponseMessage tryAgain = new FileRequestResponseMessage(ResponseCode.TRY_AGAIN_LATER, 0);
		FileRequestResponseMessage neverAgain = new FileRequestResponseMessage(ResponseCode.NEVER_TRY_AGAIN, 0);
		InputStream ok_fs = new ByteArrayInputStream(ok.Serialize().getBytes(Message.ENCODING));
		InputStream tryAgain_fs = new ByteArrayInputStream(tryAgain.Serialize().getBytes(Message.ENCODING));
		InputStream neverAgain_fs = new ByteArrayInputStream(neverAgain.Serialize().getBytes(Message.ENCODING));

		//test
		assertEquals(ok, (new TestWrapper()).handleResponse(ok_fs));
		assertEquals(tryAgain, (new TestWrapper()).handleResponse(tryAgain_fs));
		assertEquals(neverAgain, (new TestWrapper()).handleResponse(neverAgain_fs));
	}

	/**
	 * Request a file with length of 10, offset 0.
	 * Host announces, that the file will have only 9 bytes.
	 * partial transfer, checks, transfer of the rest of the file, no integrity check.
	 * 
	 * @throws InterruptedException
	 * @throws IOException
	 */
	@Test(timeout=5000)
	public void testStartDownload() throws InterruptedException, IOException {
		mock = new NetworkMock(8080);
		IncomingDownload in = new IncomingDownload(new GetFileMessage("ABC", 0, 10), new Host(mock.getAddr(), "mock", mock.getPort()),  new File("tmp"), null);
		in.DisableIntegrityCheck();
		
		//check if download request is correct
		in.start();
		
		Thread.currentThread();
		Thread.sleep(100);
		
		byte[] buffer = new byte[18];
		mock.receive(buffer);
		String strRequest = new String(buffer, Message.ENCODING);
		assertEquals((new GetFileMessage("ABC", 0, 10)), MessageDeserializer.Deserialize(strRequest));
		assertEquals(10, in.getLength());
		
		//send response
		mock.send((new FileRequestResponseMessage(ResponseCode.OK, 9)).Serialize().getBytes(Message.ENCODING));
		Thread.sleep(100);
		assertEquals(9, in.getLength());
		assertEquals(TransferStatus.Transfering, in.getStatus());
		
		//partial file transfer
		mock.send("1ABCDEF".getBytes(Message.ENCODING));
		Thread.sleep(100);
		assertEquals(7, in.getTransferedAmount());
		
		//transparent to seperators
		mock.send((String.valueOf(Message.FIELD_SEPERATOR) + Message.MESSAGE_SPERATOR).getBytes(Message.ENCODING));
		Thread.sleep(250);
		assertEquals(9, in.getTransferedAmount());
		assertEquals(TransferStatus.Finished, in.getStatus());
		mock.close();
		
		assertEquals("1ABCDEF" + Message.FIELD_SEPERATOR + Message.MESSAGE_SPERATOR, FileContentMock.getFileContent("tmp"));
		(new java.io.File("tmp")).delete();
		
		Thread.sleep(500);
	}

	/**
	 * Request a file with length of 1024, offset 0, bufferSize 31.
	 * Host announces, that the file will have only 1020 bytes, but will send 1024 bytes.
	 * partial transfer, checks, transfer of the rest of the file, no integrity check.
	 * 
	 * @throws InterruptedException
	 * @throws IOException
	 */
	@Test(timeout=15000)
	public void testAbortWhenStreamTooLong() throws InterruptedException, IOException {
		mock = new NetworkMock(8080);
		IncomingDownload in = new IncomingDownload(new GetFileMessage("ABC", 0, 1024), new Host(mock.getAddr(), "mock", mock.getPort()),  new File("tmp.out"), null, 31);
		in.DisableIntegrityCheck();
		
		//check if download request is correct
		in.start();
		
		Thread.currentThread();
		Thread.sleep(100);
		
		byte[] buffer = new byte[20];
		mock.receive(buffer);
		String strRequest = new String(buffer, Message.ENCODING);
		assertEquals((new GetFileMessage("ABC", 0, 1024)), MessageDeserializer.Deserialize(strRequest));
		assertEquals(1024, in.getLength());
		
		//send response
		mock.send((new FileRequestResponseMessage(ResponseCode.OK, 1020)).Serialize().getBytes(Message.ENCODING));
		Thread.sleep(100);
		assertEquals(1020, in.getLength());
		assertEquals(TransferStatus.Transfering, in.getStatus());
		
		//send the entire file
		byte[] sendBuffer = FileContentMock.contentA.getBytes(Message.ENCODING);
		mock.send(sendBuffer);
		Thread.sleep(250);
		assertEquals(1020, in.getTransferedAmount());
		assertEquals(TransferStatus.Finished, in.getStatus());
		mock.close();
		
		byte[] bufferSubset = Arrays.copyOfRange(sendBuffer, 0, 1020);
		assertArrayEquals(bufferSubset, FileContentMock.getFileContent("tmp.out").getBytes(Message.ENCODING));
		(new java.io.File("tmp.out")).delete();
		
		Thread.sleep(500);
	}
	
	@Test(timeout=5000)
	public void testStartDownload_NeverTryAgain() throws InterruptedException, IOException {
		mock = new NetworkMock(8080);
		MessageObserverMock obsMock = new MessageObserverMock();
		IncomingDownload in = new IncomingDownload(new GetFileMessage("ABCDEFABCDEF", 9, 1023), new Host(mock.getAddr(), "mock", mock.getPort()), new File("tmp"), null);
		in.DisableIntegrityCheck();
		in.registerObserver(obsMock);
		
		//check if download request is correct
		in.start();
		
		Thread.currentThread();
		Thread.sleep(100);
		
		byte[] buffer = new byte[29];
		mock.receive(buffer);
		String strRequest = new String(buffer, Message.ENCODING);
		assertEquals((new GetFileMessage("ABCDEFABCDEF", 9, 1023)), MessageDeserializer.Deserialize(strRequest));
		mock.send((new FileRequestResponseMessage(ResponseCode.NEVER_TRY_AGAIN, 0)).Serialize().getBytes(Message.ENCODING));
		Thread.sleep(100);
		assertEquals(TransferStatus.PermanentlyNotAvailable, in.getStatus());
		assertEquals(1, obsMock .getMessages().size());
		assertEquals(new FileRequestResponseMessage(ResponseCode.NEVER_TRY_AGAIN, 0), obsMock.getMessages().get(0));
	}
	
	@Test(timeout=30000)
	public void testStartDownload_ConnectionLoss() throws InterruptedException, IOException {
		mock = new NetworkMock(8080);
		MessageObserverMock obsMock = new MessageObserverMock();
		IncomingDownload in = new IncomingDownload(new GetFileMessage("ABCDEFABCDEF", 9, 14), new Host(mock.getAddr(), "mock", mock.getPort()), new File("tmp"), null);
		in.DisableIntegrityCheck();
		in.registerObserver(obsMock);
		
		//check if download request is correct
		in.start();
		
		Thread.currentThread();
		Thread.sleep(100);
		
		//check if getFile #1 is correct
		byte[] buffer = new byte[27];
		mock.receive(buffer);
		String strRequest = new String(buffer, Message.ENCODING);
		assertEquals((new GetFileMessage("ABCDEFABCDEF", 9, 14)), MessageDeserializer.Deserialize(strRequest));
		
		//send response and check
		mock.send((new FileRequestResponseMessage(ResponseCode.OK, 14)).Serialize().getBytes(Message.ENCODING));
		Thread.sleep(1000);
		assertEquals(TransferStatus.Transfering, in.getStatus());
		assertEquals(1, obsMock .getMessages().size());
		assertEquals(new FileRequestResponseMessage(ResponseCode.OK, 14), obsMock.getMessages().get(0));
		
		//send part of file
		mock.send("DROP THIS".getBytes(Message.ENCODING));
		Thread.sleep(1000);
		assertEquals(9, in.getTransferedAmount());
		
		//abort connection
		mock.close();
		Thread.sleep(3500);
		assertEquals(TransferStatus.LostConnection, in.getStatus());
		mock = new NetworkMock(8080);
		Thread.sleep(100);
		in.resumeTransfer();
		Thread.sleep(5000);
		
		//check if getFile #2 is correct
		buffer = new byte[27];
		mock.receive(buffer);
		strRequest = new String(buffer, Message.ENCODING);
		assertEquals((new GetFileMessage("ABCDEFABCDEF", 18, 5)), MessageDeserializer.Deserialize(strRequest));
		
		//send response #2
		mock.send((new FileRequestResponseMessage(ResponseCode.OK, 5)).Serialize().getBytes(Message.ENCODING));
		Thread.sleep(100);
		assertEquals(TransferStatus.Transfering, in.getStatus());
		assertEquals(9, in.getTransferedAmount());
		mock.send(" SH".getBytes(Message.ENCODING));
		Thread.sleep(100);
		assertEquals(12, in.getTransferedAmount());
		mock.send("IT".getBytes(Message.ENCODING));
		Thread.sleep(100);
		assertEquals(14, in.getTransferedAmount());
		assertEquals(TransferStatus.Finished, in.getStatus());
		
		assertEquals("DROP THIS SHIT", FileContentMock.getFileContent("tmp"));
		
	}
	
	@Test(timeout=5000)
	public void testStartDownload_receiveGarbage() throws InterruptedException, IOException {
		mock = new NetworkMock(8080);
		IncomingDownload in = new IncomingDownload(new GetFileMessage("ABCDEFABCDEF", 9, 1023), new Host(mock.getAddr(), "mock", mock.getPort()), new File("tmp"), null);
		in.DisableIntegrityCheck();
		
		in.start();
		
		Thread.currentThread();
		Thread.sleep(100);
		
		byte[] buffer = new byte[29];
		mock.receive(buffer);
		String strRequest = new String(buffer, Message.ENCODING);
		assertEquals((new GetFileMessage("ABCDEFABCDEF", 9, 1023)), MessageDeserializer.Deserialize(strRequest));
		mock.send((FileRequestResponseMessage.TYPE_FIELD + " OK 1022" + Message.MESSAGE_SPERATOR).getBytes(Message.ENCODING));
		Thread.sleep(100);
		assertEquals(TransferStatus.TemporaryNotAvailable, in.getStatus());
	}
	
	
	
	/**
	 * Exposes some protected methods of the IncomingDownload-Class
	 * 
	 * @author tobi
	 * 
	 */
	class TestWrapper extends IncomingDownload {

		public TestWrapper() {
			super(new GetFileMessage("", 0, 0), null, new File("tmp"), null);
		}

		@Override
		protected FileRequestResponseMessage handleResponse(InputStream in) throws UnsupportedEncodingException {
			return super.handleResponse(in);
		}

	}

}
