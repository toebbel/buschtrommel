package de.tr0llhoehle.buschtrommel.test;

import static org.junit.Assert.*;

import org.junit.Test;

import de.tr0llhoehle.buschtrommel.ShareCache;
import de.tr0llhoehle.buschtrommel.models.Share;

public class ShareCacheTest {
	@Test
	public void testAddShare() {
		ShareCache troll = new ShareCache();
		Share share = new Share("AC3", 29, 31, "test blubb", "meta", "/home/path");
		//System.out.println(share.getHash());
		
		assertEquals("", troll.getAllShares());
		
		troll.newShare(share);
		
		assertEquals("FILEAC33129test blubbmeta", troll.getAllShares());
		
		assertTrue(troll.has(share.getHash()));

	}

	@Test
	public void testStore() {
		ShareCache troll = new ShareCache();
		Share share = new Share("AC3", 29, 31, "test blubb", "meta", "/home/path");

		troll.newShare(share);

		assertTrue(troll.has(share.getHash()));

		troll.saveToFile("shares.xml");

		ShareCache troll2 = new ShareCache();

		troll2.restoreFromFile("shares.xml");

		assertTrue(troll2.has(share.getHash()));

	}

}
