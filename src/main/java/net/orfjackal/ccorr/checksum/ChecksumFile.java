// Copyright © 2003-2006, 2010-2015, Esko Luontola <www.orfjackal.net>
// This software is released under the GNU General Public License, version 2 or later.
// The license text is at http://www.gnu.org/licenses/gpl.html

package net.orfjackal.ccorr.checksum;

import net.orfjackal.ccorr.combination.PartedFile;

import java.io.*;

public class ChecksumFile implements PartedFile, Serializable {
    private final Checksums checksums;
    private final String usedAlgorithm;
    private final long partLength;
    private final File sourceFile;
    private final long sourceFileLength;

    public ChecksumFile(Checksums checksums, String usedAlgorithm, long partLength, File sourceFile, long sourceFileLength) {
        this.checksums = checksums;
        this.usedAlgorithm = usedAlgorithm;
        this.partLength = partLength;
        this.sourceFile = sourceFile;
        this.sourceFileLength = sourceFileLength;
    }

    boolean partPresentInFile(int part) {
        return this.getChecksum(part) != null;
    }

    public int getParts() {
        return checksums.size();
    }

    public String getChecksum(int part) {
        return checksums.get(part);
    }

    public boolean hasPart(int part) {
        return checksums.isValidIndex(part);
    }

    @Override
    public long getStartOffsetOfPart(int part) {
        validatePartOfPart(part);
        return partLength * part;
    }

    private void validatePartOfPart(int part) {
        if (!checksums.isValidIndex(part))
            throw new IndexOutOfBoundsException();
    }

    @Override
    public long getEndOffset(int part) {
        validatePartOfPart(part);
        long offset = (this.partLength * (part + 1)) - 1;
        if (offset >= this.sourceFileLength) {
            offset = this.sourceFileLength - 1;
        }
        return offset;
    }

    public ChecksumFile setSourceFile(File file) {
        ChecksumFile newChecksumFile = this;
        if (isValidFile(file))
            newChecksumFile = new ChecksumFile(checksums, usedAlgorithm, partLength, file, sourceFileLength);
        return newChecksumFile;
    }

    private boolean isValidFile(File file) {
        return file != null && file.exists() && file.length() == this.sourceFileLength;
    }

    @Override
    public File getSourceFile() {
        return sourceFile;
    }

    public long getSourceFileLength() {
        return sourceFileLength;
    }

    public long getPartLength() {
        return partLength;
    }

    public String getAlgorithm() {
        return usedAlgorithm;
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < this.getParts(); i++)
            sb.append(String.format("%d: %s\t start: %d\t end: %d\n", i, getChecksum(i), getStartOffsetOfPart(i), getEndOffset(i)));
        sb.append(String.format("\n%s (%d bytes) %d parts (%d bytes) using %s\n", sourceFile, sourceFileLength, getParts(), partLength, usedAlgorithm));
        return sb.toString();
    }
}
