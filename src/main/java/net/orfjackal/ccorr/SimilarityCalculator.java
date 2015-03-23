// Copyright © 2003-2006, 2010-2015, Esko Luontola <www.orfjackal.net>
// This software is released under the GNU General Public License, version 2 or later.
// The license text is at http://www.gnu.org/licenses/gpl.html

package net.orfjackal.ccorr;

class SimilarityCalculator {
    private ChecksumFiles files;
    private NumberOfDifferences numberOfDifferences;

    public SimilarityCalculator(ChecksumFiles files, NumberOfDifferences numberOfDifferences) {
        this.files = files;
        this.numberOfDifferences = numberOfDifferences;
    }

    public Similarity calculate() {
        Similarity similarity = new Similarity(files.size());
        for (int i = 0; i < files.size(); i++) {
            for (int j = i; j < files.size(); j++) {
                int shortest = files.findShorterFileParts(i, j);
                double d = 1.0 - ((1.0 * numberOfDifferences.get(i, j)) / shortest);
                similarity.set(i, j, d);
            }
        }
        return similarity;
    }
}
