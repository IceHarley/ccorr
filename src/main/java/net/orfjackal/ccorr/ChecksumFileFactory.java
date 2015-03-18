// Copyright © 2003-2006, 2010, Esko Luontola <www.orfjackal.net>
// This software is released under the GNU General Public License, version 2 or later.
// The license text is at http://www.gnu.org/licenses/gpl.html

package net.orfjackal.ccorr;

import javax.swing.*;
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

    /**
     * Creates a new <code>ChecksumFile</code> object from the given file. The operation can take some minutes depending
     * on the size of the file to be processed. A ProgressMonitor is used if available.
     *
     * @param file       the file to process.
     * @param partLength the part length used to make checksums; if less than <code>MIN_PART_SIZE</code> or greater than
     *                   <code>MAX_PART_SIZE</code>, the smallest or biggest allowed value will be used
     * @param algorithm  the name of the  algorithm to be used for making the checksums
     * @return a new <code>ChecksumFile</code> object if operation is successful, otherwise null
     * @see CRC#getSupportedAlgorithms()
     * @see Settings#setProgressMonitor(ProgressMonitor)
     */
    public static ChecksumFile createChecksumFile(File file, long partLength, String algorithm) {
        return generateChecksums(file, partLength, algorithm);
    }

    /**
     * Rebuilds the checksums from the source file using the given settings. This operation can take some minutes
     * depending on the size of the file to be processed. A ProgressMonitor is used if available
     *
     * @param sourceFile       the file to process.
     * @param partLength the part length used to make checksums; if less than <code>MIN_PART_SIZE</code> or greater than
     *                   <code>MAX_PART_SIZE</code>, the smallest or biggest allowed value will be used
     * @param algorithm  the name of the  algorithm to be used for making the checksums
     * @return true if operation is successful, otherwise false.
     * @see #createChecksumFile(java.io.File, long, String)
     * @see CRC#getSupportedAlgorithms()
     * @see Settings#setProgressMonitor(ProgressMonitor)
     */
    private static ChecksumFile generateChecksums(File sourceFile, long partLength, String algorithm) {
        Log.print("generateChecksums(" + partLength + ", " + algorithm + ") from " + sourceFile);

        FileDivider fileDivider = new FileDivider(sourceFile, partLength);
        FileChecksumsCalculator checksumsCalculator = new MonitoredChecksumsCalculator(fileDivider, new CRC(algorithm));
        ChecksumFile checksumFile = checksumsCalculator.calculateChecksums();

        boolean successful = !ChecksumFile.EMPTY_DATA.equals(checksumFile);

        if (successful) {
            Log.println("generateChecksums: Done");
        }
        else {
            Log.println("generateChecksums: Failed");
        }

        return checksumFile;
    }
}