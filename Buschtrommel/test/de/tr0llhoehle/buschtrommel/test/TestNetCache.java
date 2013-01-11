package de.tr0llhoehle.buschtrommel.test;

import static org.junit.Assert.*;

import java.net.InetAddress;
import java.net.UnknownHostException;

import org.junit.Test;

import de.tr0llhoehle.buschtrommel.models.FileAnnouncementMessage;
import de.tr0llhoehle.buschtrommel.models.Host;
import de.tr0llhoehle.buschtrommel.models.LocalShare;
import de.tr0llhoehle.buschtrommel.models.PeerDiscoveryMessage;
import de.tr0llhoehle.buschtrommel.network.NetCache;

public class TestNetCache {

	@Test
	public void testHostExists() throws UnknownHostException {
		NetCache tmp = new NetCache(null, null, null);
		Host host = new Host(InetAddress.getByName("localhost"), "troll", 1234);
		PeerDiscoveryMessage message = new PeerDiscoveryMessage(PeerDiscoveryMessage.DiscoveryMessageType.HI, "troll",
				1234);

		assertFalse(tmp.hostExists(host));

		message.setSource(InetAddress.getByName("localhost"));
		tmp.receiveMessage(message);

		assertTrue(tmp.hostExists(host));

		tmp.removeHost(host);

		assertFalse(tmp.hostExists(host));
	}

	@Test
	public void testFileAnnouncment() throws UnknownHostException {
		NetCache tmp = new NetCache(null, null, null);
		Host host = new Host(InetAddress.getByName("localhost"), "troll", 1234);
		FileAnnouncementMessage message = new FileAnnouncementMessage(new LocalShare("testhash", 42, 600, "katze",
				"katzenbilder!!!", "/home/katze.jpg"));
		message.setSource(InetAddress.getByName("localhost"));
		tmp.receiveMessage(message);
		
		assertTrue(tmp.hostExists(host));
		
		//TODO: test other cases
	}
	
	@Test
	public void testTTLExpiration() throws UnknownHostException, InterruptedException {
		NetCache tmp = new NetCache(null, null, null);
		FileAnnouncementMessage message = new FileAnnouncementMessage(new LocalShare("testhash", 42, 10, "katze",
				"katzenbilder!!!", "/home/katze.jpg"));
		message.setSource(InetAddress.getByName("localhost"));
		tmp.receiveMessage(message);
		
		assertTrue(tmp.shareExists("testhash"));
		
		Thread.currentThread().sleep(11000);
		
		assertFalse(tmp.shareExists("testhash"));
	}

}
