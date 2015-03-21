// Copyright © 2003-2006, 2010, Esko Luontola <www.orfjackal.net>
// This software is released under the GNU General Public License, version 2 or later.
// The license text is at http://www.gnu.org/licenses/gpl.html

package net.orfjackal.ccorr;

import java.io.*;

/**
 * This class is used to represent a CCorr Checksum File. A new <code>ChecksumFile</code> is created with the
 * <code>createChecksumFile</code> method. This program counts checksums from the given file at regular intervals and
 * stores the information in this class. The part checksums and other data are used in a <code>Comparison</code> to find
 * the differences between any files.
 *
 * @author Esko Luontola
 */
public class ChecksumFileFactory  {

    public static final ChecksumFile EMPTY_DATA = new ChecksumFile(null, "", -1, null, -1);

    public static ChecksumFile createChecksumFile(File file, long partLength, String algorithm) {
        return generateChecksums(file, partLength, algorithm);
    }

    private static ChecksumFile generateChecksums(File sourceFile, long partLength, String algorithm) {
        Log.print("generateChecksums(" + partLength + ", " + algorithm + ") from " + sourceFile);

        FileDivider fileDivider = new FileDivider(sourceFile, partLength);
        FileChecksumsCalculator checksumsCalculator = new MonitoredChecksumsCalculator(fileDivider, new CRC(CRCAlgorithmFactory.getByName(algorithm)));
        ChecksumFile checksumFile = checksumsCalculator.calculateChecksums();

        boolean successful = !EMPTY_DATA.equals(checksumFile);

        if (successful) {
            Log.println("generateChecksums: Done");
        }
        else {
            Log.println("generateChecksums: Failed");
        }

        return checksumFile;
    }
}