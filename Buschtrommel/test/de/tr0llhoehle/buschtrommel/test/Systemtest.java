package de.tr0llhoehle.buschtrommel.test;

import static org.junit.Assert.*;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.ArrayList;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import de.tr0llhoehle.buschtrommel.Buschtrommel;
import de.tr0llhoehle.buschtrommel.IGUICallbacks;
import de.tr0llhoehle.buschtrommel.models.FileAnnouncementMessage;
import de.tr0llhoehle.buschtrommel.models.GetFilelistMessage;
import de.tr0llhoehle.buschtrommel.models.Host;
import de.tr0llhoehle.buschtrommel.models.LocalShare;
import de.tr0llhoehle.buschtrommel.models.Message;
import de.tr0llhoehle.buschtrommel.models.PeerDiscoveryMessage;
import de.tr0llhoehle.buschtrommel.models.PeerDiscoveryMessage.DiscoveryMessageType;
import de.tr0llhoehle.buschtrommel.models.Share;
import de.tr0llhoehle.buschtrommel.test.mockups.GuiCallbackMock;
import de.tr0llhoehle.buschtrommel.test.mockups.MessageObserverMock;
import de.tr0llhoehle.buschtrommel.test.mockups.NetworkMock;
import de.tr0llhoehle.buschtrommel.test.mockups.NetworkUDPMock;

public class Systemtest {

	GuiCallbackMock gui;
	NetworkMock tcpNet;
	NetworkUDPMock udpNet;
	Buschtrommel busch;
	
	@Before
	public void setUp() throws Exception {
		gui = new GuiCallbackMock();
		udpNet = new NetworkUDPMock(4748, 4747, true, false);
		busch = new Buschtrommel(gui, "test candidate");
		Thread.sleep(500);
	}

	@After
	public void tearDown() throws Exception {
		busch.stop();
		if(tcpNet != null)
			tcpNet.close();
	}

	/**
	 * Checks if buschtrommel sends a HI message on startup
	 * @throws IOException
	 * @throws InterruptedException
	 */
	@Test(timeout=2000)
	public void testInitialHi() throws IOException, InterruptedException {
		assertEquals(0, udpNet.receivedMessages.size());
		busch.start(4747, 4748, true, false);
		Thread.sleep(500);
		assertEquals(1, udpNet.receivedMessages.size());
		assertArrayEquals((new PeerDiscoveryMessage(DiscoveryMessageType.HI, "test candidate", busch.getTransferPort()).Serialize().getBytes(Message.ENCODING)), NetworkUDPMock.stripDatagram(udpNet.receivedMessages.get(0)));
	}
	
	@Test(timeout=10000)
	public void testInitialYo() throws IOException, InterruptedException {
		busch.start(4747, 4748, true, false);
		Message hiMessage = new PeerDiscoveryMessage(DiscoveryMessageType.HI, "mock", 123);
		PeerDiscoveryMessage expectedYoMessage = new PeerDiscoveryMessage(DiscoveryMessageType.YO, "test candidate", busch.getTransferPort());
		udpNet.sendMulticast(hiMessage.Serialize().getBytes(Message.ENCODING));
		Thread.sleep(5000);
		
		//callback to gui?
		assertEquals(1, gui.newHostDiscoveryMessages.size());
		assertEquals(123, gui.newHostDiscoveryMessages.get(0).getPort());
		assertEquals("mock", gui.newHostDiscoveryMessages.get(0).getDisplayName());
		
		assertEquals(2, udpNet.receivedMessages.size()); //our own message and the YO
		assertArrayEquals(expectedYoMessage.Serialize().getBytes(Message.ENCODING), NetworkUDPMock.stripDatagram(udpNet.receivedMessages.get(1)));
		assertEquals(1, busch.getHosts().size());
//		Host host = busch.getHosts().get(new ArrayList<InetAddress>(busch.getHosts().keySet()).get(0));
//		assertEquals(new Host(InetAddress.getByName("localhost"), "mock", 123), host);
	}
	
