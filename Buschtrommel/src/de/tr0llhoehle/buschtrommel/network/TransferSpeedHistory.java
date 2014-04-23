package de.tr0llhoehle.buschtrommel.network;

import java.sql.Date;
import java.util.Calendar;
import java.util.Dictionary;

public class TransferSpeedHistory {
	private Dictionary<Long, Long> amountOfBytes; //ms -> amount of bytes transfered (total)
	private int outputTimeInterval; //time in ms
	
	public void ctor() {
		outputTimeInterval = 1000;
	}
	
	public void addMeasurePoint(long totalAmountOfBytes) {
		amountOfBytes.put(Calendar.getInstance().getTimeInMillis(), totalAmountOfBytes);
	}
	
	
}
