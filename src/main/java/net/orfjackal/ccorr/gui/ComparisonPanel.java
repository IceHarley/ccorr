// Copyright © 2003-2006, 2010, Esko Luontola <www.orfjackal.net>
// This software is released under the GNU General Public License, version 2 or later.
// The license text is at http://www.gnu.org/licenses/gpl.html

package net.orfjackal.ccorr.gui;

import net.orfjackal.ccorr.checksum.*;
import net.orfjackal.ccorr.combination.*;
import net.orfjackal.ccorr.comparison.*;
import net.orfjackal.ccorr.settings.Settings;

import javax.swing.*;
import javax.swing.table.TableColumn;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.text.*;

/**
 * The user interface for a <code>Comparison</code>. It consists primarily of a <code>JTable</code>, which shows the
 * data a <code>Comparison</code> holds and provides means for changing the markers.
 *
 * @author Esko Luontola
 */
public class ComparisonPanel extends TabPanel {

    /**
     * The main part of this GUI.
     */
    private final JTable table;

    /**
     * The TableModel that the table uses.
     */
    private final ComparisonTableModel tableModel;

    /**
     * Creates a new ComparisonPanel that uses the given Comparison.
     *
     * @param comparison the Comparison to be used, or null for a new Comparison
     */
    public ComparisonPanel(Comparison comparison) {
        if (comparison == null) {
            comparison = new Comparison();
        }

        setLayout(new BorderLayout());

        /**
         * TABLE
         */
        tableModel = new ComparisonTableModel(comparison);
        table = new JTable(tableModel);

        table.getTableHeader().setReorderingAllowed(false);
        //table.getTableHeader().setResizingAllowed(false);
        table.getTableHeader().addMouseListener(new ComparisonHeaderPopupMenu(this));

        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        add(scrollPane, BorderLayout.CENTER);

        table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        table.setRowSelectionAllowed(true);
        table.setColumnSelectionAllowed(true);
        table.setSelectionMode(ListSelectionModel.SINGLE_INTERVAL_SELECTION);

        table.setDefaultRenderer(ComparisonItem.class, new ComparisonItemRenderer());

        // listen to mouse clicks that change markers
        table.addMouseListener(new MouseListener() {
            public void mouseClicked(MouseEvent e) {
            }

            public void mouseEntered(MouseEvent e) {
            }

            public void mouseExited(MouseEvent e) {
            }

            public void mousePressed(MouseEvent e) {
                if (e.getButton() != MouseEvent.BUTTON1) {
                    int row = table.rowAtPoint(e.getPoint());
                    int col = table.columnAtPoint(e.getPoint());

                    // check if the cell is part of a larger selection
                    int[] rows = table.getSelectedRows();
                    int[] cols = table.getSelectedColumns();

                    for (int r : rows) {
                        if (r == row) {
                            for (int c : cols) {
                                if (c == col) {
                                    markSelectedNext();
                                    return;
                                }
                            }
                        }
                    }

                    if (col == 0) {
                        switchShowParts();
                    } else {
                        tableModel.nextMark(row, col);
                    }
                }
            }

            public void mouseReleased(MouseEvent e) {
            }
        });

//      // resize columns to fit the contents
//      for (int i = 0; i < table.getColumnCount(); i++) {
//          TableColumn column = table.getColumnModel().getColumn(i);
//          Component comp = table.getTableHeader().getDefaultRenderer().
//                  getTableCellRendererComponent(
//                      null, column.getHeaderValue(), 
//                      false, false, 0, 0);
//          int headerWidth = comp.getPreferredSize().width;
//          comp = table.getDefaultRenderer(tableModel.getColumnClass(i)).
//                  getTableCellRendererComponent(
//                      table, tableModel.getValueAt(0, i),
//                      false, false, 0, i);
//          int cellWidth = comp.getPreferredSize().width;
//          column.setPreferredWidth(Math.max(headerWidth, cellWidth) + 10);
//      }


        add(createBottomPanel(), BorderLayout.SOUTH);
    }

    private JPanel createBottomPanel() {
        JPanel p = new JPanel();
        JButton button;

        p.setBorder(BorderFactory.createEtchedBorder());
        p.setLayout(new FlowLayout(FlowLayout.CENTER));

        button = new JButton("Add File");
        final JButton button1 = button;
        button.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                button1.setEnabled(false);
                Thread t = new Thread() {
                    public void run() {
                        createAndAddChecksumFile();
                        button1.setEnabled(true);
                    }
                };
                t.setPriority(Thread.NORM_PRIORITY);
                t.start();
            }
        });
        p.add(button);

        button = new JButton("Add Checksum File");
        final JButton button2 = button;
        button.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                button2.setEnabled(false);
                addChecksumFile();
                button2.setEnabled(true);
            }
        });
        p.add(button);

        button = new JButton("Write Output");
        final JButton button3 = button;
        button.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                button3.setEnabled(false);
                Thread t = new Thread() {
                    public void run() {
                        writeOutput();
                        button3.setEnabled(true);
                    }
                };
                t.setPriority(Thread.NORM_PRIORITY);
                t.start();
            }
        });
        p.add(button);