	/**
	 * This test can fail due racing conditions in observer pattern
	 * @throws InterruptedException
	 * @throws IOException
	 */
	@Test(timeout=10000)
	public void testAutoFilelistDownload() throws InterruptedException, IOException {
		tcpNet = new NetworkMock(8001);
		Message hiMessage = new PeerDiscoveryMessage(DiscoveryMessageType.HI, "mock", 8001);
		byte[] expectedGetFilelistMessage = (new GetFilelistMessage()).Serialize().getBytes(Message.ENCODING);
		FileAnnouncementMessage f1 = new  FileAnnouncementMessage(new LocalShare("ABCDEF", 51302, Share.TTL_INFINITY, "file1", "meta", "~/file1"));
		FileAnnouncementMessage f2 = new  FileAnnouncementMessage(new LocalShare("DEFHG", 1, 100, "file2", "", "~/file1"));
		
		busch.start(4747, 4748, true, false);
		udpNet.sendMulticast(hiMessage.Serialize().getBytes(Message.ENCODING));
		Thread.sleep(5000);
		
		//check if getfilelist was sent
		assertTrue(tcpNet.isConnected());
		byte[] buffer = new byte[expectedGetFilelistMessage.length];
		tcpNet.receive(buffer);
		assertArrayEquals(buffer, expectedGetFilelistMessage);
		
		//send a filelist, containing f1 and f2 and check if files are encoded
		tcpNet.send((f1.Serialize() + f2.Serialize()).getBytes(Message.ENCODING));
		Thread.sleep(1000);
		assertEquals(2, gui.newShareAvailableMessages.size());
		assertEquals(51302, gui.newShareAvailableMessages.get(0).getFile().getLength());
		assertEquals("ABCDEF", gui.newShareAvailableMessages.get(0).getFile().getHash());
		assertEquals("file1", gui.newShareAvailableMessages.get(0).getDisplayName());
		assertEquals("meta", gui.newShareAvailableMessages.get(0).getMeta());
		assertEquals(Share.TTL_INFINITY, gui.newShareAvailableMessages.get(0).getTtl());
		assertEquals("mock", gui.newShareAvailableMessages.get(0).getHost().getDisplayName());
		assertEquals(1, gui.newShareAvailableMessages.get(0).getFile().getHostList().size());
		assertEquals(1, gui.newShareAvailableMessages.get(0).getFile().getSources().size());
		assertEquals(8001, gui.newShareAvailableMessages.get(0).getHost().getPort());
		
		assertEquals(1, gui.newShareAvailableMessages.get(1).getFile().getLength());
		assertEquals("DEFHG", gui.newShareAvailableMessages.get(1).getFile().getHash());
		assertEquals("file2", gui.newShareAvailableMessages.get(1).getDisplayName());
		assertEquals("", gui.newShareAvailableMessages.get(1).getMeta());
		assertEquals("mock", gui.newShareAvailableMessages.get(1).getHost().getDisplayName());
		assertEquals(8001, gui.newShareAvailableMessages.get(1).getHost().getPort());
		assertEquals(1, gui.newShareAvailableMessages.get(1).getFile().getHostList().size());
		assertEquals(1, gui.newShareAvailableMessages.get(1).getFile().getSources().size());
		assertTrue(busch.getRemoteShares().keySet().contains("ABCDEF"));
		//getRemoteShare and notification point to very same object
		assertSame(busch.getRemoteShares().get("ABCDEF").getSources(), gui.newShareAvailableMessages.get(0).getFile().getSources());
		
		assertTrue(busch.getRemoteShares().keySet().contains("DEFHG"));
		assertSame(busch.getRemoteShares().get("DEFHG").getSources(), gui.newShareAvailableMessages.get(1).getFile().getSources());
	}
	
	
	@Test(timeout=10000)
	public void testInnactivePeerDiscovery_YOBroadcast() throws InterruptedException, IOException {
		busch.start(4747, 4748, true, false);
		Thread.sleep(100);
		udpNet.sendUnicast(new PeerDiscoveryMessage(DiscoveryMessageType.YO, "mock", 321).Serialize().getBytes(Message.ENCODING), InetAddress.getLocalHost());
		Thread.sleep(1000);
		assertEquals(1, gui.newHostDiscoveryMessages.size());
		assertEquals(InetAddress.getByName("localhost"), gui.newHostDiscoveryMessages.get(0).getAddress());
		assertEquals("mock", gui.newHostDiscoveryMessages.get(0).getDisplayName());
		assertEquals(321, gui.newHostDiscoveryMessages.get(0).getPort());
		assertSame(gui.newHostDiscoveryMessages.get(0), busch.getHosts().get(InetAddress.getByName("localhost")));
	}
	
