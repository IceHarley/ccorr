// Copyright © 2003-2006, 2010, Esko Luontola <www.orfjackal.net>
// This software is released under the GNU General Public License, version 2 or later.
// The license text is at http://www.gnu.org/licenses/gpl.html

package net.orfjackal.ccorr;

import java.io.Serializable;

/**
 * A class used to represent a differing part in a <code>Comparison</code>. <code>ComparisonItem</code> stores the
 * marker set to it, which is used to indicate whether the related part is corrupt or not.
 *
 * @author Esko Luontola
 */
public class ComparisonItem implements Serializable {
    private String checksum;
    private int part;
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
        if (this.getChecksum().length() != 0) {
            if (mark == Mark.NEXT) {
                this.setMark(Mark.nextMark(this.getMark()));
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