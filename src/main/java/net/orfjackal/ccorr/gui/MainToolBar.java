// Copyright © 2003-2006, 2010, Esko Luontola <www.orfjackal.net>
// This software is released under the GNU General Public License, version 2 or later.
// The license text is at http://www.gnu.org/licenses/gpl.html

package net.orfjackal.ccorr.gui;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

/**
 * ToolBar for MainWindow.
 *
 * @author Esko Luontola
 */
public class MainToolBar extends JToolBar {

    private MainWindow parent;

    public MainToolBar(MainWindow m) {
        JButton button;

        this.parent = m;

        setFloatable(false);
        setRollover(true);
        setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(153, 153, 153)));

        button = new JButton(new ImageIcon(ClassLoader.getSystemResource("images/new.gif")));
        button.setToolTipText("New");
        button.setFocusable(false);
        button.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                parent.newComparisonPanel(null);
            }
        });
        add(button);

        button = new JButton(new ImageIcon(ClassLoader.getSystemResource("images/open.gif")));
        button.setToolTipText("Open");
        button.setFocusable(false);
        button.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                parent.openComparison();
            }
        });
        add(button);

        button = new JButton(new ImageIcon(ClassLoader.getSystemResource("images/close.gif")));
        button.setToolTipText("Close");
        button.setFocusable(false);
        button.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                parent.closeCurrent();
            }
        });
        add(button);

        button = new JButton(new ImageIcon(ClassLoader.getSystemResource("images/save.gif")));
        button.setToolTipText("Save");
        button.setFocusable(false);
        button.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                parent.saveComparison();
            }
        });
        add(button);

        button = new JButton(new ImageIcon(ClassLoader.getSystemResource("images/saveas.gif")));
        button.setToolTipText("Save As");
        button.setFocusable(false);
        button.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                parent.saveComparisonAs();
            }
        });
        add(button);
    }

}