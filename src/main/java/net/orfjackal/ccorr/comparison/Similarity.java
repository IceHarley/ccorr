// Copyright © 2003-2006, 2010-2015, Esko Luontola <www.orfjackal.net>
// This software is released under the GNU General Public License, version 2 or later.
// The license text is at http://www.gnu.org/licenses/gpl.html

package net.orfjackal.ccorr.comparison;

import java.io.Serializable;

public class Similarity implements Serializable {
    private double[][] similarity;

    public Similarity(int size) {
        similarity = new double[size][size];
    }

    public void set(int index1, int index2, double value) {
        similarity[index1][index2] = value;
        similarity[index2][index1] = value;
    }

    public double get(int index1, int index2) {
        return similarity[index1][index2];
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < similarity.length; i++) {
            sb.append(String.format("\nfile %d:", i));
            for (int j = 0; j < similarity[i].length; j++)
                sb.append(String.format("\t%f", similarity[i][j]));
        }
        return sb.toString();
    }
}