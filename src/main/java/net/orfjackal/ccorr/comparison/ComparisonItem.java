// Copyright © 2003-2006, 2010, Esko Luontola <www.orfjackal.net>
// This software is released under the GNU General Public License, version 2 or later.
// The license text is at http://www.gnu.org/licenses/gpl.html

package net.orfjackal.ccorr.comparison;

import java.io.Serializable;

public class ComparisonItem implements Serializable {
    private String checksum;
    private final int part;
    private Mark mark;

    public ComparisonItem(String checksum, int part) {
        this.checksum = checksum;
        this.part = part;
        this.mark = Mark.UNDEFINED;
        if (checksum == null || checksum.length() == 0) {
            this.mark = Mark.BAD;
            this.checksum = "";
        }
    }

    public void setMark(Mark mark) {
        if (getChecksum().length() != 0) {
            if (mark == Mark.NEXT) {
                setMark(Mark.nextMark(getMark()));
            } else {
                this.mark = mark;
            }
        }
    }

    public Mark getMark() {
        return this.mark;
    }

    public int getPart() {
        return this.part;
    }

    public String getChecksum() {
        return checksum;
    }
}