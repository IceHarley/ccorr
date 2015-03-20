// Copyright © 2003-2006, 2010-2015, Esko Luontola <www.orfjackal.net>
// This software is released under the GNU General Public License, version 2 or later.
// The license value is at http://www.gnu.org/licenses/gpl.html

package net.orfjackal.ccorr;

import jonelo.jacksum.algorithm.Crc32;
import jonelo.jacksum.algorithm.MD;

import java.util.Arrays;

public class CRCAlgorithmFactory {
    private enum Names {
        CRC_32("CRC-32"),
        MD5("MD5"),
        SHA_1("SHA-1");

        private final String value;

        private Names(final String value) {
            this.value = value;
        }

        public String value() {
            return value;
        }

        public static Names get(String value) {
            for (Names name : values())
                if (name.value().equals(value))
                    return name;
            return CRC_32;
        }
    }

    public static CRCAlgorithm getByName(String name) {
        Names key = Names.get(name);
        switch (key) {
            case MD5:
                return new CRCAlgorithm(key.value(), new MD(Names.MD5.value()));
            case SHA_1:
                return new CRCAlgorithm(key.value(), new MD(Names.SHA_1.value()));
            case CRC_32:
            default:
                return new CRCAlgorithm(key.value(), new Crc32());
        }
    }

    /**
     * Returns an array containing the names of the algorithms known by this class.
     *
     * @return the algorithm names in an array
     */
    public static String[] getSupportedAlgorithms() {
        String[] result = new String[]{Names.CRC_32.value(), Names.MD5.value(), Names.SHA_1.value()};
        Arrays.sort(result);
        return result;
    }
}