//        button = new JButton("(Notepad)");
//        button.addActionListener(new ActionListener() {
//            public void actionPerformed(ActionEvent e) {
//                openNotepad();
//            }
//        });
//        bottomPane.add(button);

        button = new JButton("Close");
        button.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                close();
            }
        });
        p.add(button);

        return p;
    }

    /**
     * Writes a good combination to a file.
     */
    public void writeOutput() {
        GoodCombination gc = tableModel.createGoodCombination();
        if (GoodCombination.NOT_EXISTS.equals(gc)) {

            // TODO: possible combinations
            JOptionPane.showMessageDialog(this,
                    "It is not possible to extract a good output file. "
                            + "More good parts are needed.",
                    "Error", JOptionPane.ERROR_MESSAGE);
        } else {
            JFileChooser chooser = new JFileChooser();
            chooser.setCurrentDirectory(Settings.getCurrentDirectory());

            boolean ok = false;
            do {
                int returnVal = chooser.showSaveDialog(this);
                if (returnVal == JFileChooser.APPROVE_OPTION) {

                    if (chooser.getSelectedFile().exists()) {
                        returnVal = JOptionPane.showConfirmDialog(this,
                                "The selected file exists. Do you want to overwrite it?",
                                "Overwrite File?", JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.WARNING_MESSAGE);
                        if (returnVal == JOptionPane.YES_OPTION) {
                            ok = true;
                        } else if (returnVal == JOptionPane.NO_OPTION) {
                            ok = false;
                        } else if (returnVal == JOptionPane.CANCEL_OPTION) {
                            return;
                        }
                    } else {
                        ok = true;
                    }

                } else {
                    return;
                }
            } while (!ok);

            // start writing
            ProgressMonitorRepository.set(new ProgressMonitor(this,
                    "Writing output", "", 0, 0));

            GoodCombinationWriter goodCombinationWriter = GoodCombinationWriterFactory.make(gc);
            OutputStream stream = StreamFactory.openOutputStream(chooser.getSelectedFile());
            boolean successful = goodCombinationWriter.writeCombination(stream);
            Settings.setCurrentDirectory(chooser.getSelectedFile());
            if (!successful) {
                JOptionPane.showMessageDialog(this,
                        "There was an error in writing the file \""
                                + chooser.getSelectedFile().getName() + "\".",
                        "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    /**
     * Creates a new ChecksumFile and adds it to the Comparison.
     */
    public void createAndAddChecksumFile() {
        File inputFile;
        File outputFile;
        String algorithm;
        long partLength;

        // select input file, algorithm and part length
        CreateChecksumsFileChooser chooser = new CreateChecksumsFileChooser();
        chooser.setCurrentDirectory(Settings.getCurrentDirectory());

        algorithm = tableModel.getAlgorithm();
        partLength = tableModel.getPartLength();

        if (algorithm != null && partLength > 0) {
            chooser.setAlgorithm(algorithm);
            chooser.setPartLength(partLength);
            chooser.setOptionsEnabled(false);
        }

        int returnVal = chooser.showOpenDialog(this);

        if (returnVal == JFileChooser.APPROVE_OPTION) {
            algorithm = chooser.getAlgorithm();
            partLength = chooser.getPartLength();

            Settings.setDefaultAlgorithm(algorithm);
            Settings.setDefaultPartLength(partLength);
            Settings.setCurrentDirectory(chooser.getSelectedFile());

            if (chooser.getSelectedFile().isFile()) {
                inputFile = chooser.getSelectedFile();
            } else {
                JOptionPane.showMessageDialog(this,
                        "This is not a file.",
                        "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
        } else {
            return;
        }

        // select where to save the output file
        outputFile = new File(inputFile.getAbsolutePath() + ".ccf");

        // check input file
        if (!inputFile.exists() || !inputFile.canRead()) {
            JOptionPane.showMessageDialog(this,
                    "Unable to read " + inputFile,
                    "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        // check output file
        try {
            if (!(outputFile.exists() && outputFile.canWrite()) && !(outputFile.createNewFile() && outputFile.canWrite())) {
                JOptionPane.showMessageDialog(this,
                        "Unable to write " + outputFile,
                        "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
        } catch (IOException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this,
                    "Unable to write " + outputFile,
                    "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        // do the checksums
        ProgressMonitorRepository.set(new ProgressMonitor(this,
                "Creating checksums", "", 0, 0));

        ChecksumFile cf = ChecksumFileFactory.createChecksumFile(inputFile, partLength, algorithm);

        if (cf == null) {
            JOptionPane.showMessageDialog(this,
                    "An error happened in making the checksum file.",
                    "Error", JOptionPane.ERROR_MESSAGE);
        } else {
            if (!new ChecksumFileLoader(cf).saveToFile(outputFile)) {
                JOptionPane.showMessageDialog(this,
                        "There was an error in writing " + outputFile,
                        "Error", JOptionPane.ERROR_MESSAGE);
            }
            addChecksumFile(cf);
            System.out.println("OK");
        }
    }

    /**
     * Pops up a dialog for relocating a file which has been moved. The file will be automatically checked for
     * consistency.
     */
    public void relocateChecksumFile(ChecksumFile checksumFile) {
        // TODO: this code is quite ugly in the way that it accesses the checksumFile and tableModel how it pleaces - some refactoring might do good

        File sourceFile = checksumFile.getSourceFile();
        long sourceFileLength = checksumFile.getSourceFileLength();

        CreateChecksumsFileChooser chooser = new CreateChecksumsFileChooser();
        chooser.setSelectedFile(sourceFile);
        chooser.setAlgorithm(checksumFile.getAlgorithm());
        chooser.setPartLength(checksumFile.getPartLength());
        chooser.setOptionsEnabled(false);

        int returnVal = chooser.showOpenDialog(this);
        if (returnVal == JFileChooser.APPROVE_OPTION) {
            File newSourceFile = chooser.getSelectedFile();
            Settings.setCurrentDirectory(newSourceFile);

            if (!newSourceFile.isFile()) {
                JOptionPane.showMessageDialog(this,
                        "No such file: " + newSourceFile,
                        "Error", JOptionPane.ERROR_MESSAGE);

            } else if (newSourceFile.length() != sourceFileLength) {
                NumberFormat format = new DecimalFormat();
                format.setGroupingUsed(true);
                format.setParseIntegerOnly(true);

                JOptionPane.showMessageDialog(this,
                        "The size of the selected file " +
                                "(" + format.format(newSourceFile.length()) + " bytes) " +
                                "is different from the size of the original file " +
                                "(" + format.format(sourceFileLength) + " bytes)",
                        "Error", JOptionPane.ERROR_MESSAGE);

            } else {
                checksumFile.setSourceFile(newSourceFile);

                // TODO: UpdateChecksums does not work well if the action is cancelled.
                // It would be best to make a copy of the ChecksumFile and add it to the project after
                // the checksums have been successfully updated. It would also be best to ask the user if
                // he wants to update the checksums at all. For now no updating is done at relocation.

//                Settings.set(new ProgressMonitor(this,
//                        "Updating checksums", "", 0, 0));
//                checksumFile.updateChecksums();
                getTableModel().fireFileRelocated();
            }
        }
    }


    /**
     * Adds a ChecksumFile to the Comparison.
     *
     * @param cf the ChecksumFile to be added
     */
    public void addChecksumFile(ChecksumFile cf) {
        if (cf != null) {
            tableModel.addFile(cf);
        }
    }

    /**
     * Shows a file dialog for loading a ChecksumFile from a file.
     */
    public void addChecksumFile() {
        JFileChooser chooser = new JFileChooser();
        String[] ext = {"ccf"};
        GeneralFileFilter filter = new GeneralFileFilter(ext, "CCorr Checksum File");
        chooser.setFileFilter(filter);
        chooser.setSelectedFile(new File("*.ccf"));
        chooser.setCurrentDirectory(Settings.getCurrentDirectory());
        int returnVal = chooser.showOpenDialog(this);

        if (returnVal == JFileChooser.APPROVE_OPTION) {

            ChecksumFile cf = ChecksumFileLoader.loadFromFile(chooser.getSelectedFile());
            Settings.setCurrentDirectory(chooser.getSelectedFile());
            if (cf != null) {
                tableModel.addFile(cf);
            } else {
                JOptionPane.showMessageDialog(this,
                        "The file \"" + chooser.getSelectedFile().getName()
                                + "\" was not recognized as a valid CCorr Checksum File.",
                        "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    /**
     * TODO: NOT IMPLEMENTED
     */
    public void openNotepad() {

    }

    /**
     * Saves any modifications made to the Comparison.
     */
    public boolean saveComparison() {
        // save where previously
        if (tableModel.getSavedAsFile() != null) {
            if (tableModel.saveToFile(tableModel.getSavedAsFile())) {
                return true;
            }
        }

        // otherwise save to a new file
        return this.saveComparisonAs();
    }

    /**
     * Saves the Comparison to a file and prompts for the file name.
     */
    public boolean saveComparisonAs() {
        JFileChooser chooser = new JFileChooser();
        String[] ext = {"ccp"};
        GeneralFileFilter filter = new GeneralFileFilter(ext, "CCorr Comparison Projects");
        chooser.setFileFilter(filter);
        File selectedFile;
        if (tableModel.getSavedAsFile() != null) {
            selectedFile = tableModel.getSavedAsFile();
        } else {
            selectedFile = new File("*.ccp");
        }
        chooser.setSelectedFile(selectedFile);
        chooser.setCurrentDirectory(Settings.getCurrentDirectory());

        boolean ok = false;
        do {
            int returnVal = chooser.showDialog(this, "Save As");
            if (returnVal == JFileChooser.APPROVE_OPTION) {

                if (chooser.getSelectedFile().exists()) {
                    returnVal = JOptionPane.showConfirmDialog(this,
                            "The selected file exists. Do you want to overwrite it?",
                            "Overwrite File?", JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.WARNING_MESSAGE);
                    if (returnVal == JOptionPane.YES_OPTION) {
                        ok = true;
                    } else if (returnVal == JOptionPane.NO_OPTION) {
                        ok = false;
                    } else if (returnVal == JOptionPane.CANCEL_OPTION) {
                        return false;
                    }
                } else {
                    ok = true;
                }

            } else {
                return false;
            }
        } while (!ok);

        boolean successful = tableModel.saveToFile(chooser.getSelectedFile());
        Settings.setCurrentDirectory(chooser.getSelectedFile());
        if (!successful) {
            JOptionPane.showMessageDialog(this,
                    "There was an error writing the file "
                            + chooser.getSelectedFile().getName() + ".",
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
        return successful;
    }

    /**
     * Closes this ComparisonPanel and prompts for saving any changes.
     */
    public boolean close() {
        if (tableModel.isModified()) {
            int returnVal = JOptionPane.showConfirmDialog(this,
                    "The comparison has been modified. Do you want to save the changes?",
                    "Save Comparison?", JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE);
            if (returnVal == JOptionPane.CANCEL_OPTION) {
                return false;
            }
            if (returnVal == JOptionPane.YES_OPTION) {
                if (!this.saveComparison()) {
                    return false;
                }
            }
        }

        return super.close();
    }

    /**
     * Returns the TableModel connected to this ComparisonPanel.
     */
    public ComparisonTableModel getTableModel() {
        return tableModel;
    }

    /**
     * Switches between showing part numbers and byte offsets in the first column.
     */
    public void switchShowParts() {
        tableModel.switchShowParts();

        // update column header
        TableColumn col = this.table.getColumnModel().getColumn(0);
        col.setHeaderValue(this.tableModel.getColumnName(0));
        this.table.getTableHeader().repaint();
    }

    /**
     * Sets the marker for all selected cells if all of them have the same old marker
     *
     * @param mark new marker value to be set
     */
    private void markSelected(Mark mark) {
        int[] rows = table.getSelectedRows();
        int[] cols = table.getSelectedColumns();
        tableModel.setMarks(rows, cols, mark);
    }

    /**
     * Marks the selected cell's ComparisonItem "good".
     */
    public void markSelectedGood() {
        markSelected(Mark.GOOD);
    }

    /**
     * Marks the selected cell's ComparisonItem "bad".
     */
    public void markSelectedBad() {
        markSelected(Mark.BAD);
    }

    /**
     * Marks the selected cell's ComparisonItem "unsure".
     */
    public void markSelectedUnsure() {
        markSelected(Mark.UNSURE);
    }

    /**
     * Marks the selected cell's ComparisonItem "undefined".
     */
    public void markSelectedUndefined() {
        markSelected(Mark.UNDEFINED);
    }

    /**
     * Changes the mark of the selected cell's ComparisonItem.
     */
    public void markSelectedNext() {
        markSelected(Mark.NEXT);
    }

    /**
     * Finds and marks cells in the table which appear to be good parts.
     */
    public void markGoodPartsInTable() {
        tableModel.markGoodParts(0, table.getRowCount() - 1);
    }

    /**
     * Sets all parts in the table to UNDEFINED.
     */
    public void markTableUndefined() {
        tableModel.markRowUndefined(0, table.getRowCount() - 1);
    }

    /**
     * Returns the name of the comparison.
     *
     * @return the name of the comparison as a <code>String</code>
     */
    public String getName() {
        return tableModel.getName();
    }

    /**
     * Sets the name of the comparison to the given <code>String</code>.
     *
     * @param name the new name of the comparison
     */
    public void setName(String name) {
        tableModel.setName(name);
    }
}