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
 * 
 * @author benjamin
 */
public class LocalSharesTableModel extends AbstractTableModel {

	private Vector<String[]> shares = new Vector<String[]>();;

	private String[] names = new String[] { "Filename", "Meta-Information", "Path", "Size", "TTL" };
	private boolean[] canEdit = new boolean[] { true, true, false, false, true };

	public String getColumnName(int col) {
		return names[col].toString();
	}

	public Class getColumnClass(int columnIndex) {
		// return types [columnIndex];
		return java.lang.String.class;
	}

	public boolean isCellEditable(int rowIndex, int columnIndex) {
		return canEdit[columnIndex];

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
			// throw new UnsupportedOperationException("Not supported yet.");
		} else
			return null;
	}

	public synchronized void addShare(String name, String path, String size, String ttl) {
		if (path == null) {
			System.out.print("path is null");
			return;
		}
		String eintrag[] = new String[names.length];
		// "Filename", "Meta-Information", "Path", "Size","TTL"
		eintrag[0] = name;
		eintrag[1] = "";
		eintrag[2] = path;
		eintrag[3] = size;
		eintrag[4] = ttl;
		if (ttl == "") {
			ttl = "-1";
		}

		this.shares.add(eintrag);
		this.fireTableDataChanged();
	}

	public synchronized void addMeta(int index, String meta) {
		shares.get(index)[1] = meta;
		this.fireTableDataChanged();
	}

	synchronized void removeShare(int index) {
		if (index < shares.size()) {
			shares.remove(index);
			this.fireTableDataChanged();
		}
	}

}
