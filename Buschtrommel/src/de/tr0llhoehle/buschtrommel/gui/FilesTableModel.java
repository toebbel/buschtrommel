/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.tr0llhoehle.buschtrommel.gui;

import de.tr0llhoehle.buschtrommel.models.Host;
import de.tr0llhoehle.buschtrommel.models.RemoteShare;
import de.tr0llhoehle.buschtrommel.models.ShareAvailability;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Vector;
import javax.swing.table.AbstractTableModel;

/**
 * the tableModel to store the entries of the files table
 * 
 * @author benjamin
 */
public class FilesTableModel extends AbstractTableModel {

	private Vector<String[]> shares = new Vector<String[]>();;

	private String[] names = new String[] { "Filename", "Meta-Information", "Size", "Host-Name", "IP", "Hash", "TTL" };
	private Class[] types = new Class[] { java.lang.String.class, java.lang.String.class, java.lang.String.class,
			java.lang.String.class, java.lang.String.class };
	private boolean[] canEdit = new boolean[] { false, false, false, false, false };

	public String getColumnName(int col) {
		return names[col].toString();
	}

	public Class getColumnClass(int columnIndex) {
		// return types [columnIndex];
		return java.lang.String.class;
	}

	public boolean isCellEditable(int rowIndex, int columnIndex) {
		// return canEdit [columnIndex];
		return false;
	}

	@Override
	public int getRowCount() {
		return shares.size();
		// throw new UnsupportedOperationException("Not supported yet.");
	}

	@Override
	public int getColumnCount() {
		return names.length;
		// throw new UnsupportedOperationException("Not supported yet.");
	}

	@Override
	public String getValueAt(int rowIndex, int columnIndex) {
		if (rowIndex < shares.size()) {
			return shares.get(rowIndex)[columnIndex];
		} else {
			return null;
		}
		// throw new UnsupportedOperationException("Not supported yet.");

	}

	public synchronized void addShare(ShareAvailability avail) {
		if (avail == null) {
			return;
		}
		String eintragVector[] = new String[names.length];

		// "Filename", "Meta-Information", "Size", "Host-Name","IP", "Hash",
		// "TTL"

		eintragVector[0] = avail.getDisplayName();
		eintragVector[1] = avail.getMeta();
		eintragVector[3] = avail.getHost().getDisplayName();
		eintragVector[4] = avail.getHost().getAddress().getHostAddress();

		eintragVector[6] = String.valueOf(avail.getTtl());
		// get a share

		// avail.getFile().getHash()
		RemoteShare tempfile = avail.getFile();
		if (tempfile != null) {

			eintragVector[2] = MainFrame.humanReadableByteCount(tempfile.getLength(), true);
			// eintragVector[2] = String.valueOf(tempfile.getLength());

			eintragVector[5] = tempfile.getHash();
		}

		this.shares.add(eintragVector);
		this.fireTableDataChanged();
		
	}

	public void addMock(String filename, String meta, String size, String hostName, String ip, String Hash, String ttl) {
		String eintragVector[] = { filename, meta, size, hostName, ip, Hash, ttl };
		shares.add(eintragVector);
		this.fireTableDataChanged();

		// "Filename", "Meta-Information", "Size", "Host-Name","IP", "Hash",
		// "TTL"
	}

	// public FilesTableModell(Hashtable<String, RemoteShare> remoteShares) {
	// // this.shares = new
	// Vector<String[]>();//[remoteShares.keySet().size()][names.length];
	//
	//
	//
	// for (String key : remoteShares.keySet()) {
	// RemoteShare temp = remoteShares.get(key);
	//
	// for (ShareAvailability avail : temp.getSources()) {
	// //"Filename", "Meta-Information", "Size", "Host", "Hash", "TTL"
	//
	// String eintragVector[] = new String[names.length];
	// if (avail != null) {
	// eintragVector[0] = avail.getDisplayName();
	// eintragVector[1] = avail.getMeta();
	// eintragVector[3] = avail.getHost().getDisplayName();
	// eintragVector[5] = String.valueOf(avail.getTtl());
	// } else {
	// eintragVector[0] = "No name";
	// eintragVector[1] = "No meta";
	// eintragVector[3] = "no display name";
	// eintragVector[5] = "no meta";
	// }
	//
	// eintragVector[2] = String.valueOf(temp.getLength());
	//
	// eintragVector[4] = key;
	//
	//
	//
	// ec this.shares.add(eintragVector);
	//
	// }
	//
	//
	//
	//
	// }
	// }

	synchronized void removeShare(ShareAvailability file) {
		Vector<String[]> ids = new Vector<String[]>();
		for (String[] col : shares) {
			// "Filename", "Meta-Information", "Size", "Host-Name","IP", "Hash",
			// "TTL"
			if (col[5].equals(file.getFile().getHash()) && col[4].equals(file.getHost().getAddress().getHostAddress())) {
				// shares.remove(col);
				// this.fireTableDataChanged();
				ids.add(col);
			}
		}
		shares.removeAll(ids);
		this.fireTableStructureChanged();
		// throw new UnsupportedOperationException("Not yet implemented");
	}

	synchronized void hostWentOffline(Host host) {
		// throw new UnsupportedOperationException("Not yet implemented");
		Vector<String[]> ids = new Vector<String[]>();
		for (String[] col : shares) {
			// "Filename", "Meta-Information", "Size", "Host-Name","IP", "Hash",
			// "TTL"
			if (col[4].equals(host.getAddress().getHostAddress())) {
				ids.add(col);

			}
		}
		shares.removeAll(ids);
		// this.fireTableStructureChanged();
		this.fireTableDataChanged();
	}

	void newHostDiscovered(Host host) {
		// throw new UnsupportedOperationException("Not yet implemented");
	}

	public synchronized void updatedTTL(ShareAvailability file) {
		int row = 0;
		for (String[] col : shares) {
			// "Filename", "Meta-Information", "Size", "Host-Name","IP", "Hash",
			// "TTL"
			if (col[5].equals(file.getFile().getHash()) && col[4].equals(file.getHost().getAddress().getHostAddress())) {

				col[6] = String.valueOf(file.getTtl());

			}
			row++;
			if (shares.size() > row) {
				this.fireTableCellUpdated(row, 6);
			} else {
				this.fireTableStructureChanged();
			}
		}

	}
}
