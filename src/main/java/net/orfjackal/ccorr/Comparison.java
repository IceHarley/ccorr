// Copyright © 2003-2006, 2010, Esko Luontola <www.orfjackal.net>
// This software is released under the GNU General Public License, version 2 or later.
// The license text is at http://www.gnu.org/licenses/gpl.html

package net.orfjackal.ccorr;

import java.io.*;
import java.util.*;

/**
 * This class is used to represent a CCorr Comparison Project. <code>Comparison</code> is the central class of
 * Corruption Corrector and it is used to compare <code>ChecksumFile</code> objects, mark corrupt/uncorrupt parts and
 * create <code>FileCombination</code> objects. The files to be compared need to be first added to the
 * <code>Comparison</code>, after which they are compared with the <code>doCompare</code> method. <code>doCompare</code>
 * needs to be run after adding or removing files, because otherwise the data is not up to date and most of the methods
 * will refuse to work.
 *
 * @author Esko Luontola
 */
public class Comparison implements Serializable {

    private static final long serialVersionUID = 1L;

    public static final int NEXT_MARK = -1;

    public static final int MARK_IS_UNDEFINED = 0;
    public static final int MARK_IS_GOOD = 1;
    public static final int MARK_IS_BAD = 2;
    public static final int MARK_IS_UNSURE = 3;

    /**
     * The file extension used by CCorr Comparison Project.
     */
    public static final String FILE_EXTENSION = "ccp";

    /**
     * The the file type name of CCorr Comparison Project.
     */
    public static final String FILE_TYPE = "CCorr Comparison Project";

    /**
     * The name of the comparison.
     */
    private String name;

    /**
     * The <code>ChecksumFile</code> objects that are being compared.
     */
    private ChecksumFile[] files;

    /**
     * Used to store the mark information.
     */
    private ComparisonItem[][] items;

    /**
     * Used to store the similarity between the compared files.
     */
    private double[][] similarity;

    /**
     * Indicates in the comparison data needs to be updated.
     *
     * @see #doCompare()
     */
    private boolean needsUpdating;

    /**
     * Comments connected to this <code>Comparison</code>.
     */
    private String comments;

    /**
     * The file to which this <code>Comparison</code> was saved.
     */
    private File savedAsFile;

    /**
     * Creates a new empty <code>Comparison</code>.
     */
    public Comparison() {
        this.name = "";
        this.files = new ChecksumFile[0];
        this.items = new ComparisonItem[0][0];
        this.similarity = new double[0][0];
        this.needsUpdating = false;
        this.comments = "";
    }

