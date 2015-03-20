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

    private static final long serialVersionUID = 1L;

    private String checksum;

    /**
     * The index of the part that this <code>ComparisonItem</code> is related to.
     */
    private int part;

    /**
     * The mark of this <code>ComparisonItem</code>.
     */
    private int mark;

    /**
     * A caption for this <code>ComparisonItem</code> that can be showed in place of the checksum.
     */
    private String caption;

    public ComparisonItem(String checksum, int part) {
        this.checksum = checksum;
        this.part = part;
        this.mark = Comparison.MARK_IS_UNDEFINED;
        this.caption = null;
        if (checksum == null || checksum.length() == 0) {
            this.mark = Comparison.MARK_IS_BAD;
            this.checksum = "";
        }
    }

    private void writeObject(ObjectOutputStream out) throws IOException {
        // TODO: write as the first object an Integer which tells the version of the file, so that importing old versions would be possible
        out.writeObject(checksum);
        out.writeInt(part);
        out.writeInt(mark);
        out.writeObject(caption);
    }

    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
        checksum = (String) in.readObject();
        part = in.readInt();
        mark = in.readInt();
        caption = (String) in.readObject();
    }

    /**
     * Sets the mark.
     *
     * @param mark the new mark
     */
    public void setMark(int mark) {
        if (this.getChecksum().length() != 0) {     // if the file is long enough
            if (mark == Comparison.NEXT_MARK) {
                nextMark();
            } else {
                this.mark = mark;
            }
        }
    }

    /**
     * Returns the mark.
     *
     * @return the current mark
     */
    public int getMark() {
        return this.mark;
    }

    /**
     * Changes to the next mark. The order is {@link Comparison#MARK_IS_UNDEFINED MARK_IS_UNDEFINED}, {@link
     * Comparison#MARK_IS_GOOD MARK_IS_GOOD}, {@link Comparison#MARK_IS_BAD MARK_IS_BAD}, {@link
     * Comparison#MARK_IS_UNSURE MARK_IS_UNSURE}, {@link Comparison#MARK_IS_UNDEFINED MARK_IS_UNDEFINED}.
     *
     * @return the new mark that was set
     */
    public int nextMark() {
        switch (this.mark) {
            case Comparison.MARK_IS_UNDEFINED:
                this.mark = Comparison.MARK_IS_GOOD;
                break;

            case Comparison.MARK_IS_GOOD:
                this.mark = Comparison.MARK_IS_BAD;
                break;

            case Comparison.MARK_IS_BAD:
                this.mark = Comparison.MARK_IS_UNSURE;
                break;

            case Comparison.MARK_IS_UNSURE:
            default:
                this.mark = Comparison.MARK_IS_UNDEFINED;
                break;
        }
        return this.mark;
    }

    /**
     * Returns the related part.
     *
     * @return index of the part
     */
    public int getPart() {
        return this.part;
    }

    /**
     * Returns the offset of the related part's first byte.
     *
     * @return index of the part's first byte, or -1 if the part does not exist
     * @see ChecksumFile#getStartOffset(int)
     */
    /*public long getStartOffset() {
        return this.file.getStartOffset(this.part);
    }*/

    /**
     * Returns the offset of the related part's last byte.
     *
     * @return index of the part's last byte, or -1 if the part does not exist
     * @see ChecksumFile#getEndOffset(int)
     */
    /*public long getEndOffset() {
        return this.file.getEndOffset(this.part);
    }*/

    public String getChecksum() {
        return checksum;
    }

    /**
     * Returns the caption.
     *
     * @return the caption, or the checksum if the caption does not exist
     */
    public String getCaption() {
        String result;
        if (this.caption == null) {
            result = this.getChecksum();
        } else {
            result = this.caption;
        }
        return result;
    }
}
