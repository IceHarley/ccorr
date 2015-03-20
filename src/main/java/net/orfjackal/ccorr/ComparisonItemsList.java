// Copyright © 2003-2006, 2010-2015, Esko Luontola <www.orfjackal.net>
// This software is released under the GNU General Public License, version 2 or later.
// The license text is at http://www.gnu.org/licenses/gpl.html

package net.orfjackal.ccorr;

import java.util.ArrayList;
import java.util.List;

public class ComparisonItemsList {
    List<ComparisonItem> items = new ArrayList<ComparisonItem>();

    public void add(ComparisonItem item) {
        items.add(item);
    }

    public ComparisonItem findMark(ComparisonItem sourceItem) {
        return findMark(sourceItem.getPart(), sourceItem.getChecksum());
    }

    public ComparisonItem findMark(int part, String checksum) {
        for (ComparisonItem item : items) {
            if (item.getPart() == part && item.getChecksum().equals(checksum))
                return item;
        }
        return null;
    }
}
