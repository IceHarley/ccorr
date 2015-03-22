// Copyright © 2003-2006, 2010, Esko Luontola <www.orfjackal.net>
// This software is released under the GNU General Public License, version 2 or later.
// The license text is at http://www.gnu.org/licenses/gpl.html

package net.orfjackal.ccorr;

import java.io.*;

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

    private ComparisonItem(String checksum, int part, Mark mark) {
        this.checksum = checksum;
        this.part = part;
        this.mark = mark;
    }

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

    //Serialization
    private Object writeReplace() {
        return new SerializationProxy(this);
    }

    private void readObject(ObjectInputStream stream) throws InvalidObjectException {
        throw new InvalidObjectException("Proxy required.");
    }

    private static class SerializationProxy implements Serializable {
        private static final long serialVersionUID = 1L;

        private final String checksum;
        private final int part;
        private final Mark mark;

        public SerializationProxy(ComparisonItem target) {
            this.checksum = target.checksum;
            this.part = target.part;
            this.mark = target.mark;
        }

        private Object readResolve() {
            return new ComparisonItem(checksum, part, mark);
        }
    }
}