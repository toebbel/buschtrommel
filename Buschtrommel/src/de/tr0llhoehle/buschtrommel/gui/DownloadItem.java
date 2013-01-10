/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.tr0llhoehle.buschtrommel.gui;

import de.tr0llhoehle.buschtrommel.network.ITransferProgress;
import java.awt.Color;
import java.awt.Component;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.ListCellRenderer;

/**
 *
 * @author benjamin
 */
public class DownloadItem extends JPanel implements ListCellRenderer<ITransferProgress> {
     public DownloadItem() {
         setOpaque(true);
     }

     @Override
     public Component getListCellRendererComponent(JList<? extends ITransferProgress> list,
                                                   ITransferProgress value,
                                                   int index,
                                                   boolean isSelected,
                                                   boolean cellHasFocus) {

        // setText(value.toString());

         Color background;
         Color foreground;

         // check if this cell represents the current DnD drop location
         JList.DropLocation dropLocation = list.getDropLocation();
         if (dropLocation != null
                 && !dropLocation.isInsert()
                 && dropLocation.getIndex() == index) {

             background = Color.BLUE;
             foreground = Color.WHITE;

         // check if this cell is selected
         } else if (isSelected) {
             background = Color.RED;
             foreground = Color.WHITE;

         // unselected, and not the DnD drop location
         } else {
             background = Color.WHITE;
             foreground = Color.BLACK;
         };

         setBackground(background);
         setForeground(foreground);

         return this;
     }

    



 }