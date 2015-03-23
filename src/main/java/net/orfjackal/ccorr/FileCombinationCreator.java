// Copyright © 2003-2006, 2010-2015, Esko Luontola <www.orfjackal.net>
// This software is released under the GNU General Public License, version 2 or later.
// The license text is at http://www.gnu.org/licenses/gpl.html

package net.orfjackal.ccorr;

import java.io.File;

class FileCombinationCreator {
    private Comparison comparison;

    public FileCombinationCreator(Comparison comparison) {
        this.comparison = comparison;
    }

    public FileCombination create() {
        FileCombination fc = getFileCombination();
        Log.print("createGoodCombination: Done");
        return fc;
    }

    private FileCombination getFileCombination() {
        FileCombination fc = new FileCombination();

        long nextStart = 0;
        int differences = comparison.getDifferences();
        item:
        for (int difference = 0; difference < differences; difference++) {
            int filesCount = comparison.getFilesCount();
            for (int fileIndex = 0; fileIndex < filesCount; fileIndex++) {
                if (comparison.getMark(difference, fileIndex) == Mark.GOOD) {
                    ChecksumFile checksumFile = comparison.getFile(fileIndex);
                    File file = checksumFile.getSourceFile();
                    boolean isLastDifference = isLastIndex(differences, difference);
                    long start = nextStart;
                    long end = findEndPosition(difference, isLastDifference, checksumFile);
                    nextStart = findStartPosition(nextStart, isLastDifference, end);

                    fc.addItem(file, start, end);
                    continue item;
                }
                if (isLastIndex(filesCount, fileIndex)) {
                    // no good parts found, abort
                    return null;
                }
            }
        }
        return fc;
    }

    private long findStartPosition(long nextStart, boolean isLastDifference, long end) {
        if (!isLastDifference) {

            nextStart = end + 1;
        }
        return nextStart;
    }

    private long findEndPosition(int difference, boolean isLastDifference, ChecksumFile checksumFile) {
        long end;
        if (isLastDifference) {
            end = checksumFile.getSourceFileLength();
        } else {
            end = checksumFile.getEndOffset(difference);
        }
        return end;
    }

    private boolean isLastIndex(int differences, int difference) {
        return difference == (differences - 1);
    }
}
