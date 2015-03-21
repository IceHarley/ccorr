// Copyright © 2003-2006, 2010-2015, Esko Luontola <www.orfjackal.net>
// This software is released under the GNU General Public License, version 2 or later.
// The license text is at http://www.gnu.org/licenses/gpl.html

package net.orfjackal.ccorr;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class FileChecksumsCalculator {

    private FileDivider fileDivider;
    private CRC crc;

    public FileChecksumsCalculator(FileDivider fileDivider, CRC crc) {
        this.fileDivider = fileDivider;
        this.crc = crc;
    }

    public ChecksumFile calculateChecksums() {
        try {
            Checksums checksums = fillChecksums();
            long partLength = fileDivider.getPartLength();
            File sourceFile = fileDivider.getSourceFile();
            long length = sourceFile.length();
            return new ChecksumFile(checksums, crc.getAlgorithm(), partLength, sourceFile, length);
        }
        catch (IOException e) {
            e.printStackTrace();
            return ChecksumFileFactory.EMPTY_DATA;
        }
    }

    protected int getPartsCount() {
        return fileDivider.getParts();
    }

    private Checksums fillChecksums() throws IOException {
        List<String> checksums = new ArrayList<String>();
        try {
            fileDivider.divide();
            Buffer buffer = fileDivider.nextPart();
            while (!Buffer.EOF.equals(buffer)) {
                checksums.add(calculateChecksum(buffer));
                buffer = fileDivider.nextPart();
            }
        }
        finally {
            fileDivider.close();
        }
        return new Checksums(checksums);
    }

    protected String calculateChecksum(Buffer buffer) {
        return crc.calculateChecksum(buffer);
    }
}
