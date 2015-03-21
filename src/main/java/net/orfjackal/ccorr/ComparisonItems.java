// Copyright © 2003-2006, 2010-2015, Esko Luontola <www.orfjackal.net>
// This software is released under the GNU General Public License, version 2 or later.
// The license text is at http://www.gnu.org/licenses/gpl.html

package net.orfjackal.ccorr;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ComparisonItems implements Serializable {
    List<ComparisonItem> items = new ArrayList<ComparisonItem>();

    public void add(ComparisonItem item) {
        items.add(item);
    }

    public ComparisonItem find(ComparisonItem sourceItem) {
        return find(sourceItem.getPart(), sourceItem.getChecksum());
    }

    public ComparisonItem find(int part, String checksum) {
        for (ComparisonItem item : items) {
            if (item.getPart() == part && item.getChecksum().equals(checksum))
                return item;
        }
        return null;
    }

    private void writeObject(ObjectOutputStream out) throws IOException {
        out.writeObject(items.toArray(new ComparisonItem[items.size()]));
    }

    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
        items = new ArrayList<ComparisonItem>(Arrays.asList((ComparisonItem[]) in.readObject()));
    }
}
