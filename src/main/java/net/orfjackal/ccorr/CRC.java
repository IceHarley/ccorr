// Copyright © 2003-2006, 2010, Esko Luontola <www.orfjackal.net>
// This software is released under the GNU General Public License, version 2 or later.
// The license text is at http://www.gnu.org/licenses/gpl.html

package net.orfjackal.ccorr;

import jonelo.jacksum.algorithm.*;

/**
 * A class that can be used to compute the checksum of a data stream with many different algorithms.
 *
 * @author Esko Luontola
 */
public class CRC {

    /**
     * Name of the algorithm used.
     */
    private CRCAlgorithm algorithm;

    /**
     * The first byte of the data
     */
    private int firstByte = Byte.MIN_VALUE - 1;

    /**
     * Binary OR for all bytes of the data
     */
    private boolean allSameAsFirstByte = true;

    /**
     * Creates a new <code>CRC</code> that uses the given algorithm. If the algorithm name is not recognized, CRC-32
     * will be used.
     *
     * @param algorithm Name of the algorithm as defined in <code>getSupportedAlgorithms</code>.
     * @see CRCAlgorithmRepository#getSupportedAlgorithms()
     */
    public CRC(CRCAlgorithm algorithm) {
        this.algorithm = algorithm;
    }

    /**
     * Resets the checksum to initial value.
     */
    public void reset() {
        algorithm.reset();
        firstByte = Byte.MIN_VALUE - 1;
        allSameAsFirstByte = true;
    }

    /**
     * Updates the checksum with specified array of bytes.
     *
     * @param bytes  the byte array to update the checksum with
     * @param offset the start offset of the data
     * @param length the number of bytes to use for the update
     */
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

    /**
     * Returns the checksum value in HEX format.
     *
     * @return the current checksum value or "0x??" if all bytes are it
     */
    public String getHexValue() {
        if (!allSameAsFirstByte) {
            return algorithm.getHexValue();
        } else {
            String hex = Integer.toHexString(firstByte & 0xFF).toUpperCase();
            if (hex.length() < 2) {
                hex = "0" + hex;
            }
            return "0x" + hex;
        }
    }

    /**
     * Returns the name of the algorithm used by this <code>CRC</code> object.
     *
     * @return the name of the algorithm.
     */
    public String getAlgorithm() {
        return this.algorithm.getName();
    }

    String calculateChecksum(Buffer buffer) {
        reset();
        update(buffer.getBytes(), 0, buffer.getLength());
        return getHexValue();
    }
}