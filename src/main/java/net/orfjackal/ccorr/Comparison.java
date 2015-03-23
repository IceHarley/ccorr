// Copyright © 2003-2006, 2010, Esko Luontola <www.orfjackal.net>
// This software is released under the GNU General Public License, version 2 or later.
// The license text is at http://www.gnu.org/licenses/gpl.html

package net.orfjackal.ccorr;

import java.io.*;
import java.util.*;

/**
 * This class is used to represent a CCorr Comparison Project. <code>Comparison</code> is the central class of
 * Corruption Corrector and it is used to compare <code>ChecksumFile</code> objects, mark corrupt/uncorrupted parts and
 * create <code>FileCombination</code> objects. The files to be compared need to be first added to the
 * <code>Comparison</code>, after which they are compared with the <code>doCompare</code> method. <code>doCompare</code>
 * needs to be run after adding or removing files, because otherwise the data is not up to date and most of the methods
 * will refuse to work.
 *
 * @author Esko Luontola
 */
public class Comparison implements Serializable {
    private String name;
    private ChecksumFiles files;
    private ComparisonItems items;
    Similarity similarity;
    private boolean needsUpdating;
    private List<Integer> partsThatDiffer = new ArrayList<Integer>();
    private NumberOfDifferences numberOfDifferences;

    public Comparison() {
        this.name = "";
        this.files = new ChecksumFiles();
        this.similarity = new Similarity(0);
        this.needsUpdating = false;
        this.items = new ComparisonItems();
    }

    public void doCompare() {
        if (files.size() > 1)
            compareMultipleFiles();
        else if (files.size() == 1)
            compareSingleFile();
        else if (files.size() == 0)
            compareNoFile();
        logComparison();
    }

    private void logComparison() {
        if (this.getDifferences() > 100) {
            Log.println("doCompare:\n[over 100 differences, output hidden]");
        } else {
            Log.println("doCompare:\n" + this.toString());
        }
    }

    private void compareMultipleFiles() {
        partsThatDiffer = new ArrayList<Integer>();
        numberOfDifferences = new NumberOfDifferences(files.size());
        compareEachFilesPair();
        storeComparisonDataToList();
        calculateSimilarity();
        needsUpdating = false;
    }

    private void compareEachFilesPair() {
        for (int i = 0; i < (files.size() - 1); i++)
            for (int j = (i + 1); j < files.size(); j++)
                compareFiles(numberOfDifferences, i, j);
    }

    private void compareFiles(NumberOfDifferences numberOfDifferences, int file1, int file2) {
        int shortest = files.findShorterFileParts(file1, file2);

        for (int part = 0; part < shortest; part++)
            if (files.get(file1).partPresentInFile(part) && files.get(file2).partPresentInFile(part))
                if (!files.arePartsEquals(file1, file2, part)) {
                    numberOfDifferences.add(file1, file2);
                    if (!partsThatDiffer.contains(part)) partsThatDiffer.add(part);
                }
    }

    private void storeComparisonDataToList() {
        for (Integer part : partsThatDiffer)
            for (int f = 0; f < files.size(); f++) {
                String checksum = files.get(f).getChecksum(part);
                if (items.find(part, checksum) == null) {
                    ComparisonItem item = new ComparisonItem(checksum, part);
                    items.add(item);
                }
            }
    }

    private void calculateSimilarity() {
        similarity = new Similarity(files.size());
        for (int i = 0; i < files.size(); i++) {          // each file is also compared to itself
            for (int j = i; j < files.size(); j++) {
                int shortest = files.findShorterFileParts(i, j);
                double d = 1.0 - ((1.0 * numberOfDifferences.get(i, j)) / shortest);
                similarity.set(i, j, d);
            }
        }
    }

    private void compareNoFile() {
        partsThatDiffer = new ArrayList<Integer>();
        items = new ComparisonItems();
        similarity = new Similarity(0);
        needsUpdating = false;
    }

    private void compareSingleFile() {
        partsThatDiffer = new ArrayList<Integer>();
        items = new ComparisonItems();
        similarity = new Similarity(1);
        similarity.set(0, 0, 1.0);
        needsUpdating = false;
    }

    /**
     * Returns the number of parts the have differences between the compared files.
     *
     * @return the number of differences, or -1 if needs updating
     */
    public int getDifferences() {
        if (needsUpdating) {
            return -1;
        } else {
            return partsThatDiffer.size();
        }
    }

    /**
     * Returns the number of <code>ChecksumFile</code>s in this <code>Comparison</code>.
     *
     * @return the number of files
     */
    public int getFilesCount() {
        return this.files.size();
    }

    /**
     * Returns the part related to the given difference.
     *
     * @param difference the index of the difference
     * @return the index of the part, or -1 if parameter invalid
     */
    public int getPart(int difference) {
        if (difference < 0 || difference >= partsThatDiffer.size()) {
            return -1;
        } else {
            return partsThatDiffer.get(difference);
        }
    }

