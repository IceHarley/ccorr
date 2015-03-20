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
    private int partsCount;
    private int filesCount;

    public ComparisonItems(int partsCount, int filesCount) {
        items = new ComparisonItem[partsCount][filesCount];
        this.partsCount = partsCount;
        this.filesCount = filesCount;
    }

    public ComparisonItem get(int part, int file) {
        return items[part][file];
    }

    private void writeObject(ObjectOutputStream out) throws IOException {
        out.writeObject(items);
    }

    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
        items = (ComparisonItem[][]) in.readObject();
        partsCount = items.length;
        filesCount = 0;
        if (partsCount > 0)
            filesCount = items[0].length;
    }

    public int partsCount() {
        return partsCount;
    }

    public int filesCount() {
        return filesCount;
    }

    public void set(int index1, int index2, ComparisonItem value) {
        items[index1][index2] = value;
    }

    public Mark findMark(Integer part, String checksum) {
        Mark result = Mark.UNDEFINED;
        for (int p = 0; p < partsCount; p++) {
            for (int f = 0; f < filesCount; f++) {
                if (items[p][f].getPart() == part && items[p][f].getChecksum() == checksum)
                    if (items[p][f].getMark() != Mark.UNDEFINED)
                        result = items[p][f].getMark();
            }
        }
        return result;
    }
}
