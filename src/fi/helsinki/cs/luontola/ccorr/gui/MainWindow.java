/* 
 * Copyright (C) 2003-2005  Esko Luontola, http://ccorr.sourceforge.net
 *
 * This file is part of Corruption Corrector (CCorr).
 *
 * CCorr is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * CCorr is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with CCorr; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package fi.helsinki.cs.luontola.ccorr.gui;

import fi.helsinki.cs.luontola.ccorr.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import javax.swing.*;

/**
 * Corruption Corrector's main window. It consists of menus, a toolbar and a
 * <code>JTabbedPane</code> that holds all the program's views.
 *
 * @author      Esko Luontola
 */
public class MainWindow extends JFrame {
    
    /**
     * The central <code>JTabbedPane</code>.
     */
    private final JTabbedPane tabbedPane;
    
//  private final JLabel statusbar;
    
    /**
     * Starts the program.
     */
    public static void main(String[] args) {
        MainWindow frame = new MainWindow();
        frame.pack();
        
        // window size and position
        Rectangle bounds = Settings.getWindowBounds();
        Rectangle maxBounds = GraphicsEnvironment.getLocalGraphicsEnvironment().getMaximumWindowBounds(); 
        if (bounds.x < 0) {
            bounds.x = 0;
        }
        if (bounds.y < 0) {
            bounds.y = 0;
        }
        if (bounds.width > maxBounds.width) {
            bounds.width = maxBounds.width;
        }
        if (bounds.height > maxBounds.height) {
            bounds.height = maxBounds.height;
        }
        if (bounds.x + bounds.width > maxBounds.width) {
            bounds.x = maxBounds.width - bounds.width;
        }
        if (bounds.y + bounds.height > maxBounds.height) {
            bounds.y = maxBounds.height - bounds.height;
        }
        frame.setBounds(bounds);
        frame.setVisible(true);
    }
    
