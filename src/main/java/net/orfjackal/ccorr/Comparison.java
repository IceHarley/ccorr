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

    private static final long serialVersionUID = 1L;

    private String name;
    private ChecksumFiles files;
    private ComparisonItems items;
    private ComparisonItemsList list;
    Similarity similarity;
    private boolean needsUpdating;
    private List<Integer> partsThatDiffer;
    private NumberOfDifferences numberOfDifferences;

    public Comparison() {
        this.name = "";
        this.files = new ChecksumFiles();
        items = new ComparisonItems(0, 0);
        this.similarity = new Similarity(0);
        this.needsUpdating = false;
    }

    private void writeObject(ObjectOutputStream out) throws IOException {
        // TODO: write as the first object an Integer which tells the version of the file, so that importing old versions would be possible
        out.writeObject(name);
        out.writeObject(files);
        out.writeObject(items);
        out.writeObject(similarity);
        out.writeBoolean(needsUpdating);
    }

    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
        name = (String) in.readObject();
        files = (ChecksumFiles) in.readObject();
        items = (ComparisonItems) in.readObject();
        similarity = (Similarity) in.readObject();
        needsUpdating = in.readBoolean();
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
        ComparisonItems newItems = storeComparisonDataToNewItems();
        storeComparisonDataToList();

        List<Integer> filesToBeMirrored = findFilesToBeMirrored(newItems);
        items = newItems;
        calculateSimilarity();

        needsUpdating = false;
        mirrorMarkers(filesToBeMirrored);
    }

    private void compareEachFilesPair() {
        for (int i = 0; i < (files.size() - 1); i++) {
            for (int j = (i + 1); j < files.size(); j++) {
                compareFiles(numberOfDifferences, i, j);
            }
        }
    }

    private void compareFiles(NumberOfDifferences numberOfDifferences, int file1, int file2) {
        int shortest = files.findShorterFileParts(file1, file2);

        for (int part = 0; part < shortest; part++) {
            if (ChecksumFile.partPresentInFile(files.get(file1), part) && ChecksumFile.partPresentInFile(files.get(file2), part)    // takes short files in account
                    ) {

                if (!files.arePartsEquals(file1, file2, part)) {
                    // difference found between files file1 and file2 (file1 is always lower index than file2)
                    numberOfDifferences.add(file1, file2);

                    // update our part list
                    if (!partsThatDiffer.contains(part)) {
                        partsThatDiffer.add(part);
                    }
                }
            }
        }
    }

    private ComparisonItems storeComparisonDataToNewItems() {
        ComparisonItems newItems = new ComparisonItems(partsThatDiffer.size(), files.size());
        for (int p = 0; p < newItems.partsCount(); p++) {
            for (int f = 0; f < newItems.filesCount(); f++) {
                Integer part = partsThatDiffer.get(p);
                String checksum = files.get(f).getChecksum(part);
                Mark mark = items.findMark(part, checksum);
                ComparisonItem item = new ComparisonItem(checksum, part);
                item.setMark(mark);
                newItems.set(p, f, item);
            }
        }
        return newItems;
    }

    private void storeComparisonDataToList() {
        list = new ComparisonItemsList();
        for (int p = 0; p < partsThatDiffer.size(); p++) {
            for (int f = 0; f < files.size(); f++) {
                Integer part = partsThatDiffer.get(p);
                String checksum = files.get(f).getChecksum(part);
                if (list.findMark(part, checksum) == null) {
                    ComparisonItem item = new ComparisonItem(checksum, part);
                    list.add(item);
                }
            }
        }
    }

    private List<Integer> findFilesToBeMirrored(ComparisonItems newItems) {
        List<Integer> filesToBeMirrored = new ArrayList<Integer>();
        if (items.partsCount() > 0 && newItems.partsCount() > 0) {
            for (int newFile = 0; newFile < newItems.filesCount(); newFile++) {
                for (int oldFile = 0; oldFile < items.filesCount(); oldFile++) {
                    if (files.get(newFile) == files.get(oldFile)) {
                        filesToBeMirrored.add(newFile);
                    }
                }
            }
        }
        return filesToBeMirrored;
    }

    private void mirrorMarkers(List<Integer> filesToBeMirrored) {
        if (Settings.isMarkMirroringEnabled()) {
            for (Integer file : filesToBeMirrored) {
                for (int j = 0; j < items.partsCount(); j++) {
                    this.mirrorMark(j, file);
                }
            }
        }
    }

    /**
     * Mirrors the mark in the given index. All items that have the same checksum and part as the given index, will get
     * the same mark.
     *
     * @param difference the index of the difference
     * @param file       the index of the file
     * @see Settings#setMarkMirroringEnabled(boolean)
     * @see Settings#isMarkMirroringEnabled()
     */
    public void mirrorMark(int difference, int file) {
        if (this.isGoodIndex(difference, file)) {
            String crc = items.get(difference, file).getChecksum();
            Mark mark = items.get(difference, file).getMark();
            for (int i = 0; i < items.filesCount(); i++) {
                if (crc.equals(items.get(difference, i).getChecksum())) {
                    items.get(difference, i).setMark(mark);
                }
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
        items = new ComparisonItems(0, 0);
        list = new ComparisonItemsList();
        similarity = new Similarity(0);
        needsUpdating = false;
    }

    private void compareSingleFile() {
        items = new ComparisonItems(0, 0);
        list = new ComparisonItemsList();
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
            return items.partsCount();
        }
    }

    /**
     * Returns the number of <code>ChecksumFile</code>s in this <code>Comparison</code>.
     *
     * @return the number of files
     */
    public int getFiles() {
        return this.files.size();
    }

    /**
     * Returns the part related to the given difference.
     *
     * @param difference the index of the difference
     * @return the index of the part, or -1 if parameter invalid
     */
    public int getPart(int difference) {
        if (difference < 0 || difference >= items.partsCount()) {
            return -1;
        } else {
            return items.get(difference, 0).getPart();
        }
    }

    /**
     * Used to check that the given index is not out of bounds and that this <code>Comparison</code> does not need
     * updating.
     *
     * @param difference the index of the difference
     * @param file       the index of the file
     */
    private boolean isGoodIndex(int difference, int file) {
        return !(this.needsUpdating
                || difference < 0
                || difference >= items.partsCount()
                || file < 0
                || file >= items.filesCount());
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
            return items.get(difference, file);
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
        if (this.isGoodIndex(difference, file)) {
            return items.get(difference, file).getChecksum();
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
            for (int i = 0; i < this.getFiles(); i++) {
                if (files.get(i).getStartOffset(items.get(difference, i).getPart()) > offset) {
                    offset = files.get(i).getStartOffset(items.get(difference, i).getPart());
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
            for (int i = 0; i < this.getFiles(); i++) {
                if (files.get(i).getEndOffset(items.get(difference, i).getPart()) > offset) {
                    offset = files.get(i).getEndOffset(items.get(difference, i).getPart());
                }
            }
            return offset;
        } else {
            return -1;
        }
    }

    /**
     * Sets the mark in the given index. If {@link Settings#isMarkMirroringEnabled() isMarkMirroringEnabled} returns
     * true, the mark is mirrored.
     *
     * @param difference the index of the difference
     * @param file       the index of the file
     * @param mark       the mark, which can be UNDEFINED, GOOD, BAD, UNSURE, or any
     *                   integer
     * @see #mirrorMark(int, int)
     */
    public void setMark(int difference, int file, Mark mark) {
        if (this.isGoodIndex(difference, file)) {
            items.get(difference, file).setMark(mark);
            if (Settings.isMarkMirroringEnabled()) {
                this.mirrorMark(difference, file);
            }
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
        if (this.isGoodIndex(difference, file)) {
            return items.get(difference, file).getMark();
        } else {
            return Mark.NOT_EXISTS;
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
        if (this.isGoodIndex(difference, file)) {
            Mark result = Mark.nextMark(items.get(difference, file).getMark());
            items.get(difference, file).setMark(result);
            if (Settings.isMarkMirroringEnabled()) {
                this.mirrorMark(difference, file);
            }
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
        if (items.partsCount() == 0) {
            Log.print("createGoodCombination: Aborted, no parts available");
            return null;
        }


        FileCombination fc = new FileCombination();
        long nextStart = 0;

        item:
        for (int i = 0; i < items.partsCount(); i++) {
            for (int j = 0; j < this.files.size(); j++) {
                if (items.get(i, j).getMark() == Mark.GOOD) {
                    ChecksumFile checksumFile = files.get(j);
                    File file = checksumFile.getSourceFile();
                    long start = nextStart;
                    long end;

                    if (i == (items.partsCount() - 1)) {
                        // last part
                        end = checksumFile.getSourceFileLength();
                    } else {
                        end = checksumFile.getEndOffset(i);
                        nextStart = end + 1;
                    }

                    fc.addItem(file, start, end);
                    continue item;
                } else if (j == (this.files.size() - 1)) {
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
            for (int j = 0; j < getFiles(); j++) {
                sb.append("\t").append(getChecksum(i, j)).append(" (").append(getMark(i, j)).append(")");
            }
            sb.append("\t").append(getFile(0).getStartOffset(getPart(i))).append("-").append(getFile(0).getEndOffset(getPart(i))).append("\n");
        }

        sb.append("length:   ");
        for (int i = 0; i < getFiles(); i++) {
            sb.append("\t").append(getFile(i).getSourceFileLength()).append(" bytes");
        }

        sb.append("\n * Similarity:").append(similarity.toString());

        return sb.toString();
    }

    /**
     * Finds and marks the parts in a row according to the number of occurrence.
     *
     * @param start the index of the starting row
     * @param end   the index of the ending row
     * @return true if successful, false if invalid range was given
     */
    public boolean markGoodParts(int start, int end) {
        /*
         * good, unsure and undefined count the number of parts (increased once per row)
         */
        int good = 0;
        int unsure = 0;
        int undefined = 0;

        /*
        * Check for valid range. Abort if invalid range is given.
        */
        if (start < 0 || end >= items.partsCount() || start > end) {
            Log.print("Comparison.markGoodParts: Invalid range, aborting.");
            return false;
        }

        /*
        * ht           a hashtable to store the number of occurrences for each checksum
        * max          the maximum number of occurrences
        * maxIndex     the index of the checksum with the maximum occurrence
        * isUnsure     decides whether GOOD or UNSURE should be set
        */
        for (int row = start; row <= end; row++) {
            Hashtable<String, Integer> ht = new Hashtable<String, Integer>();
            int max = 1;
            int maxIndex = -1;
            boolean isUnsure = false;

            /*
             * Increase the counter for existing checksums or
             * create a new entry in the hashtable.
             */
            for (int col = 0; col < items.filesCount(); col++) {
                if (this.isGoodIndex(row, col)) {
                    // Skip column if past the end of file
                    if (items.get(row, col).getChecksum().length() == 0) {
                        continue;
                    }

                    // Do not change rows that already have markers set
                    if (items.get(row, col).getMark() != Mark.UNDEFINED) {
                        maxIndex = -1;
                        break;
                    }

                    String crc = items.get(row, col).getChecksum();

                    if (ht.containsKey(crc)) {
                        Integer counter = ht.get(crc);
                        ht.remove(crc);
                        ht.put(crc, counter + 1);

                        /*
                        * Remember index of checksum with maximum count
                        */
                        if (counter + 1 > max) {
                            max = counter + 1;
                            maxIndex = col;
                            isUnsure = false;
                        } else {
                            if (counter + 1 == max) {
                                isUnsure = true;
                            }
                        }
                    } else {
                        ht.put(crc, 1);
                    }
                }
            }

            /*
            * Now mark all checksums with the maximum count in the row.
            */
            if (maxIndex >= 0) {
                if (isUnsure) {
                    setMark(row, maxIndex, Mark.UNSURE);
                    unsure++;
                } else {
                    setMark(row, maxIndex, Mark.GOOD);
                    good++;
                }
                mirrorMark(row, maxIndex);
            } else {
                undefined++;
            }
        }

        Log.print("Comparison.markGoodParts: " + good + " good, "
                + unsure + " unsure and " + undefined + " row(s) unchanged.");
        return true;
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
        if (start < 0 || end >= items.partsCount() || start > end) {
            Log.print("Comparison.markRowUndefined: Invalid range, aborting.");
            return false;
        }

        for (int row = start; row <= end; row++) {
            for (int col = 0; col < items.filesCount(); col++) {
                if (this.isGoodIndex(row, col)) {
                    setMark(row, col, Mark.UNDEFINED);
                }
            }
        }

        Log.print("Comparison.markRowUndefined: UNDEFINED set.");
        return true;
    }
}