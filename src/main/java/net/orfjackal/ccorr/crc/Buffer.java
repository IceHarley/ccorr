// Copyright © 2003-2006, 2010-2015, Esko Luontola <www.orfjackal.net>
// This software is released under the GNU General Public License, version 2 or later.
// The license text is at http://www.gnu.org/licenses/gpl.html

package net.orfjackal.ccorr.crc;

public class Buffer {
    public final static Buffer EOF = new Buffer(new byte[0], 0);

    private byte[] buffer;
    private int length;

    public Buffer(byte[] buffer, int length) {
        this.buffer = buffer;
        this.length = length;
    }

    public byte[] getBytes() {
        return buffer;
    }

    public int getLength() {
        return length;
    }
}
