// Copyright © 2003-2006, 2010, Esko Luontola <www.orfjackal.net>
// This software is released under the GNU General Public License, version 2 or later.
// The license text is at http://www.gnu.org/licenses/gpl.html

package net.orfjackal.ccorr.checksum;

import net.orfjackal.ccorr.crc.*;

import java.io.File;
import java.util.logging.*;

public class ChecksumFileFactory {
    private final static Logger logger = Logger.getLogger(ChecksumFileFactory.class.getName());

    public static final ChecksumFile EMPTY_DATA = new ChecksumFile(null, "", -1, null, -1);

    public static ChecksumFile createChecksumFile(File file, long partLength, String algorithm) {
        return generateChecksums(file, partLength, algorithm);
    }

    private static ChecksumFile generateChecksums(File sourceFile, long partLength, String algorithm) {
        logger.log(Level.INFO, "generateChecksums({0}, {1}) from {2}", new Object[]{partLength, algorithm, sourceFile});

        FileDivider fileDivider = new FileDivider(sourceFile, partLength);
        FileChecksumsCalculator checksumsCalculator = new MonitoredChecksumsCalculator(fileDivider, new CRC(CRCAlgorithmFactory.getByName(algorithm)));
        ChecksumFile checksumFile = checksumsCalculator.calculateChecksums();

        logger.info(!EMPTY_DATA.equals(checksumFile) ? "generateChecksums: Done" : "generateChecksums: Failed");
        return checksumFile;
    }
}