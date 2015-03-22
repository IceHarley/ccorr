// Copyright © 2003-2006, 2010-2015, Esko Luontola <www.orfjackal.net>
// This software is released under the GNU General Public License, version 2 or later.
// The license text is at http://www.gnu.org/licenses/gpl.html

package net.orfjackal.ccorr;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class ComparisonItems implements Serializable {
    List<ComparisonItem> items = new ArrayList<ComparisonItem>();

    public ComparisonItems() {
    }

    private ComparisonItems(List<ComparisonItem> items) {
        this.items = items;
    }

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

    //Serialization
    private Object writeReplace() {
        return new SerializationProxy(this);
    }

    private void readObject(ObjectInputStream stream) throws InvalidObjectException {
        throw new InvalidObjectException("Proxy required.");
    }

    private static class SerializationProxy implements Serializable {
        private static final long serialVersionUID = 1L;

        private List<ComparisonItem> items = new ArrayList<ComparisonItem>();

        public SerializationProxy(ComparisonItems target) {
            this.items = target.items;
        }

        private Object readResolve() {
            return new ComparisonItems(items);
        }
    }
}