    /**
     * Used to check that the given index is not out of bounds and that this <code>Comparison</code> does not need
     * updating.
     *
     * @param difference the index of the difference
     * @param file       the index of the file
     */
    boolean isGoodIndex(int difference, int file) {
        return !(this.needsUpdating
                || difference < 0
                || difference >= partsThatDiffer.size()
                || file < 0
                || file >= files.size());
    }

    /**
     * Returns the <code>ComparisonItem</code> in the given index.
     *
     * @param difference the index of the difference
     * @param file       the index of the file
     * @return reference to the item
     */
    public ComparisonItem getItem(int difference, int file) {
        if (this.isGoodIndex(difference, file)) {
            int part = partsThatDiffer.get(difference);
            return items.find(part, files.get(file).getChecksum(part));
        } else {
            return null;
        }
    }

    /**
     * Returns the checksum in the given index.
     *
     * @param difference the index of the difference
     * @param file       the index of the file
     * @return the checksum, or "" if the requested item does not exist
     */
    public String getChecksum(int difference, int file) {
        ComparisonItem item = getItem(difference, file);
        if (item != null) {
            return item.getChecksum();
        } else {
            return "";
        }
    }

    /**
     * Returns the start offset of the given difference.
     *
     * @param difference the index of the difference
     * @return the biggest index of the part's first byte in all files
     */
    public long getStartOffset(int difference) {
        if (this.isGoodIndex(difference, 0)) {
            long offset = -1;
            for (int i = 0; i < this.getFilesCount(); i++) {
                ComparisonItem item = getItem(difference, i);
                if (item != null && files.get(i).getStartOffset(item.getPart()) > offset) {
                    offset = files.get(i).getStartOffset(item.getPart());
                }
            }
            return offset;
        } else {
            return -1;
        }
    }

    /**
     * Returns the start offset of the given difference.
     *
     * @param difference the index of the difference
     * @return the biggest index of the part's last byte in all files
     */
    public long getEndOffset(int difference) {
        if (this.isGoodIndex(difference, 0)) {
            long offset = -1;
            for (int i = 0; i < this.getFilesCount(); i++) {
                ComparisonItem item = getItem(difference, i);
                if (item != null && files.get(i).getEndOffset(item.getPart()) > offset) {
                    offset = files.get(i).getEndOffset(item.getPart());
                }
            }
            return offset;
        } else {
            return -1;
        }
    }

    public void setMark(int difference, int file, Mark mark) {
        if (this.isGoodIndex(difference, file)) {
            ComparisonItem item = getItem(difference, file);
            if (item != null)
                item.setMark(mark);
        }
    }

    /**
     * Returns the mark in the given index.
     *
     * @param difference the index of the difference
     * @param file       the index of the file
     * @return the current mark, or -1 if the requested item does not exist
     */
    public Mark getMark(int difference, int file) {
        ComparisonItem item = getItem(difference, file);
        if (item != null) {
            return item.getMark();
        } else {
            return Mark.BAD;
        }
    }

    /**
     * Changes to the next mark in the given index.
     *
     * @param difference the index of the difference
     * @param file       the index of the file
     * @return the new mark that was set
     */
    public Mark nextMark(int difference, int file) {
        ComparisonItem item = getItem(difference, file);
        if (item != null) {
            Mark result = Mark.nextMark(item.getMark());
            item.setMark(result);
            return result;
        } else {
            return Mark.NOT_EXISTS;
        }
    }

    /**
     * Returns the name of the comparison.
     *
     * @return the name of the comparison as a <code>String</code>
     */
    public String getName() {
        return this.name;
    }

    /**
     * Sets the name of the comparison to the given <code>String</code>.
     *
     * @param name the new name of the comparison
     */
    public void setName(String name) {
        this.name = name;
        Log.print("Comparison.setName: Set name to \"" + name + "\".");
    }

    /**
     * Returns the name of the algorithm that was used for making the checksums.
     *
     * @return the name of the algorithm
     * @see CRCAlgorithmFactory#getSupportedAlgorithms()
     */
    public String getAlgorithm() {
        if (files.size() == 0) {
            return null;
        } else {
            return files.get(0).getAlgorithm();
        }
    }

    /**
     * Returns the part length that was used for making the checksums.
     *
     * @return the length in bytes
     */
    public long getPartLength() {
        if (files.size() == 0) {
            return -1;
        } else {
            return files.get(0).getPartLength();
        }
    }

    /**
     * Returns the <code>ChecksumFile</code> in the given index.
     *
     * @param file the index of the file
     * @return the requested <code>ChecksumFile</code>, or null if parameter invalid
     */
    public ChecksumFile getFile(int file) {
        if (file < 0 || file >= this.files.size()) {
            return null;
        } else {
            return this.files.get(file);
        }
    }

