// Copyright © 2003-2006, 2010, Esko Luontola <www.orfjackal.net>
// This software is released under the GNU General Public License, version 2 or later.
// The license text is at http://www.gnu.org/licenses/gpl.html

package net.orfjackal.ccorr;

import jonelo.jacksum.algorithm.*;

import java.util.Arrays;

/**
 * A class that can be used to compute the checksum of a data stream with many different algorithms.
 *
 * @author Esko Luontola
 */
public class CRC {

    /**
     * All algorithms known by this class.
     */
    private static final String[] ALGORITHM_NAMES = {
            "CRC-32",
            "MD5",
            "SHA-1"
// no need to have this many algorithms...
//        "Adler32",
//        "BSD sum",
//        "POSIX cksum",
//        "CRC-16",
//        "Unix System V"
    };

    /**
     * The checksum this <code>CRC</code> object uses.
     */
    private AbstractChecksum crc;

    /**
     * Name of the algorithm used.
     */
    private String algorithmName;

    /**
     * The first byte of the data
     */
    private int firstByte = Byte.MIN_VALUE - 1;

    /**
     * Binary OR for all bytes of the data
     */
    private boolean allSameAsFirstByte = true;

    /**
     * Creates a new <code>CRC</code> that uses the given algorithm. If the algorithmn name is not recognized, CRC-32
     * will be used.
     *
     * @param algorithm Name of the algorithmn as defined in <code>getSupportedAlgorithms</code>.
     * @see #getSupportedAlgorithms()
     */
    public CRC(String algorithm) {
        if (algorithm == null) {
            algorithm = "CRC-32";
        }

        if (algorithm.equals("CRC-32")) {
            this.crc = new Crc32();
        } else if (algorithm.equals("MD5")) {
            this.crc = new MD("MD5");
        } else if (algorithm.equals("SHA-1")) {
            this.crc = new MD("SHA-1");
        } else if (algorithm.equals("Adler32")) {
            this.crc = new Adler32();
        } else if (algorithm.equals("BSD sum")) {
            this.crc = new Sum();
        } else if (algorithm.equals("POSIX cksum")) {
            this.crc = new Cksum();
        } else if (algorithm.equals("CRC-16")) {    // CRC-16 is for some reason very slow
            this.crc = new Crc16();
        } else if (algorithm.equals("Unix System V")) {
            this.crc = new SumSysV();
        } else {
            this.crc = new Crc32();
            algorithm = "CRC-32";
        }

        this.algorithmName = algorithm;
    }

    /**
     * Resets the checksum to initial value.
     */
    public void reset() {
        crc.reset();
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
        crc.update(bytes, offset, length);

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
            return crc.getHexValue();
        } else {
            String hex = Integer.toHexString(firstByte & 0xFF).toUpperCase();
            if (hex.length() < 2) {
                hex = "0" + hex;
            }
            return "0x" + hex;
        }
    }

    /**
     * Returns an array containing the names of the algorithms known by this class.
     *
     * @return the algorithm names in an array
     * @see #CRC(String)
     */
    public static String[] getSupportedAlgorithms() {
        String[] result = new String[ALGORITHM_NAMES.length];
        System.arraycopy(ALGORITHM_NAMES, 0, result, 0, ALGORITHM_NAMES.length);
        Arrays.sort(result);
        return result;
    }

    /**
     * Returns the name of the algorithmn used by this <code>CRC</code> object.
     *
     * @return the name of the algorithm.
     */
    public String getAlgorithm() {
        return this.algorithmName;
    }

    String calculateChecksum(Buffer buffer) {
        reset();
        update(buffer.getBytes(), 0, buffer.getLength());
        return getHexValue();
    }
}