    /**
     * Creates a new <code>MainWindow</code>. This should be run only once
     * when the program starts.
     */
    public MainWindow() {
        JButton button;
        JMenu menu;
        JMenuBar menuBar;
        JMenuItem menuItem;
        JPanel panel;
        JToolBar toolBar;
        
        setTitle(Settings.APP_NAME + " " + Settings.VERSION_NUMBER);
        setIconImage(new ImageIcon(ClassLoader.getSystemResource("images/logo.gif")).getImage());
        
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        try {
        	UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
    	} catch (Exception e) {}
        
        setJMenuBar(new MainMenuBar(this));
        getContentPane().add(new MainToolBar(this), BorderLayout.NORTH);
        
        // TODO: class StatusBar
//      panel = new JPanel();
//      panel.setBorder(BorderFactory.createLoweredBevelBorder());
//      getContentPane().add(panel, BorderLayout.SOUTH);
//      statusbar = new JLabel("Status Bar");
//      statusbar.setFont(statusbar.getFont().deriveFont(Font.PLAIN));
//      panel.add(statusbar);
        
        tabbedPane = new JTabbedPane();
        tabbedPane.setTabLayoutPolicy(JTabbedPane.SCROLL_TAB_LAYOUT);
        //tabbedPane.setTabLayoutPolicy(JTabbedPane.WRAP_TAB_LAYOUT);
        getContentPane().add(tabbedPane, BorderLayout.CENTER);
        newComparisonPanel(null);
        
        // TEST:
        //newComparisonPanel(Comparison.loadFromFile(new File("G:\\test.ccp")));
        
        
        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                exitProgram();
            }
        });
    }
    
    public TabPanel getSelectedTab() {
    	return (TabPanel) tabbedPane.getSelectedComponent();
	}
    
    /**
     * Closes the currently selected tab.
     */
    public boolean closeCurrent() {
        TabPanel tp = (TabPanel)getSelectedTab();
        if (tp != null) {
        	return tp.close();
        } else {
        	return false;
    	}
    }
    
    /**
     * Exits the program after saving the settings.
     */
    public void exitProgram() {
        
        // close open files, prompts automatically for saving modified files
        while (tabbedPane.getTabCount() > 0) {
            if (closeCurrent() == false) {
                return;     // operation cancelled
            }
        }
        
        // save settings
        Settings.setWindowBounds(getBounds());
        Settings.saveSettings();
        
        System.exit(0);
    }
    
    /**
     * Used to create a unique name for each <code>ComparisonPanel</code>.
     */
    private int comparisonNumber = 0;
    
    /**
     * Opens a <code>ComparisonPanel</code> to the central <code>JTabbedPane</code>.
     *
     * @param	c	the <code>Comparison</code> to be opened, 
     *				or null to create a new one
     */
    public void newComparisonPanel(Comparison c) {
        if (c != null) {
            
            // try opening to an empty tab
            int lastIndex = tabbedPane.getTabCount() - 1;
            if (lastIndex >= 0) {
                
                // open to selected index?
                if (getSelectedTab() instanceof ComparisonPanel) {
                    Component comp = getSelectedTab();
                    
                    ComparisonPanel cp = (ComparisonPanel) (comp);
                    ComparisonTableModel model = cp.getTableModel();
                    if (!model.isModified() && model.getFiles() == 0) {
                        ComparisonPanel panel = new ComparisonPanel(c);
                        tabbedPane.setTitleAt(tabbedPane.indexOfComponent(comp), panel.getName());
                        tabbedPane.setComponentAt(tabbedPane.indexOfComponent(comp), panel);
                        tabbedPane.setSelectedComponent(panel);
                        return;
                    }
                }
                
                // open to the last index?
                if (tabbedPane.getComponentAt(lastIndex) instanceof ComparisonPanel) {
                    Component comp = tabbedPane.getComponentAt(lastIndex);
                    
                    ComparisonPanel cp = (ComparisonPanel) (comp);
                    ComparisonTableModel model = cp.getTableModel();
                    if (!model.isModified() && model.getFiles() == 0) {
                        ComparisonPanel panel = new ComparisonPanel(c);
                        tabbedPane.setTitleAt(lastIndex, panel.getName());
                        tabbedPane.setComponentAt(tabbedPane.indexOfComponent(comp), panel);
                        tabbedPane.setSelectedComponent(panel);
                        return;
                    }
                }
            }
        } else {
            c = new Comparison();
        }
        
        // create a new tab
        comparisonNumber++;
        ComparisonPanel panel = new ComparisonPanel(c);
        
        if (panel.getName().equals("")) {
            panel.setName("Comparison " + comparisonNumber);
        }
        
        tabbedPane.addTab(panel.getName(), panel);
        tabbedPane.setSelectedComponent(panel);
    }
    
    /**
     * Shows a file dialog and asks to open a <code>Comparison</code> from a file.
     */
    public void openComparison() {
        JFileChooser chooser = new JFileChooser();
        String[] ext = { "ccp" };
        GeneralFileFilter filter = new GeneralFileFilter(ext, "CCorr Comparison Projects");
        chooser.setFileFilter(filter);
        chooser.setSelectedFile(new File("*.ccp"));
        chooser.setCurrentDirectory(Settings.getCurrentDirectory());
        
        int returnVal = chooser.showOpenDialog(this);
        if (returnVal == JFileChooser.APPROVE_OPTION) {
            
            Comparison comp = Comparison.loadFromFile(chooser.getSelectedFile());
            Settings.setCurrentDirectory(chooser.getSelectedFile());
            if (comp != null) {
                newComparisonPanel(comp);
            } else {
                JOptionPane.showMessageDialog(this, 
                        "The file " + chooser.getSelectedFile().getName() 
                        + " was not recognized as a valid CCorr Comparison Project.", 
                        "File Format Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
    
    /**
     * Saves the the currenty selected <code>ComparisonPanel</code> (if selected) 
     * by calling <code>ComparisonPanel.saveComparison()</code>.
     */
    public void saveComparison() {
        if (getSelectedTab() instanceof ComparisonPanel) {
            ComparisonPanel cp = (ComparisonPanel)getSelectedTab();
            cp.saveComparison();
        }
    }
    
    /**
     * Saves the the currenty selected <code>ComparisonPanel</code> (if selected) 
     * by calling <code>ComparisonPanel.saveComparisonAs()</code>.
     */
    public void saveComparisonAs() {
        if (getSelectedTab() instanceof ComparisonPanel) {
            ComparisonPanel cp = (ComparisonPanel)getSelectedTab();
            cp.saveComparisonAs();
        }
    }
    
    /**
     *  Shows a dialog for entering a new comparison name.
     */
    public void renameComparison() {
        ComparisonPanel cp = (ComparisonPanel)getSelectedTab();
        ComparisonTableModel model = cp.getTableModel();
        
        String name = model.getName();

        JOptionPane renameDialog = new JOptionPane();
        
        name = (String) renameDialog.showInputDialog(
                    getContentPane(),
                    "Enter new comparison name:",
                    "Rename Comparison",
                    JOptionPane.PLAIN_MESSAGE,
                    null,
                    null,
                    name);
        
        if (name != null && !name.equals("")) {
            cp.setName(name);
            tabbedPane.setTitleAt(tabbedPane.indexOfComponent(cp), name);
        }
    }
    
    /**
     * Shows the help dialog.
     */
    public void showHelp() {
    	new HelpDialog(this);
    }
    
    /**
     * Shows the about dialog.
     */
    public void showAbout() {
    	new AboutDialog(this);
    }
}