    /**
     * Adds a <code>ChecksumFile</code> the this <code>Comparison</code>. The <code>ChecksumFile</code> given as
     * parameter must have the same part size and algorithm as all the other <code>ChecksumFile</code>s that are part of
     * this <code>Comparison</code>, and the same object is not accepted twice. If the requirements are not met, nothing
     * is done. After adding files {@link #doCompare() doCompare} must be run.
     *
     * @param file the <code>ChecksumFile</code> to be added
     */
    public void addFile(ChecksumFile file) {
        if (file != null) {

            // must have same part size and algorithm
            if ((getPartLength() > 0 && file.getPartLength() != getPartLength())
                    || (getAlgorithm() != null && !file.getAlgorithm().equals(getAlgorithm()))) {
                return;
            }

            // no duplicates wanted
            for (ChecksumFile f : this.files)
                if (file == f) return;

            files.add(file);
            this.needsUpdating = true;      // somebody should run doCompare()
        }
    }

    /**
     * Removes the <code>ChecksumFile</code> in the given index from this <code>Comparison</code>. After removing files
     * {@link #doCompare() doCompare} must be run or most of this <code>Comparison</code>'s methods will refuse to
     * work.
     *
     * @param file the index of the file
     */
    public void removeFile(int file) {
        if (file >= 0 && file < this.files.size()) {
            this.removeFile(this.files.get(file));
        }
    }

    /**
     * Removes the given <code>ChecksumFile</code> object from this <code>Comparison</code>. After removing files {@link
     * #doCompare() doCompare} must be run or most of this <code>Comparison</code>'s methods will refuse to work.
     *
     * @param file a reference to the file
     */
    public void removeFile(ChecksumFile file) {

        int removeFromIndex = files.indexOf(file);
        if (files.remove(file)) {
            Log.print("Comparison.removeFile: File #" + (removeFromIndex + 1) + " removed.");
            this.needsUpdating = true;
        }
    }

    /**
     * Creates a <code>FileCombination</code> from the parts marked as good. If in one difference index there is no item
     * with GOOD as the marker, a good combination will not be possible.
     *
     * @return a good combination, or null if it is not possible or if updating is needed or if there are no files
     */
    public FileCombination createGoodCombination() {
        Log.print("createGoodCombination: Start");
        if (this.needsUpdating) {
            Log.print("createGoodCombination: Aborted, needsUpdating == true");
            return null;
        }
        if (getDifferences() == 0) {
            Log.print("createGoodCombination: Aborted, no parts available");
            return null;
        }


        FileCombination fc = new FileCombination();
        long nextStart = 0;

        item:
        for (int difference = 0; difference < getDifferences(); difference++) {
            for (int fileIndex = 0; fileIndex < this.files.size(); fileIndex++) {
                if (getMark(difference, fileIndex) == Mark.GOOD) {
                    ChecksumFile checksumFile = files.get(fileIndex);
                    File file = checksumFile.getSourceFile();
                    long start = nextStart;
                    long end;

                    if (difference == (getDifferences() - 1)) {
                        // last part
                        end = checksumFile.getSourceFileLength();
                    } else {
                        end = checksumFile.getEndOffset(difference);
                        nextStart = end + 1;
                    }

                    fc.addItem(file, start, end);
                    continue item;
                } else if (fileIndex == (this.files.size() - 1)) {
                    // no good parts found, abort
                    fc = null;
                    break item;
                }
            }
        }

        Log.print("createGoodCombination: Done");
        return fc;
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();

        sb.append("\t*** Comparison ***\n");

        sb.append(" * Differences:\n");
        for (int i = 0; i < getDifferences(); i++) {
            sb.append("part ").append(getPart(i)).append(": ");
            for (int j = 0; j < getFilesCount(); j++) {
                sb.append("\t").append(getChecksum(i, j)).append(" (").append(getMark(i, j)).append(")");
            }
            sb.append("\t").append(getFile(0).getStartOffset(getPart(i))).append("-").append(getFile(0).getEndOffset(getPart(i))).append("\n");
        }

        sb.append("length:   ");
        for (int i = 0; i < getFilesCount(); i++) {
            sb.append("\t").append(getFile(i).getSourceFileLength()).append(" bytes");
        }

        sb.append("\n * Similarity:").append(similarity.toString());

        return sb.toString();
    }

    /**
     * Set UNDEFINED for all parts in the specified rows.
     *
     * @param start the index of the starting row
     * @param end   the index of the ending row
     * @return true if successful, false if invalid range was given.
     */
    public boolean markRowUndefined(int start, int end) {
        /*
         * Check for valid range. Abort if invalid range is given.
         */
        if (start < 0 || end >= getDifferences() || start > end) {
            Log.print("Comparison.markRowUndefined: Invalid range, aborting.");
            return false;
        }

        for (int row = start; row <= end; row++) {
            for (int col = 0; col < files.size(); col++) {
                if (this.isGoodIndex(row, col)) {
                    setMark(row, col, Mark.UNDEFINED);
                }
            }
        }

        Log.print("Comparison.markRowUndefined: UNDEFINED set.");
        return true;
    }
}