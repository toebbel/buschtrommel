package de.tr0llhoehle.buschtrommel.test;

import static org.junit.Assert.*;

import java.io.FileNotFoundException;
import java.io.IOException;

import org.junit.AfterClass;
import org.junit.Test;

import de.tr0llhoehle.buschtrommel.Config;

public class TestConfig {
	private static String target = "config.xml";
	private int defaultTTL = 60;
	private int maximumYoResponseTime = 3000;
	private String shareCachePath = "shares.xml";
	private int TTLRenewalTimer = 10;
	
	@AfterClass
	public static void cleanUp() {
		//target.delete();
	}
	
	@Test
	public void testSaveLoad() throws IOException {
		Config.defaultTTL = defaultTTL;
		Config.maximumYoResponseTime = maximumYoResponseTime;
		Config.shareCachePath = shareCachePath;
		Config.TTLRenewalTimer = TTLRenewalTimer;

		Config.getInstance().saveToFile(target);
		Config.getInstance().readFromFile(target);
		assertEquals(Config.defaultTTL, defaultTTL);
		assertEquals(Config.maximumYoResponseTime, maximumYoResponseTime);
		assertEquals(Config.shareCachePath, shareCachePath);
		assertEquals(Config.TTLRenewalTimer, TTLRenewalTimer);
	}

}
