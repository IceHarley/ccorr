// Copyright © 2003-2006, 2010, Esko Luontola <www.orfjackal.net>
// This software is released under the GNU General Public License, version 2 or later.
// The license text is at http://www.gnu.org/licenses/gpl.html

package net.orfjackal.ccorr;

import org.junit.Assert;
import org.junit.Test;

/**
 * @author Esko Luontola
 */
public class CrcTest extends Assert {

    private static final byte[] DATA = {0, 1, 2, 3, 4, 5, 6, 7, 8, 9};

    private static String calculate(String algorithm, byte[] data) {
        CRC md5 = new CRC(new CRCAlgorithmRepository().getByName(algorithm));
        md5.update(data, 0, data.length);
        return md5.getHexValue();
    }

    @Test
    public void calculates_CRC32() {
        assertEquals("456CD746", calculate("CRC-32", DATA));
    }

    @Test
    public void calculates_MD5() {
        assertEquals("c56bd5480f6e5413cb62a0ad9666613a", calculate("MD5", DATA));
    }

    @Test
    public void calculates_SHA1() {
        assertEquals("494179714a6cd627239dfededf2de9ef994caf03", calculate("SHA-1", DATA));
    }

    @Test
    public void when_data_contains_entirely_the_same_byte_then_the_byte_is_shown_in_hex() {
        assertEquals("0x00", calculate("CRC-32", new byte[]{0, 0, 0, 0, 0, 0}));
        assertEquals("0x20", calculate("CRC-32", new byte[]{' ', ' ', ' ', ' '}));
    }
}
