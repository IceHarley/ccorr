// Copyright © 2003-2006, 2010-2015, Esko Luontola <www.orfjackal.net>
// This software is released under the GNU General Public License, version 2 or later.
// The license text is at http://www.gnu.org/licenses/gpl.html

package net.orfjackal.ccorr;

import java.io.*;
import java.util.List;

public class Checksums implements Serializable {
    private List<String> checksums;

    public Checksums(List<String> checksums) {
        this.checksums = checksums;
    }

    public int size() {
        return checksums.size();
    }

    public boolean isValidIndex(int index) {
        return index >= 0 && index < size();
    }

    public String get(int index) {
        return isValidIndex(index) ? checksums.get(index) : null;
    }
}
