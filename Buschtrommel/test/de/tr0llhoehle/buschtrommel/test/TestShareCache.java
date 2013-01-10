package de.tr0llhoehle.buschtrommel.test;

import static org.junit.Assert.*;

import org.junit.Test;

import de.tr0llhoehle.buschtrommel.ShareCache;
import de.tr0llhoehle.buschtrommel.models.LocalShare;

public class ShareCacheTest {
	@Test
	public void testAddShare() {
		ShareCache troll = new ShareCache();
		LocalShare share = new LocalShare("AC3", 29, 31, "test blubb", "meta", "/home/path");
		//System.out.println(share.getHash());
		
		assertEquals("", troll.getAllShares());
		
		troll.newShare(share);
		
		assertEquals("FILEAC33129test blubbmeta", troll.getAllShares());
		
		assertTrue(troll.has(share.getHash()));

	}

	@Test
	public void testStore() {
		ShareCache troll = new ShareCache();
		LocalShare share = new LocalShare("AC3", 29, 31, "test blubb", "meta", "/home/path");

		LocalShare share2 = new LocalShare("asdasd", 29, 31, "test blubb", "meta", "/home/path");
		
		troll.newShare(share);
		troll.newShare(share2);

		assertTrue(troll.has(share.getHash()));

		troll.saveToFile("shares.ht");

		ShareCache troll2 = new ShareCache();

		troll2.restoreFromFile("shares.ht");

		assertTrue(troll2.has(share.getHash()));

	}

}
