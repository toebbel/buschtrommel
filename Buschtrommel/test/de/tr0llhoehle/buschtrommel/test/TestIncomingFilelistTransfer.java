package de.tr0llhoehle.buschtrommel.test;

import static org.junit.Assert.*;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.InetAddress;
import java.net.InetSocketAddress;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import de.tr0llhoehle.buschtrommel.models.FileAnnouncementMessage;
import de.tr0llhoehle.buschtrommel.models.GetFilelistMessage;
import de.tr0llhoehle.buschtrommel.models.Host;
import de.tr0llhoehle.buschtrommel.models.LocalShare;
import de.tr0llhoehle.buschtrommel.models.Message;
import de.tr0llhoehle.buschtrommel.network.IncomingFilelistTransfer;
import de.tr0llhoehle.buschtrommel.test.mockups.MessageObserverMock;
import de.tr0llhoehle.buschtrommel.test.mockups.NetworkMock;

public class TestIncomingFilelistTransfer {

	NetworkMock mock;
	MessageObserverMock msgMock;
	

	@Test
	public void testFilelistTransfer() throws InterruptedException, IOException {
		mock = new NetworkMock(8080);
		Thread.sleep(1000);
		msgMock = new MessageObserverMock();
		IncomingFilelistTransfer in = new IncomingFilelistTransfer(new Host(mock.getAddr(), "mock", mock.getPort()));
		in.registerObserver(msgMock);
		in.RegisterLogHander(new java.util.logging.ConsoleHandler());
		in.start();
		Thread.sleep(1000);
		byte[] getMsg = new GetFilelistMessage().Serialize().getBytes(Message.ENCODING);
		byte[] buffer = new byte[getMsg.length];
		mock.receive(buffer);
		assertArrayEquals(getMsg, buffer);
		LocalShare s1 = new LocalShare("abc", 10221, 15, "display", "metadeta", "");
		LocalShare s2 = new LocalShare("abc", 10221, 15, "display", "metadeta", "");
		
		mock.send(((new FileAnnouncementMessage(s1).Serialize()) + (new FileAnnouncementMessage(s1).Serialize())).getBytes(Message.ENCODING));
		Thread.sleep(1000);
		
		assertEquals(2, msgMock.getMessages().size());
	}

}
