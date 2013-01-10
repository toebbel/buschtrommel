package de.tr0llhoehle.buschtrommel.test;

import static org.junit.Assert.*;

import java.io.IOException;

import org.junit.Test;

import de.tr0llhoehle.buschtrommel.ShareCache;
import de.tr0llhoehle.buschtrommel.models.LocalShare;
import de.tr0llhoehle.buschtrommel.models.Share;

public class TestShareCache {
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
	public void testSaveAndLoad() throws IOException {
		(new java.io.File("test.file")).createNewFile();
		ShareCache shares = new ShareCache();
		LocalShare s1 = new LocalShare("ABC", 1044, Share.TTL_INFINITY, "file 1", "metablub", "test.file");
		LocalShare s2 = new LocalShare("asdasdasdasdsa", 10, 100, "file 2", "", "test2.file");
		shares.newShare(s1);
		shares.newShare(s2);
		shares.saveToFile("test.ht");
		
		ShareCache loaded = new ShareCache();
		loaded.restoreFromFile("test.ht");
		assertTrue(loaded.has("ABC"));
		assertEquals(loaded.get("ABC").getDisplayName(), s1.getDisplayName());
		assertEquals(loaded.get("ABC").getHash(), s1.getHash());
		assertEquals(loaded.get("ABC").getLength(), s1.getLength());
		assertEquals(loaded.get("ABC").getMeta(), s1.getMeta());
		assertEquals(loaded.get("ABC").getTTL(), s1.getTTL());
		assertEquals(loaded.get("ABC").getPath(), (new java.io.File(s1.getPath()).getAbsolutePath()));
		assertFalse(loaded.has("asdasdasdasdsa"));
		(new java.io.File("test.file")).delete();
		(new java.io.File("test.ht")).delete();

	}

}
