// Copyright © 2003-2006, 2010, Esko Luontola <www.orfjackal.net>
// This software is released under the GNU General Public License, version 2 or later.
// The license text is at http://www.gnu.org/licenses/gpl.html

package net.orfjackal.ccorr.gui;

import net.orfjackal.ccorr.*;

import javax.swing.table.AbstractTableModel;
import java.io.File;

/**
 * A <code>TableModel</code> for showing a <code>Comparison</code> in a <code>JTable</code>.
 *
 * @author Esko Luontola
 */
public class ComparisonTableModel extends AbstractTableModel {

    /**
     * The Comparison object that this ComparisonPanel represents.
     */
    private Comparison comparison;
    private ComparisonLoader comparisonLoader;

    /**
     * Indicates if this Comparison has been modified after saving.
     */
    private boolean isModified = false;

    /**
     * Indicates whether part numbers or byte/KB/MB offsets are shown.
     */
    private int showMode = 0;

    private static final int SHOW_PARTS = 0;
    private static final int SHOW_BYTES = 1;
    private static final int SHOW_KILOBYTES = 2;
    private static final int SHOW_MEGABYTES = 3;

    /**
     * Creates a new instance of this class.
     *
     * @param comparison the Comparison object that this should represent, or null to create a new Comparison
     */
    public ComparisonTableModel(Comparison comparison) {
        if (comparison == null) {
            comparison = new Comparison();
        }
        this.comparison = comparison;
        this.comparisonLoader = new ComparisonLoader(comparison);
    }

    /**
     * Returns the number of columns in the model.
     */
    public int getColumnCount() {
        return this.comparison.getFilesCount() + 1;
    }

    /**
     * Returns the number of rows in the model.
     */
    public int getRowCount() {
        return this.comparison.getDifferences();
    }

    /**
     * Returns the name of the column at columnIndex.
     */
    public String getColumnName(int columnIndex) {
        String result;
        if (columnIndex == 0) {
            switch (showMode) {
                default:
                case SHOW_PARTS:
                    result = "Part #";
                    break;
                case SHOW_BYTES:
                    result = "Bytes";
                    break;
                case SHOW_KILOBYTES:
                    result = "KBytes";
                    break;
                case SHOW_MEGABYTES:
                    result = "MBytes";
                    break;
            }
        } else {
            result = this.comparison.getFile(columnIndex - 1).getSourceFile().getName();
        }
        return result;
    }

    /**
     * Returns the value for the cell at rowIndex and columnIndex.
     */
    public Object getValueAt(int rowIndex, int columnIndex) {
        final int DECIMALS = 100;
        Object result;
        if (columnIndex == 0) {
            switch (showMode) {
                default:
                case SHOW_PARTS:
                    result = Integer.toString(comparison.getPart(rowIndex) + 1);
                    break;
                case SHOW_BYTES:
                    result = comparison.getStartOffset(rowIndex)
                            + "-" + comparison.getEndOffset(rowIndex);
                    break;
                case SHOW_KILOBYTES:
                    result = (double) Math.round((double) comparison.getStartOffset(rowIndex) / 1024 * DECIMALS) / DECIMALS
                            + "-" + (double) Math.round((double) comparison.getEndOffset(rowIndex) / 1024 * DECIMALS) / DECIMALS;
                    break;
                case SHOW_MEGABYTES:
                    result = (double) Math.round((double) comparison.getStartOffset(rowIndex) / 1024 / 1024 * DECIMALS) / DECIMALS
                            + "-" + (double) Math.round((double) comparison.getEndOffset(rowIndex) / 1024 / 1024 * DECIMALS) / DECIMALS;
                    break;
            }
        } else {
            result = this.comparison.getItem(rowIndex, columnIndex - 1);
        }
        return result;
    }

    /**
     * Returns true if the cell at rowIndex and columnIndex is editable.
     */
    public boolean isCellEditable(int rowIndex, int columnIndex) {
        // if columnIndex==0, enable copying to clipboard, modifying not possible
        return columnIndex == 0;
    }

    /**
     * Returns true if the Comparison has been modified after saving.
     */
    public boolean isModified() {
        return this.isModified;
    }

