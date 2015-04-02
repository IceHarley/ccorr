// Copyright © 2003-2006, 2010-2015, Esko Luontola <www.orfjackal.net>
// This software is released under the GNU General Public License, version 2 or later.
// The license text is at http://www.gnu.org/licenses/gpl.html

package net.orfjackal.ccorr.comparison;

import net.orfjackal.ccorr.checksum.ChecksumFiles;

public class SimilarityCalculator {
    private final ChecksumFiles files;
    private final NumberOfDifferences numberOfDifferences;

    public SimilarityCalculator(ChecksumFiles files, NumberOfDifferences numberOfDifferences) {
        this.files = files;
        this.numberOfDifferences = numberOfDifferences;
    }

    public Similarity calculate() {
        Similarity similarity = new Similarity(files.size());
        for (int i = 0; i < files.size(); i++)
            for (int j = i; j < files.size(); j++)
                similarity.set(i, j, calculate(i, j));
        return similarity;
    }

    private double calculate(int file1, int file2) {
        int shortest = files.findShorterFileParts(file1, file2);
        return 1.0 - ((1.0 * numberOfDifferences.get(file1, file2)) / shortest);
    }
}
