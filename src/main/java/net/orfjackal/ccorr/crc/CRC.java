// Copyright © 2003-2006, 2010, Esko Luontola <www.orfjackal.net>
// This software is released under the GNU General Public License, version 2 or later.
// The license text is at http://www.gnu.org/licenses/gpl.html

package net.orfjackal.ccorr.crc;

public class CRC {
    private final CRCAlgorithm algorithm;
    private int firstByte = Byte.MIN_VALUE - 1;
    private boolean allSameAsFirstByte = true;

    public CRC(CRCAlgorithm algorithm) {
        this.algorithm = algorithm;
    }

    public void update(byte[] bytes, int offset, int length) {
        algorithm.update(bytes, offset, length);

        if (firstByte < Byte.MIN_VALUE) {
            firstByte = bytes[offset];
            allSameAsFirstByte = true;
        }
        if (allSameAsFirstByte) {
            for (int i = offset; i < length; i++) {
                if (bytes[i] != firstByte) {
                    allSameAsFirstByte = false;
                    break;
                }
            }
        }
    }

    public String getHexValue() {
        if (!allSameAsFirstByte)
            return algorithm.getHexValue();
        String hex = Integer.toHexString(firstByte & 0xFF).toUpperCase();
        if (hex.length() < 2)
            hex = "0" + hex;
        return "0x" + hex;
    }

    public String getAlgorithm() {
        return this.algorithm.getName();
    }

    public String calculateChecksum(Buffer buffer) {
        reset();
        update(buffer.getBytes(), 0, buffer.getLength());
        return getHexValue();
    }

    private void reset() {
        algorithm.reset();
        firstByte = Byte.MIN_VALUE - 1;
        allSameAsFirstByte = true;
    }
}