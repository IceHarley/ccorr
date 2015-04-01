// Copyright © 2003-2006, 2010, Esko Luontola <www.orfjackal.net>
// This software is released under the GNU General Public License, version 2 or later.
// The license text is at http://www.gnu.org/licenses/gpl.html

package net.orfjackal.ccorr.gui;

import net.orfjackal.ccorr.Comparison;
import net.orfjackal.ccorr.ComparisonLoader;
import net.orfjackal.ccorr.Settings;
import net.orfjackal.ccorr.SettingsLoader;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;

/**
 * Corruption Corrector's main window. It consists of menus, a toolbar and a <code>JTabbedPane</code> that holds all the
 * program's views.
 *
 * @author Esko Luontola
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
     * Creates a new <code>MainWindow</code>. This should be run only once when the program starts.
     */
    public MainWindow() {
//        JButton button;
//        JMenu menu;
//        JMenuBar menuBar;
//        JMenuItem menuItem;
//        JPanel panel;
//        JToolBar toolBar;

        setTitle(Settings.APP_NAME + " " + Settings.VERSION_NUMBER);
        setIconImage(new ImageIcon(ClassLoader.getSystemResource("images/logo.gif")).getImage());

        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        try {
            UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }

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
        TabPanel tp = getSelectedTab();
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
            if (!closeCurrent()) {
                return;     // operation cancelled
            }
        }

        // save settings
        Settings.setWindowBounds(getBounds());
        SettingsLoader.saveSettings();

        System.exit(0);
    }

    /**
     * Used to extract a unique name for each <code>ComparisonPanel</code>.
     */
    private int comparisonNumber = 0;

    /**
     * Opens a <code>ComparisonPanel</code> to the central <code>JTabbedPane</code>.
     *
     * @param c the <code>Comparison</code> to be opened, or null to extract a new one
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

        // extract a new tab
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
        String[] ext = {"ccp"};
        GeneralFileFilter filter = new GeneralFileFilter(ext, "CCorr Comparison Projects");
        chooser.setFileFilter(filter);
        chooser.setSelectedFile(new File("*.ccp"));
        chooser.setCurrentDirectory(Settings.getCurrentDirectory());

        int returnVal = chooser.showOpenDialog(this);
        if (returnVal == JFileChooser.APPROVE_OPTION) {

            Comparison comp = new ComparisonLoader(chooser.getSelectedFile()).getComparison();
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
     * Saves the the currenty selected <code>ComparisonPanel</code> (if selected) by calling
     * <code>ComparisonPanel.saveComparison()</code>.
     */
    public void saveComparison() {
        if (getSelectedTab() instanceof ComparisonPanel) {
            ComparisonPanel cp = (ComparisonPanel) getSelectedTab();
            cp.saveComparison();
        }
    }

    /**
     * Saves the the currenty selected <code>ComparisonPanel</code> (if selected) by calling
     * <code>ComparisonPanel.saveComparisonAs()</code>.
     */
    public void saveComparisonAs() {
        if (getSelectedTab() instanceof ComparisonPanel) {
            ComparisonPanel cp = (ComparisonPanel) getSelectedTab();
            cp.saveComparisonAs();
        }
    }

    /**
     * Shows a dialog for entering a new comparison name.
     */
    public void renameComparison() {
        ComparisonPanel cp = (ComparisonPanel) getSelectedTab();
        ComparisonTableModel model = cp.getTableModel();

        String name = model.getName();

        name = (String) JOptionPane.showInputDialog(
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