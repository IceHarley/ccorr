// Copyright © 2003-2006, 2010-2015, Esko Luontola <www.orfjackal.net>
// This software is released under the GNU General Public License, version 2 or later.
// The license text is at http://www.gnu.org/licenses/gpl.html

package net.orfjackal.ccorr;

import java.io.*;

public class NumberOfDifferences implements Serializable {
    int[][] numberOfDifferences;

    public NumberOfDifferences(int size) {
        this.numberOfDifferences = new int[size][size];
    }

    private NumberOfDifferences(int[][] numberOfDifferences) {
        this.numberOfDifferences = numberOfDifferences;
    }

    public void add(int index1, int index2) {
        numberOfDifferences[index1][index2]++;
    }

    public int get(int index1, int index2) {
        return numberOfDifferences[index1][index2];
    }

    //Serialization
    private Object writeReplace() {
        return new SerializationProxy(this);
    }

    private void readObject(ObjectInputStream stream) throws InvalidObjectException {
        throw new InvalidObjectException("Proxy required.");
    }

    private static class SerializationProxy implements Serializable {
        private static final long serialVersionUID = 1L;

        private final int[][] numberOfDifferences;

        public SerializationProxy(NumberOfDifferences target) {
            this.numberOfDifferences = target.numberOfDifferences;
        }

        private Object readResolve() {
            return new NumberOfDifferences(numberOfDifferences);
        }
    }
}
