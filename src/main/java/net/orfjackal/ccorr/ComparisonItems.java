// Copyright © 2003-2006, 2010-2015, Esko Luontola <www.orfjackal.net>
// This software is released under the GNU General Public License, version 2 or later.
// The license text is at http://www.gnu.org/licenses/gpl.html

package net.orfjackal.ccorr;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

public class ComparisonItems implements Serializable {
    private ComparisonItem[][] items;
    private int size1;
    private int size2;

    public ComparisonItems(int size1, int size2) {
        items = new ComparisonItem[size1][size2];
        this.size1 = size1;
        this.size2 = size2;
    }

    public ComparisonItem get(int index1, int index2) {
        return items[index1][index2];
    }

    private void writeObject(ObjectOutputStream out) throws IOException {
        out.writeObject(items);
    }

    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
        items = (ComparisonItem[][]) in.readObject();
        size1 = items.length;
        size2 = 0;
        if (size1 > 0)
            size2 = items[0].length;
    }

    public int size1() {
        return size1;
    }

    public int size2() {
        return size2;
    }

    public void set(int index1, int index2, ComparisonItem value) {
        items[index1][index2] = value;
    }

    public Mark findMark(Integer part, String checksum) {
        Mark result = Mark.UNDEFINED;
        for (int i = 0; i < size1; i++) {
            for (int j = 0; j < size2; j++) {
                if (items[i][j].getPart() == part && items[i][j].getChecksum() == checksum)
                    if (items[i][j].getMark() != Mark.UNDEFINED)
                        result = items[i][j].getMark();
            }
        }
        return result;
    }
}