	@Test(timeout=10000)
	public void testActivePeerDiscovery_FileAnnouncement() throws InterruptedException, IOException {
		busch.start(4747, 4748, true, false);
		Thread.sleep(100);
		udpNet.sendUnicast((new FileAnnouncementMessage(new LocalShare("HASH", 321, 100, "dsp", "meta", ""))).Serialize().getBytes(Message.ENCODING), InetAddress.getLocalHost());
		Thread.sleep(500);
		
		//test candidate should send a HI message to mock
		assertEquals(2, udpNet.receivedMessages.size()); //File announcement and a HI message
		byte[] expectedHIUnicastMessage = (new PeerDiscoveryMessage(DiscoveryMessageType.HI, "test candidate", busch.getTransferPort())).Serialize().getBytes(Message.ENCODING);
		assertArrayEquals(expectedHIUnicastMessage, NetworkUDPMock.stripDatagram(udpNet.receivedMessages.get(1)));
		
		//we answer with YO
		udpNet.sendUnicast((new PeerDiscoveryMessage(DiscoveryMessageType.YO, "mock", 222)).Serialize().getBytes(Message.ENCODING), InetAddress.getByName("localhost"));
		Thread.sleep(250);
		
		assertEquals(1, gui.newHostDiscoveryMessages.size());
		assertEquals(InetAddress.getByName("localhost"), gui.newHostDiscoveryMessages.get(0).getAddress());
		assertEquals("mock", gui.newHostDiscoveryMessages.get(0).getDisplayName());
		assertEquals(222, gui.newHostDiscoveryMessages.get(0).getPort());
		assertSame(gui.newHostDiscoveryMessages.get(0), busch.getHosts().get(InetAddress.getByName("localhost")));
		
		//check if delayed FileAnnouncementMessage arrived
		Thread.sleep(6000);
		assertEquals(1, gui.newShareAvailableMessages.size());
		assertEquals("dsp", gui.newShareAvailableMessages.get(0).getDisplayName());
		//assertEquals("meta", gui.newShareAvailableMessages.get(0).getMeta());
		assertEquals("HASH", gui.newShareAvailableMessages.get(0).getFile().getHash());
		assertEquals(1, busch.getRemoteShares().get("HASH").getHostList().size());
		assertEquals("mock", busch.getRemoteShares().get("HASH").getHostList().get(0).getDisplayName());
		assertEquals(222, busch.getRemoteShares().get("HASH").getHostList().get(0).getPort());
		
		//check if host is known
		assertEquals(1, busch.getHosts().size());
		assertEquals("mock", busch.getHosts().get(InetAddress.getByName("localhost")).getDisplayName());
		assertEquals(222, busch.getHosts().get(InetAddress.getByName("localhost")).getPort());
		
		//check if host has the file
		Host h = busch.getHosts().get(InetAddress.getByName("localhost"));
		assertEquals(1, h.getSharedFiles().size());
		assertTrue(h.getSharedFiles().containsKey("HASH"));
	}
}
