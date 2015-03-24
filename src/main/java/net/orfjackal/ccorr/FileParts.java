// Copyright © 2003-2006, 2010-2015, Esko Luontola <www.orfjackal.net>
// This software is released under the GNU General Public License, version 2 or later.
// The license text is at http://www.gnu.org/licenses/gpl.html

package net.orfjackal.ccorr;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class FileParts implements Iterable<FilePart> {
    List<FilePart> fileParts;

    public FileParts() {
        fileParts = new ArrayList<FilePart>();
    }

    public void add(FilePart filePart) {
        fileParts.add(filePart);
    }

    public int size() {
        return fileParts.size();
    }

    public FilePart get(int index) {
        return fileParts.get(index);
    }

    @Override
    public Iterator<FilePart> iterator() {
        return fileParts.iterator();
    }
}
