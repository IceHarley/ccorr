// Copyright © 2003-2006, 2010, Esko Luontola <www.orfjackal.net>
// This software is released under the GNU General Public License, version 2 or later.
// The license text is at http://www.gnu.org/licenses/gpl.html

package net.orfjackal.ccorr.comparison;

import net.orfjackal.ccorr.checksum.*;
import net.orfjackal.ccorr.combination.*;

import java.io.Serializable;
import java.util.*;
import java.util.logging.*;

public class Comparison implements Serializable {
    private final static Logger logger = Logger.getLogger(Comparison.class.getName());

    private String name;
    private final ChecksumFiles files;
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
        partsThatDiffer = new ArrayList<Integer>();
        if (files.size() > 1)
            compareMultipleFiles();
        else if (files.size() == 1)
            compareSingleFile();
        else if (files.size() == 0)
            compareNoFile();
        logComparison();
    }

    private void compareMultipleFiles() {
        numberOfDifferences = new NumberOfDifferences(files.size());
        compareEachFilesPair();
        storeComparisonDataToList();
        similarity = new SimilarityCalculator(files, numberOfDifferences).calculate();
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
            if (files.isPartPresentInBothFiles(part, file1, file2))
                if (!files.arePartsEquals(part, file1, file2))
                    addDifference(numberOfDifferences, file1, file2, part);
    }

    private void addDifference(NumberOfDifferences numberOfDifferences, int file1, int file2, int part) {
        numberOfDifferences.add(file1, file2);
        if (!partsThatDiffer.contains(part))
            partsThatDiffer.add(part);
    }

    private void storeComparisonDataToList() {
        for (Integer part : partsThatDiffer)
            storePartComparisonData(part);
    }

    private void storePartComparisonData(Integer part) {
        for (int f = 0; f < files.size(); f++) {
            String checksum = files.get(f).getChecksum(part);
            addComparisonItem(part, checksum);
        }
    }

    private void addComparisonItem(Integer part, String checksum) {
        if (items.find(part, checksum) == null) {
            ComparisonItem item = new ComparisonItem(checksum, part);
            items.add(item);
        }
    }

    private void compareSingleFile() {
        items = new ComparisonItems();
        similarity = new Similarity(1);
        similarity.set(0, 0, 1.0);
        needsUpdating = false;
    }

    private void compareNoFile() {
        items = new ComparisonItems();
        similarity = new Similarity(0);
        needsUpdating = false;
    }

    private void logComparison() {
        logger.info(getDifferences() > 100 ? "doCompare:\n[over 100 differences, output hidden]" : "doCompare:\n" + this);
    }

    public int getDifferences() {
        if (needsUpdating)
            return -1;
        return partsThatDiffer.size();
    }

    public int getFilesCount() {
        return this.files.size();
    }

    public int getPart(int difference) {
        if (!isValidDifferenceIndex(difference))
            return -1;
        return partsThatDiffer.get(difference);
    }

    private boolean isValidDifferenceIndex(int difference) {
        return difference >= 0 && difference < partsThatDiffer.size();
    }

    public ComparisonItem getItem(int difference, int file) {
        if (!this.isGoodIndex(difference, file)) {
            return null;
        }
        int part = partsThatDiffer.get(difference);
        return items.find(part, files.get(file).getChecksum(part));
    }

    public boolean isGoodIndex(int difference, int file) {
        return !this.needsUpdating
                && isValidDifferenceIndex(difference)
                && files.isValidFileIndex(file);
    }

    public String getChecksum(int difference, int file) {
        ComparisonItem item = getItem(difference, file);
        if (item == null) return "";
        return item.getChecksum();
    }

    public long getStartOffset(int difference) {
        if (!this.isGoodIndex(difference, 0))
            throw new IllegalArgumentException();
        long offset = -1;
        for (int i = 0; i < files.size(); i++) {
            ComparisonItem item = getItem(difference, i);
            if (item != null && files.get(i).getStartOffsetOfPart(item.getPart()) > offset)
                offset = files.get(i).getStartOffsetOfPart(item.getPart());
        }
        return offset;
    }

    public long getEndOffset(int difference) {
        if (!this.isGoodIndex(difference, 0))
            throw new IllegalArgumentException();
        long offset = -1;
        for (int i = 0; i < files.size(); i++) {
            ComparisonItem item = getItem(difference, i);
            if (item != null && files.get(i).getEndOffset(item.getPart()) > offset)
                offset = files.get(i).getEndOffset(item.getPart());
        }
        return offset;
    }

    public void setMark(int difference, int file, Mark mark) {
        if (!this.isGoodIndex(difference, file)) return;
        ComparisonItem item = getItem(difference, file);
        if (item != null)
            item.setMark(mark);
    }

    public Mark getMark(int difference, int file) {
        ComparisonItem item = getItem(difference, file);
        if (item != null)
            return item.getMark();
        return Mark.BAD;
    }

    public Mark nextMark(int difference, int file) {
        ComparisonItem item = getItem(difference, file);
        if (item == null)
            return Mark.NOT_EXISTS;
        Mark result = Mark.nextMark(item.getMark());
        item.setMark(result);
        return result;
    }

    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
        logger.log(Level.INFO, "Comparison.setName: Set name to \"{0}\".", name);
    }

    public void addFile(ChecksumFile file) {
        if (files.add(file))
            this.needsUpdating = true;
    }

    public void removeFile(int index) {
        files.remove(index);
        logger.info("Comparison.removeFile: File #" + (index + 1) + " removed.");
        this.needsUpdating = true;
    }

    public static GoodCombination createGoodCombination(Comparison comparison) {
        logger.info("createGoodCombination: Start");
        if (comparison.needsUpdating) {
            logger.info("createGoodCombination: Aborted, needsUpdating == true");
            return GoodCombination.NOT_EXISTS;
        }
        if (comparison.getDifferences() == 0) {
            logger.info("createGoodCombination: Aborted, no parts available");
            return GoodCombination.NOT_EXISTS;
        }
        return new GoodCombinationExtractor(comparison).extract();
    }

    public boolean markRowsUndefined(int start, int end) {
        if (!validateRange(start, end)) {
            logger.info("Comparison.markRowsUndefined: Invalid range, aborting.");
            return false;
        }
        for (int row = start; row <= end; row++)
            for (int col = 0; col < files.size(); col++)
                markRowUndefined(row, col);

        logger.info("Comparison.markRowsUndefined: UNDEFINED set.");
        return true;
    }

    private boolean validateRange(int start, int end) {
        return start >= 0 && end < getDifferences() && start <= end;
    }

    private void markRowUndefined(int row, int col) {
        if (this.isGoodIndex(row, col))
            setMark(row, col, Mark.UNDEFINED);
    }

    public ChecksumFile getFile(int index) {
        return files.get(index);
    }

    public PartedFile getPartedFile(int index) {
        return files.get(index);
    }

    public String getAlgorithm() {
        return files.getAlgorithm();
    }

    public long getPartLength() {
        return files.getPartLength();
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();

        sb.append("\t*** Comparison ***\n");
        sb.append(" * Differences:\n");
        for (int i = 0; i < getDifferences(); i++)
            sb.append(differenceToString(i));

        sb.append("length:\t");
        for (int i = 0; i < files.size(); i++)
            sb.append(String.format("\t%d bytes", files.get(i).getSourceFileLength()));

        sb.append(String.format("\n * Similarity: %s", similarity));
        return sb.toString();
    }

    private String differenceToString(int index) {
        int part = getPart(index);
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("part %d: ", part));
        String offsets = "";
        for (int j = 0; j < files.size(); j++) {
            sb.append(String.format("\t%s (%s) ", getChecksum(index, j), getMark(index, j)));
            if (files.get(j).hasPart(part))
                offsets = String.format("\t%d - %d\n", files.get(j).getStartOffsetOfPart(part), files.get(j).getEndOffset(part));
        }
        sb.append(offsets);
        return sb.toString();
    }
}