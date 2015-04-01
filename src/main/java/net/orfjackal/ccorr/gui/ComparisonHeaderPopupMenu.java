// Copyright © 2003-2006, 2010, Esko Luontola <www.orfjackal.net>
// This software is released under the GNU General Public License, version 2 or later.
// The license text is at http://www.gnu.org/licenses/gpl.html

package net.orfjackal.ccorr.gui;

import net.orfjackal.ccorr.ChecksumFile;

import javax.swing.*;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

/**
 * Popupmenu that shows up when right clicking a file's header in a comparison.
 *
 * @author Esko Luontola
 */
public class ComparisonHeaderPopupMenu extends JPopupMenu implements MouseListener {

    private int selectedColumn = -1;

    private ComparisonPanel comparisonPanel;

    public ComparisonHeaderPopupMenu(ComparisonPanel cp) {
        this.comparisonPanel = cp;
        JMenuItem menuItem;

        menuItem = new JMenuItem("Locate File");
        menuItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                int column = selectedColumn - 1;
                if (column >= 0) {
                    final ChecksumFile file = comparisonPanel.getTableModel().getFile(column);
                    // TODO: a new thread is not needed at the moment, because relocateChecksumFile does not update the checksums
//                    Thread t = new Thread() {
//                        public void run() {
                    comparisonPanel.relocateChecksumFile(file);
//                        }
//                    };
//                    t.setPriority(Thread.NORM_PRIORITY);
//                    t.start();
                }
            }
        });
        add(menuItem);

        menuItem = new JMenuItem("Remove File");
        menuItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                int column = selectedColumn - 1;
                if (column >= 0) {
                    comparisonPanel.getTableModel().removeFile(column);
                }
            }
        });
        add(menuItem);
    }

    public void mouseEntered(MouseEvent e) {
    }

    public void mouseExited(MouseEvent e) {
    }

    public void mouseClicked(MouseEvent e) {
    }

    public void mousePressed(MouseEvent e) {
    }

    public void mouseReleased(MouseEvent e) {
        if (e.isPopupTrigger()) {
            JTableHeader tableHeader = (JTableHeader) e.getComponent();
            selectedColumn = tableHeader.columnAtPoint(new Point(e.getX(), e.getY()));
            if (selectedColumn > 0) {
                show(e.getComponent(), e.getX(), e.getY());
            }
        }
    }
}