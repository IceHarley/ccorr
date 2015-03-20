// Copyright © 2003-2006, 2010-2015, Esko Luontola <www.orfjackal.net>
// This software is released under the GNU General Public License, version 2 or later.
// The license text is at http://www.gnu.org/licenses/gpl.html

package net.orfjackal.ccorr;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

public class NumberOfDifferences implements Serializable {
    int[][] numberOfDifferences;

    public NumberOfDifferences(int size) {
        this.numberOfDifferences = new int[size][size];
    }

    public void add(int index1, int index2) {
        numberOfDifferences[index1][index2]++;
    }

    public int get(int index1, int index2) {
        return numberOfDifferences[index1][index2];
    }

    private void writeObject(ObjectOutputStream out) throws IOException {
        out.writeObject(numberOfDifferences);
    }

    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
        numberOfDifferences = (int[][]) in.readObject();
    }
}
