package de.tr0llhoehle.buschtrommel.test;

import static org.junit.Assert.*;

import java.io.FileNotFoundException;

import org.junit.AfterClass;
import org.junit.Test;

import de.tr0llhoehle.buschtrommel.Config;

public class TestConfig {
	private static java.io.File target = new java.io.File("config.xml");
	
	@AfterClass
	public static void cleanUp() {
		target.delete();
	}
	
	@Test
	public void testSaveLoad() throws FileNotFoundException {
		Config c = new Config();
		c.defaultTTL = 60;
		c.minimumYoResponseTime = 3;
		c.shareCachePath = "shares.xml";
		c.TTLRenewalTimer = 10;

		Config.saveToFile(target, c);
		Config loaded = Config.readFromFile(target);
		assertEquals(loaded.defaultTTL, c.defaultTTL);
		assertEquals(loaded.minimumYoResponseTime, c.minimumYoResponseTime);
		assertEquals(loaded.shareCachePath, c.shareCachePath);
		assertEquals(loaded.TTLRenewalTimer, c.TTLRenewalTimer);
	}

}