    /**
     * Switches between showing part numbers and byte offsets in the first column. Does only fireTableDataChanged and
     * not update the column header.
     */
    public void switchShowParts() {
        showMode = (showMode + 1) % 4;
        fireTableDataChanged();
    }

    /**
     * Represents the corresponding method in the comparison.
     */
    public int getFiles() {
        return this.comparison.getFilesCount();
    }

    /**
     * Represents the corresponding method in the comparison.
     */
    public ChecksumFile getFile(int file) {
        return this.comparison.getFile(file);
    }

    /**
     * Represents the corresponding method in the comparison.
     */
    public void setMark(int row, int col, Mark mark) {
        isModified = true;
        comparison.setMark(row, col - 1, mark);
        fireTableRowsUpdated(row, row);
    }

    /**
     * Represents the corresponding method in the comparison.
     */
    public Mark getMark(int row, int col) {
        return comparison.getMark(row, col - 1);
    }

    /**
     * Set multiple markers at once.
     */
    public boolean setMarks(int[] rows, int[] cols, Mark mark) {

        // do not allow more than one column to be set
        if (cols.length != 1) {
            return false;
        }

        // do not allow the first column to be set
        if (cols[0] == 0) {
            return false;
        }

        Mark oldMark = getMark(rows[0], cols[0]);

        // all selected cells must have the same old marker
        for (int row : rows) {
            for (int col : cols) {
                if (getMark(row, col) != oldMark) {
                    return false;
                }
            }
        }

        // set the markers
        for (int row : rows) {
            for (int col : cols) {
                setMark(row, col, mark);
            }
        }
        return true;
    }

    /**
     * Represents the corresponding method in the comparison.
     */
    public Mark nextMark(int row, int col) {
        isModified = true;
        Mark result = this.comparison.nextMark(row, col - 1);
        fireTableRowsUpdated(row, row);
        return result;
    }

    /**
     * Represents the corresponding method in the comparison.
     */
    public String getName() {
        return comparison.getName();
    }

    /**
     * Represents the corresponding method in the comparison.
     */
    public void setName(String name) {
        comparison.setName(name);
    }

    /**
     * Represents the corresponding method in the comparison.
     */
    public String getAlgorithm() {
        return comparison.getAlgorithm();
    }

    /**
     * Represents the corresponding method in the comparison.
     */
    public long getPartLength() {
        return comparison.getPartLength();
    }

    /**
     * Represents the corresponding method in the comparison.
     */
    public void addFile(ChecksumFile file) {
        isModified = true;
        comparison.addFile(file);
        comparison.doCompare();
        fireTableStructureChanged();
    }

    /**
     * Represents the corresponding method in the comparison.
     */
    public void removeFile(int index) {
        isModified = true;
        comparison.removeFile(index);
        comparison.doCompare();
        fireTableStructureChanged();
    }

    /**
     * Notifies the tableModel that {@link ComparisonPanel#relocateChecksumFile} has been run.
     */
    public void fireFileRelocated() {
        isModified = true;
        comparison.doCompare();
        fireTableStructureChanged();
    }

    /**
     * Represents the corresponding method in the comparison.
     */
    public FileCombination createGoodCombination() {
        return comparison.createGoodCombination();
    }

    /**
     * Represents the corresponding method in the comparison.
     */
    public File getSavedAsFile() {
        return comparisonLoader.getSavedAsFile();
    }

    /**
     * Represents the corresponding method in the comparison.
     */
    public boolean saveToFile(File file) {
        boolean successful = comparisonLoader.saveToFile(file);
        if (successful) {
            isModified = false;
        }
        return successful;
    }

    /**
     * Represents the corresponding method in the comparison.
     */
    public boolean markGoodParts(int start, int end) {
        boolean successful = new GoodPartsMarker(comparison).markGoodParts(start, end);
        if (successful) {
            isModified = true;
            fireTableRowsUpdated(start, end);
        }
        return successful;
    }

    /**
     * Represents the corresponding method in the comparison.
     */
    public boolean markRowUndefined(int start, int end) {
        boolean successful = this.comparison.markRowUndefined(start, end);
        if (successful) {
            isModified = true;
            fireTableRowsUpdated(start, end);
        }
        return successful;
    }
}