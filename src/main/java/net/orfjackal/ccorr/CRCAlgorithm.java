// Copyright © 2003-2006, 2010-2015, Esko Luontola <www.orfjackal.net>
// This software is released under the GNU General Public License, version 2 or later.
// The license text is at http://www.gnu.org/licenses/gpl.html

package net.orfjackal.ccorr;

import jonelo.jacksum.algorithm.AbstractChecksum;

public class CRCAlgorithm {

    private String name;
    private AbstractChecksum crc;

    public CRCAlgorithm(String name, AbstractChecksum crc) {
        this.name = name;
        this.crc = crc;
    }

    public String getName() {
        return name;
    }

    public void reset() {
        crc.reset();
    }

    public void update(byte[] bytes, int offset, int length) {
        crc.update(bytes, offset, length);
    }

    public String getHexValue() {
        return crc.getHexValue();
    }
}