    private void writeObject(ObjectOutputStream out) throws IOException {
        // TODO: write as the first object an Integer which tells the version of the file, so that importing old versions would be possible
        out.writeObject(name);
        out.writeObject(files);
        out.writeObject(items);
        out.writeObject(similarity);
        out.writeBoolean(needsUpdating);
        out.writeObject(comments);
        out.writeObject(savedAsFile);
    }

    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
        name = (String) in.readObject();
        files = (ChecksumFile[]) in.readObject();
        items = (ComparisonItem[][]) in.readObject();
        similarity = (double[][]) in.readObject();
        needsUpdating = in.readBoolean();
        comments = (String) in.readObject();
        savedAsFile = (File) in.readObject();
    }

    /**
     * Compares the <code>ChecksumFile</code> objects and updates all the comparison data.
     */
    public void doCompare() {
        Vector<Integer> partsThatDiffer = new Vector<Integer>();
        int[][] numberOfDifferences = new int[files.length][files.length];
        Vector<Integer> filesToBeMirrored = new Vector<Integer>();

        if (files.length > 1) {

            // compare all files to eachother to find similar files
            for (int i = 0; i < (files.length - 1); i++) {
                for (int j = (i + 1); j < files.length; j++) {

                    // one file being shorter does not necessarily make it different
                    int shortest = files[i].getParts();
                    if (files[j].getParts() < shortest) {
                        shortest = files[j].getParts();
                    }

                    for (int part = 0; part < shortest; part++) {
                        if (files[i].getChecksum(part) != null && files[j].getChecksum(part) != null    // takes short files in account
                                && !files[i].getChecksum(part).equals(files[j].getChecksum(part))) {

                            // difference found between files i and j (i is always lower index than j)
                            numberOfDifferences[i][j]++;

                            // update our part list
                            if (!partsThatDiffer.contains(part)) {
                                partsThatDiffer.add((Integer) part);
                            }
                        }
                    }
                }
            }

            // clean up the differing parts information gathered earlier
            int[] partsThatDiffer2 = new int[partsThatDiffer.size()];
            for (int i = 0; i < partsThatDiffer2.length; i++) {
                partsThatDiffer2[i] = partsThatDiffer.get(i);
            }
            Arrays.sort(partsThatDiffer2);

            // store gathered comparison data to new arrays
            ComparisonItem[][] newItems = new ComparisonItem[partsThatDiffer2.length][files.length];
            for (int i = 0; i < newItems.length; i++) {
                for (int j = 0; j < newItems[i].length; j++) {
                    newItems[i][j] = new ComparisonItem(partsThatDiffer2[i], files[j]);
                }
            }

            if (items.length > 0 && newItems.length > 0) {

                // copy old markers to new items
                for (int newFile = 0; newFile < newItems[0].length; newFile++) {
                    for (int oldFile = 0; oldFile < items[0].length; oldFile++) {

                        // find the same old files from the new array
                        if (newItems[0][newFile].getFile() == items[0][oldFile].getFile()) {
                            int jStartIndex = 0;
                            filesToBeMirrored.add(newFile);

                            // proceed with moving markers if the files, parts and checksums are the same
                            for (ComparisonItem[] newItem : newItems) {
                                for (int j = jStartIndex; j < items.length; j++) {

                                    if (newItem[newFile].getFile() == items[j][oldFile].getFile()
                                            && newItem[newFile].getPart() == items[j][oldFile].getPart()
                                            && newItem[newFile].getChecksum().equals(items[j][oldFile].getChecksum())) {
                                        newItem[newFile].setMark(items[j][oldFile].getMark());

                                        // we don't need to go through all the indexes again
                                        jStartIndex = j + 1;
                                        break;
                                    }
                                }
                            }
                        }
                    }
                }
            }
            items = newItems;

            // process and save data for similarity
            similarity = new double[files.length][files.length];
            for (int i = 0; i < numberOfDifferences.length; i++) {          // each file is also compared to itself
                for (int j = i; j < numberOfDifferences[i].length; j++) {
                    int shortest = files[i].getParts();
                    if (files[j].getParts() < shortest) {
                        shortest = files[j].getParts();
                    }
                    double d = 1.0 - ((1.0 * numberOfDifferences[i][j]) / shortest);
                    similarity[i][j] = d;
                    similarity[j][i] = d;
                }
            }

        } else if (files.length == 1) {
            items = new ComparisonItem[0][0];
            similarity = new double[1][1];
            similarity[0][0] = 1.0;
        } else if (files.length == 0) {
            items = new ComparisonItem[0][0];
            similarity = new double[0][0];
        }

        needsUpdating = false;

        // mirror old file's markers in new array (in case files were added to this comparison)
        if (Settings.isMarkMirroringEnabled()) {
            for (int i = 0; i < filesToBeMirrored.size(); i++) {
                int file = filesToBeMirrored.elementAt(i);
                for (int j = 0; j < this.items.length; j++) {
                    this.mirrorMark(j, file);
                }
            }
        }

        if (this.getDifferences() > 100) {
            Log.println("doCompare:\n[over 100 differences, output hidden]");
        } else {
            Log.println("doCompare:\n" + this.toString());
        }
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
            return this.items.length;
        }
    }

    /**
     * Returns the number of <code>ChecksumFile</code>s in this <code>Comparison</code>.
     *
     * @return the number of files
     */
    public int getFiles() {
        return this.files.length;
    }

    /**
     * Returns the part related to the given difference.
     *
     * @param difference the index of the difference
     * @return the index of the part, or -1 if parameter invalid
     */
    public int getPart(int difference) {
        if (difference < 0 || difference >= this.items.length) {
            return -1;
        } else {
            return this.items[difference][0].getPart();
        }
    }

    /**
     * Returns the similarity between two compared files. Similarity is indicated by a double between 0.0 and 1.0; the
     * greater the more similar.
     *
     * @param file1 the index of the first file
     * @param file2 the index of the second file
     * @return similarity between the two files
     */
    public double getSimilarity(int file1, int file2) {
        if (this.needsUpdating
                || file1 < 0
                || file1 >= this.similarity.length
                || file2 < 0
                || file2 >= this.similarity.length) {       // similarity is always a square array
            return -1;
        } else {
            return this.similarity[file1][file2];
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
                || difference >= this.items.length
                || file < 0
                || file >= this.items[difference].length);
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
            return this.items[difference][file];
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
            return this.items[difference][file].getChecksum();
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
                if (this.items[difference][i].getStartOffset() > offset) {
                    offset = this.items[difference][i].getStartOffset();
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
                if (this.items[difference][i].getEndOffset() > offset) {
                    offset = this.items[difference][i].getEndOffset();
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
     * @param mark       the mark, which can be MARK_IS_UNDEFINED, MARK_IS_GOOD, MARK_IS_BAD, MARK_IS_UNSURE, or any
     *                   integer
     * @see #mirrorMark(int, int)
     */
    public void setMark(int difference, int file, int mark) {
        if (this.isGoodIndex(difference, file)) {
            this.items[difference][file].setMark(mark);
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
    public int getMark(int difference, int file) {
        if (this.isGoodIndex(difference, file)) {
            return this.items[difference][file].getMark();
        } else {
            return -1;
        }
    }

    /**
     * Changes to the next mark in the given index.
     *
     * @param difference the index of the difference
     * @param file       the index of the file
     * @return the new mark that was set
     * @see ComparisonItem#nextMark()
     */
    public int nextMark(int difference, int file) {
        if (this.isGoodIndex(difference, file)) {
            int result = this.items[difference][file].nextMark();
            if (Settings.isMarkMirroringEnabled()) {
                this.mirrorMark(difference, file);
            }
            return result;
        } else {
            return -1;
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
            String crc = this.items[difference][file].getChecksum();
            int mark = this.items[difference][file].getMark();
            for (int i = 0; i < this.items[difference].length; i++) {
                if (crc.equals(this.items[difference][i].getChecksum())) {
                    this.items[difference][i].setMark(mark);
                }
            }
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
     * Returns the comments of this <code>Comparison</code>.
     *
     * @return the comments
     */
    public String getComments() {
        return this.comments;
    }

    /**
     * Sets the comments for this <code>Comparison</code>.
     *
     * @param comments the comments, or null to empty them
     */
    public void setComments(String comments) {
        if (comments != null) {
            this.comments = comments;
        } else {
            this.comments = "";
        }
    }

    /**
     * Returns the name of the algorithm that was used for making the checksums.
     *
     * @return the name of the algorithm
     * @see CRCAlgorithmRepository#getSupportedAlgorithms()
     */
    public String getAlgorithm() {
        if (files.length == 0) {
            return null;
        } else {
            return files[0].getAlgorithm();
        }
    }

    /**
     * Returns the part length that was used for making the checksums.
     *
     * @return the length in bytes
     */
    public long getPartLength() {
        if (files.length == 0) {
            return -1;
        } else {
            return files[0].getPartLength();
        }
    }

    /**
     * Returns the <code>ChecksumFile</code> in the given index.
     *
     * @param file the index of the file
     * @return the requested <code>ChecksumFile</code>, or null if parameter invalid
     */
    public ChecksumFile getFile(int file) {
        if (file < 0 || file >= this.files.length) {
            return null;
        } else {
            return this.files[file];
        }
    }

    /**
     * Adds a <code>ChecksumFile</code> the this <code>Comparison</code>. The <code>ChecksumFile</code> given as
     * parameter must have the same part size and algorithm as all the other <code>ChecksumFile</code>s that are part of
     * this <code>Comparison</code>, and the same object is not accepted twise. If the requirements are not met, nothing
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
            for (ChecksumFile f : this.files) {
                if (file == f) {
                    return;
                }
            }

            // resize files array to fit the new file
            ChecksumFile[] newArray = new ChecksumFile[this.files.length + 1];
            System.arraycopy(this.files, 0, newArray, 0, this.files.length);
            newArray[newArray.length - 1] = file;
            this.files = newArray;
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
        if (file >= 0 && file < this.files.length) {
            this.removeFile(this.files[file]);
        }
    }

    /**
     * Removes the given <code>ChecksumFile</code> object from this <code>Comparison</code>. After removing files {@link
     * #doCompare() doCompare} must be run or most of this <code>Comparison</code>'s methods will refuse to work.
     *
     * @param file a reference to the file
     */
    public void removeFile(ChecksumFile file) {

        // find the right index...
        int removeFromIndex = -1;
        for (int i = 0; i < this.files.length; i++) {
            if (file == this.files[i]) {
                removeFromIndex = i;
                break;
            }
        }

        // ...and remove the file
        if (removeFromIndex >= 0) {
            ChecksumFile[] newArray = new ChecksumFile[this.files.length - 1];
            for (int i = removeFromIndex; i < (this.files.length - 1); i++) {
                this.files[i] = this.files[i + 1];
            }
            System.arraycopy(this.files, 0, newArray, 0, newArray.length);
            this.files = newArray;
            this.needsUpdating = true;      // somebody should run doCompare()

            Log.print("Comparison.removeFile: File #" + (removeFromIndex + 1) + " removed.");
        }
    }

    /**
     * Creates a <code>FileCombination</code> from the parts marked as good. If in one difference index there is no item
     * with MARK_IS_GOOD as the marker, a good combination will not be possible.
     *
     * @return a good combination, or null if it is not possible or if updating is needed or if there are no files
     */
    public FileCombination createGoodCombination() {
        Log.print("createGoodCombination: Start");
        if (this.needsUpdating) {
            Log.print("createGoodCombination: Aborted, needsUpdating == true");
            return null;
        }
        if (this.items.length == 0) {
            Log.print("createGoodCombination: Aborted, no parts available");
            return null;
        }


        FileCombination fc = new FileCombination();
        long nextStart = 0;

        item:
        for (int i = 0; i < this.items.length; i++) {
            for (int j = 0; j < this.files.length; j++) {
                if (this.items[i][j].getMark() == MARK_IS_GOOD) {
                    File file = this.items[i][j].getFile().getSourceFile();
                    long start = nextStart;
                    long end;

                    if (i == (this.items.length - 1)) {
                        // last part
                        end = this.items[i][j].getFile().getSourceFileLength();
                    } else {
                        end = this.items[i][j].getEndOffset();
                        nextStart = this.items[i][j].getEndOffset() + 1;
                    }

                    fc.addItem(file, start, end);
                    continue item;
                } else if (j == (this.files.length - 1)) {
                    // no good parts found, abort
                    fc = null;
                    break item;
                }
            }
        }

        Log.print("createGoodCombination: Done");
        return fc;
    }

    /**
     * Returns the number of all <code>FileCombination</code>s that are not marked as bad. If in one difference index
     * there is no item with MARK_IS_GOOD as the marker, marks MARK_IS_UNDEFINED and MARK_IS_UNSURE will be looked for.
     * If in one difference index all items are marked as MARK_IS_BAD, no combinations are possible.
     *
     * @return the number of the combinations, or -1 if updating is needed or if there are no files
     */
    public int getPossibleCombinations() {
        if (this.needsUpdating) {
            Log.print("getPossibleCombinations = -1 (needsUpdating == true)");
            return -1;
        }
        if (this.items.length == 0) {
            Log.print("getPossibleCombinations = -1 (no parts available)");
            return -1;
        }

        int result = 1;

        item:
        for (int item = 0; item < this.items.length; item++) {
            int inThisItem = 0;
            boolean isPossible = false;

            for (int i = 0; i < this.files.length; i++) {
                if (this.items[item][i].getMark() == MARK_IS_GOOD) {
                    continue item;
                } else if (this.items[item][i].getMark() == MARK_IS_UNSURE
                        || this.items[item][i].getMark() == MARK_IS_UNDEFINED) {
                    inThisItem++;
                    isPossible = true;
                }
            }

            if (isPossible) {
                result = result * inThisItem;
            } else {
                Log.print("getPossibleCombinations = 0 (item " + item + " is impossible)");
                return 0;
            }
        }

        Log.print("getPossibleCombinations = " + result);
        return result;
    }

    /**
     * Returns all <code>FileCombination</code>s that are not marked as bad. The number of combinations is that returned
     * by {@link #getPossibleCombinations() getPossibleCombinations}.
     *
     * @return an array containing the combinations, or null if updating is needed or if there are no files
     */
    public FileCombination[] createPossibleCombinations() {
        // TODO: optimize with countChecksums in mind, i.e. read as much as possible from the same file

        Log.print("createPossibleCombinations: Start");
        if (this.needsUpdating) {
            Log.print("createPossibleCombinations: Aborted, needsUpdating == true");
            return null;
        }

        int possibleCombinations = this.getPossibleCombinations();
        if (possibleCombinations < 1) {
            Log.print("createPossibleCombinations: Aborted, no possibilities");
            return new FileCombination[0];
        }

        FileCombination[] fc = new FileCombination[possibleCombinations];
        for (int i = 0; i < fc.length; i++) {
            fc[i] = new FileCombination();
        }
        Vector<Integer> possibleFiles = new Vector<Integer>();
        int goodFile;
        int combinationsDone = 1;
        long start = 0;

        for (int item = 0; item < this.items.length; item++) {
            possibleFiles.removeAllElements();
            goodFile = -1;

            for (int i = 0; i < this.files.length; i++) {
                if (this.items[item][i].getMark() == MARK_IS_GOOD) {
                    goodFile = i;
                } else if (this.items[item][i].getMark() == MARK_IS_UNSURE
                        || this.items[item][i].getMark() == MARK_IS_UNDEFINED) {
                    possibleFiles.add(i);
                }
            }

            // a good file is always the best possibility
            if (goodFile != -1) {
                possibleFiles.removeAllElements();
                possibleFiles.add(goodFile);
            }

            // count all possibilities and add items to FileCombinations
            int successives = (fc.length / combinationsDone) / possibleFiles.size();
            combinationsDone = combinationsDone * possibleFiles.size();
            RepeatCounter counter = new RepeatCounter(possibleFiles, successives);

            for (FileCombination aFc : fc) {
                int inTurn = counter.getNext();
                File file = this.items[item][inTurn].getFile().getSourceFile();

                long end;
                if (item == (this.items.length - 1)) {
                    // last part
                    end = this.items[item][inTurn].getFile().getSourceFileLength();
                } else {
                    end = this.items[item][inTurn].getEndOffset();
                }

                aFc.addItem(file, start, end);
            }
            start = this.items[item][0].getEndOffset() + 1;
        }

        Log.print("createPossibleCombinations: Done");
        return fc;
    }

    /**
     * A class used by {@link Comparison#createPossibleCombinations() createPossibleCombinations}.
     * <code>RepeatCounter</code> rotates goes through a list of numbers and returns each of them as many times as
     * defined in the constructor before switching to the next number.
     *
     * @author Esko Luontola
     */
    private class RepeatCounter {

        /**
         * The numbers in the rotation.
         */
        int[] values;

        /**
         * How many times each number is returned in a row.
         */
        int successives;

        /**
         * How many times the current number has been returned.
         */
        int successivesDone = 0;

        /**
         * The index of the current number.
         */
        int inTurn = 0;

        /**
         * Creates a <code>RepeatCounter</code> from an <code>int</code> array.
         *
         * @param values      the numbers to be rotated
         * @param successives how many times each number is repeated
         */
        public RepeatCounter(int[] values, int successives) {
            if (values == null || values.length == 0 || successives < 1) {
                this.values = new int[1];
                this.values[0] = 0;
                this.successives = 1;
            } else {
                this.values = values;
                this.successives = successives;
            }
        }

        /**
         * Creates a <code>RepeatCounter</code> from a <code>Vector</code> of <code>Integer</code>s.
         *
         * @param integers    a <code>Vector</code> of <code>Integer</code>s containing the numbers to be rotated
         * @param successives how many times each number is repeated
         */
        public RepeatCounter(Vector<Integer> integers, int successives) {
            if (integers == null || integers.size() == 0 || successives < 1) {
                this.values = new int[1];
                this.values[0] = 0;
                this.successives = 1;
            } else {
                this.values = new int[integers.size()];
                for (int i = 0; i < values.length; i++) {
                    try {
                        this.values[i] = integers.elementAt(i);
                    } catch (ClassCastException e) {
                        e.printStackTrace();
                    }
                }
                this.successives = successives;
            }
        }

        /**
         * Returns the number in turn and switches to the next one when needed.
         *
         * @return the number in turn
         */
        public int getNext() {
            int result = this.values[inTurn];
            this.successivesDone++;
            if (this.successivesDone == this.successives) {
                this.successivesDone = 0;
                this.inTurn++;
                if (this.inTurn == this.values.length) {
                    this.inTurn = 0;
                }
            }
            return result;
        }

    }

    /**
     * Returns where this <code>Comparison</code> was previously saved.
     *
     * @return where this was saved, or null if not saved
     * @see #saveToFile(File)
     */
    public File getSavedAsFile() {
        return this.savedAsFile;
    }

    /**
     * Saves this <code>Comparison</code> object into a file.
     *
     * @param file the file in which to save
     * @return true if successful, otherwise false
     * @see #loadFromFile(File)
     */
    public boolean saveToFile(File file) {
        boolean successful = ObjectSaver.saveToFile(file, this);
        if (successful) {
            this.savedAsFile = file;
        }
        return successful;
    }

    /**
     * Loads a previously saved <code>Comparison</code> from a file.
     *
     * @param file the file from which to load
     * @return a new <code>Comparison</code> loaded from the file, or null if operation failed
     * @see #saveToFile(File)
     */
    public static Comparison loadFromFile(File file) {
        Comparison result;
        try {
            result = (Comparison) (ObjectSaver.loadFromFile(file));
            result.savedAsFile = file;
        } catch (Exception e) {
            e.printStackTrace();
            result = null;
        }
        return result;
    }

    /**
     * Returns a text representation of this object.
     *
     * @return a text representation of this object.
     */
    public String toString() {
        StringBuffer sb = new StringBuffer();

        sb.append("\t*** Comparison ***\n");

        sb.append(" * Differences:\n");
        for (int i = 0; i < getDifferences(); i++) {
            sb.append("part " + getPart(i) + ": ");
            for (int j = 0; j < getFiles(); j++) {
                sb.append("\t" + getChecksum(i, j) + " (" + getMark(i, j) + ")");
            }
            sb.append("\t" + getFile(0).getStartOffset(getPart(i)) + "-" + getFile(0).getEndOffset(getPart(i)) + "\n");
        }

        sb.append("length:   ");
        for (int i = 0; i < getFiles(); i++) {
            sb.append("\t" + getFile(i).getSourceFileLength() + " bytes");
        }

        sb.append("\n * Similarity:");
        for (int i = 0; i < getFiles(); i++) {
            sb.append("\nfile " + i + ":");
            for (int j = 0; j < getFiles(); j++) {
                sb.append("\t" + getSimilarity(i, j));
            }
        }

        return sb.toString();
    }

    /**
     * Finds and marks the parts in a row according to the number of occurence.
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
        if (start < 0 || end >= this.items.length || start > end) {
            Log.print("Comparison.markGoodParts: Invalid range, aborting.");
            return false;
        }

        /*
        * ht           a hashtable to store the number of occurrences for each checksum
        * max          the maximum number of occurrences
        * maxIndex     the index of the checksum with the maximum occurrence
        * isUnsure     decides whether MARK_IS_GOOD or MARK_IS_UNSURE should be set
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
            for (int col = 0; col < this.items[row].length; col++) {
                if (this.isGoodIndex(row, col)) {
                    // Skip column if past the end of file
                    if (this.items[row][col].getChecksum().length() == 0) {
                        continue;
                    }

                    // Do not change rows that already have markers set
                    if (this.items[row][col].getMark() != MARK_IS_UNDEFINED) {
                        maxIndex = -1;
                        break;
                    }

                    String crc = this.items[row][col].getChecksum();

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
                    setMark(row, maxIndex, MARK_IS_UNSURE);
                    unsure++;
                } else {
                    setMark(row, maxIndex, MARK_IS_GOOD);
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
     * Set MARK_IS_UNDEFINED for all parts in the specified rows.
     *
     * @param start the index of the starting row
     * @param end   the index of the ending row
     * @return true if successful, false if invalid range was given.
     */
    public boolean markRowUndefined(int start, int end) {
        /*
         * Check for valid range. Abort if invalid range is given.
         */
        if (start < 0 || end >= this.items.length || start > end) {
            Log.print("Comparison.markRowUndefined: Invalid range, aborting.");
            return false;
        }

        for (int row = start; row <= end; row++) {
            for (int col = 0; col < this.items[row].length; col++) {
                if (this.isGoodIndex(row, col)) {
                    setMark(row, col, MARK_IS_UNDEFINED);
                }
            }
        }

        Log.print("Comparison.markRowUndefined: MARK_IS_UNDEFINED set.");
        return true;
    }
}