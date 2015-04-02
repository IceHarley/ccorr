// Copyright © 2003-2006, 2010, Esko Luontola <www.orfjackal.net>
// This software is released under the GNU General Public License, version 2 or later.
// The license text is at http://www.gnu.org/licenses/gpl.html

package net.orfjackal.ccorr.gui;

import net.orfjackal.ccorr.settings.Settings;

import javax.swing.*;
import java.awt.event.*;

/**
 * MenuBar for MainWindow.
 *
 * @author Esko Luontola
 */
public class MainMenuBar extends JMenuBar {

    private MainWindow parent;

    public MainMenuBar(MainWindow parent) {
        this.parent = parent;

        add(createFileMenu());
        add(createEditMenu());
        add(createToolsMenu());
        add(createHelpMenu());
    }

    private JMenu createFileMenu() {
        JMenu menu;
        JMenuItem menuItem;

        menu = new JMenu("File");
        menu.setMnemonic(KeyEvent.VK_F);

        menuItem = new JMenuItem("New", KeyEvent.VK_N);
        menuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_N, ActionEvent.CTRL_MASK));
        menuItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                parent.newComparisonPanel(null);
            }
        });
        menu.add(menuItem);

        menuItem = new JMenuItem("Open", KeyEvent.VK_O);
        menuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_O, ActionEvent.CTRL_MASK));
        menuItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                parent.openComparison();
            }
        });
        menu.add(menuItem);

        menuItem = new JMenuItem("Rename", KeyEvent.VK_R);
        menuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_R, ActionEvent.CTRL_MASK));
        menuItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                parent.renameComparison();
            }
        });
        menu.add(menuItem);

        menuItem = new JMenuItem("Close", KeyEvent.VK_F);
        menuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_W, ActionEvent.CTRL_MASK));
        menuItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                parent.closeCurrent();
            }
        });
        menu.add(menuItem);

        menu.addSeparator();

        menuItem = new JMenuItem("Save", KeyEvent.VK_S);
        menuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, ActionEvent.CTRL_MASK));
        menuItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                parent.saveComparison();
            }
        });
        menu.add(menuItem);

        menuItem = new JMenuItem("Save As", KeyEvent.VK_A);
        menuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F12, 0));
        menuItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                parent.saveComparisonAs();
            }
        });
        menu.add(menuItem);

        menu.addSeparator();

        menuItem = new JMenuItem("Exit", KeyEvent.VK_X);
        menuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Q, ActionEvent.CTRL_MASK));
        //menuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F4, ActionEvent.ALT_MASK));
        menuItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                parent.exitProgram();
            }
        });
        menu.add(menuItem);

        return menu;
    }

    private JMenu createEditMenu() {
        JMenu menu;
        JMenuItem menuItem;

        menu = new JMenu("Edit");
        menu.setMnemonic(KeyEvent.VK_E);

        menuItem = new JMenuItem("Mark Good", KeyEvent.VK_G);
//        menuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_1, ActionEvent.ALT_MASK));
        menuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_1, 0));
        menuItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if (parent.getSelectedTab() instanceof ComparisonPanel) {
                    ComparisonPanel panel = (ComparisonPanel) parent.getSelectedTab();
                    panel.markSelectedGood();
                }
            }
        });
        menu.add(menuItem);

        menuItem = new JMenuItem("Mark Bad", KeyEvent.VK_B);
//        menuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_2, ActionEvent.ALT_MASK));
        menuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_2, 0));
        menuItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if (parent.getSelectedTab() instanceof ComparisonPanel) {
                    ComparisonPanel panel = (ComparisonPanel) parent.getSelectedTab();
                    panel.markSelectedBad();
                }
            }
        });
        menu.add(menuItem);

        menuItem = new JMenuItem("Mark Unsure", KeyEvent.VK_U);
//        menuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_3, ActionEvent.ALT_MASK));
        menuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_3, 0));
        menuItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if (parent.getSelectedTab() instanceof ComparisonPanel) {
                    ComparisonPanel panel = (ComparisonPanel) parent.getSelectedTab();
                    panel.markSelectedUnsure();
                }
            }
        });
        menu.add(menuItem);

        menuItem = new JMenuItem("Mark Undefined", KeyEvent.VK_D);
//        menuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_4, ActionEvent.ALT_MASK));
        menuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_4, 0));
        menuItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if (parent.getSelectedTab() instanceof ComparisonPanel) {
                    ComparisonPanel panel = (ComparisonPanel) parent.getSelectedTab();
                    panel.markSelectedUndefined();
                }
            }
        });
        menu.add(menuItem);

        menu.addSeparator();

        menuItem = new JMenuItem("Next Marker", KeyEvent.VK_N);
        menuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_SPACE, 0));
        menuItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if (parent.getSelectedTab() instanceof ComparisonPanel) {
                    ComparisonPanel panel = (ComparisonPanel) parent.getSelectedTab();
                    panel.markSelectedNext();
                }
            }
        });
        menu.add(menuItem);

        return menu;
    }

    private JMenu createToolsMenu() {
        JMenu menu;
        JMenuItem menuItem;

        menu = new JMenu("Tools");
        menu.setMnemonic(KeyEvent.VK_T);

        menuItem = new JMenuItem("Try to Guess Good Parts", KeyEvent.VK_G);
        menuItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if (parent.getSelectedTab() instanceof ComparisonPanel) {
                    ComparisonPanel panel = (ComparisonPanel) parent.getSelectedTab();
                    panel.markGoodPartsInTable();
                }
            }
        });
        menu.add(menuItem);

        menuItem = new JMenuItem("Mark All Undefined", KeyEvent.VK_U);
        menuItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if (parent.getSelectedTab() instanceof ComparisonPanel) {
                    ComparisonPanel panel = (ComparisonPanel) parent.getSelectedTab();
                    panel.markTableUndefined();
                }
            }
        });
        menu.add(menuItem);

        return menu;
    }

    private JMenu createHelpMenu() {
        JMenu menu;
        JMenuItem menuItem;

        menu = new JMenu("Help");
        menu.setMnemonic(KeyEvent.VK_H);

        menuItem = new JMenuItem("Help Topics", KeyEvent.VK_H);
        menuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F1, 0));
        menuItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                parent.showHelp();
            }
        });
        menu.add(menuItem);

        menu.addSeparator();

        menuItem = new JMenuItem("About " + Settings.APP_NAME_SHORT, KeyEvent.VK_A);
        menuItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                parent.showAbout();
            }
        });
        menu.add(menuItem);

        return menu;
    }
}