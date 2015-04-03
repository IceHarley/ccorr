// Copyright © 2003-2006, 2010-2015, Esko Luontola <www.orfjackal.net>
// This software is released under the GNU General Public License, version 2 or later.
// The license text is at http://www.gnu.org/licenses/gpl.html

package net.orfjackal.ccorr.combination;

import net.orfjackal.ccorr.comparison.*;

import java.io.*;
import java.util.logging.Logger;

public class GoodCombinationExtractor {
    private final static Logger logger = Logger.getLogger(GoodCombinationExtractor.class.getName());

    private final Comparison comparison;
    private int differences;
    private GoodCombination gc;
    private long nextStart;

    public GoodCombinationExtractor(Comparison comparison) {
        this.comparison = comparison;
    }

    public static GoodCombination createGoodCombination(Comparison comparison) {
        logger.info("createGoodCombination: Start");
        if (comparison.getDifferences() <= 0) {
            logger.info("createGoodCombination: Aborted, no parts available");
            return GoodCombination.NOT_EXISTS;
        }
        return new GoodCombinationExtractor(comparison).extract();
    }

    public GoodCombination extract() {
        GoodCombination goodCombination = extractGoodCombination();
        logger.info("createGoodCombination: Done");
        return goodCombination;
    }

    private GoodCombination extractGoodCombination() {
        gc = new GoodCombination();
        nextStart = 0;
        differences = comparison.getDifferences();
        for (int difference = 0; difference < differences; difference++)
            if (!addGoodPart(difference)) {
                closeStreams();
                return GoodCombination.NOT_EXISTS;
            }
        return gc;
    }

    private boolean addGoodPart(int difference) {
        int filesCount = comparison.getFilesCount();
        for (int fileIndex = 0; fileIndex < filesCount; fileIndex++)
            if (addFilePartIfGood(difference, fileIndex))
                return true;
        return false;
    }

    private boolean addFilePartIfGood(int difference, int fileIndex) {
        if (comparison.getMark(difference, fileIndex) == Mark.GOOD) {
            addGoodFilePart(difference, fileIndex);
            return true;
        }
        return false;
    }

    private void addGoodFilePart(int difference, int fileIndex) {
        PartedFile partedFile = comparison.getPartedFile(fileIndex);
        boolean isLastDifference = isLastIndex(differences, difference);
        long start = nextStart;
        long end = findEndPosition(difference, isLastDifference, partedFile);
        nextStart = findStartPosition(nextStart, isLastDifference, end);
        if (!gc.containsStream(fileIndex)) {
            InputStream stream = getFileStream(fileIndex);
            gc.addStream(fileIndex, stream);
        }
        gc.add(new GoodCombinationPart(fileIndex, start, end));
    }

    private boolean isLastIndex(int differences, int difference) {
        return difference == (differences - 1);
    }

    private long findEndPosition(int difference, boolean isLastDifference, PartedFile partedFile) {
        return isLastDifference ? partedFile.getSourceFileLength() : partedFile.getEndOffset(difference);
    }

    private long findStartPosition(long nextStart, boolean isLastDifference, long end) {
        if (!isLastDifference)
            nextStart = end + 1;
        return nextStart;
    }

    private void closeStreams() {
        try {
            gc.closeStreams();
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }

    private InputStream getFileStream(int fileIndex) {
        PartedFile partedFile = comparison.getPartedFile(fileIndex);
        return StreamFactory.openInputStream(partedFile.getSourceFile());
    }
}
