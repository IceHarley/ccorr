// Copyright © 2003-2006, 2010-2015, Esko Luontola <www.orfjackal.net>
// This software is released under the GNU General Public License, version 2 or later.
// The license text is at http://www.gnu.org/licenses/gpl.html

package net.orfjackal.ccorr;

import java.io.*;

public class ChecksumFile implements Serializable {

    public static final ChecksumFile EMPTY_DATA = new ChecksumFile(null, "", -1, null, -1);
    private static final long serialVersionUID = 1L;

    private Checksums checksums;
    private String usedAlgorithm;
    private long partLength;
    private File sourceFile;
    private long sourceFileLength;

    public ChecksumFile(Checksums checksums, String usedAlgorithm, long partLength, File sourceFile, long sourceFileLength) {
        this.checksums = checksums;
        this.usedAlgorithm = usedAlgorithm;
        this.partLength = partLength;
        this.sourceFile = sourceFile;
        this.sourceFileLength = sourceFileLength;
    }

    static boolean partPresentInFile(ChecksumFile file, int part) {
        return file.getChecksum(part) != null;
    }

    public int getParts() {
        return checksums.size();
    }

    public String getChecksum(int part) {
//        if (!checksums.isValidIndex(part)) {
//            throw new IndexOutOfBoundsException();
//        }
        return checksums.get(part);
    }

    public long getStartOffset(int part) {
        if (!checksums.isValidIndex(part)) {
            return -1;
        } else {
            return this.partLength * part;
        }
    }

    public long getEndOffset(int part) {
        if (!checksums.isValidIndex(part)) {
            return -1;
        } else {
            long offset = (this.partLength * (part + 1)) - 1;
            if (offset >= this.sourceFileLength) {
                offset = this.sourceFileLength - 1;
            }
            return offset;
        }
    }

    public ChecksumFile setSourceFile(File file) {
        ChecksumFile newChecksumFile = this;
        if (file != null && file.exists() && file.length() == this.sourceFileLength) {
            newChecksumFile = new ChecksumFile(checksums, usedAlgorithm, partLength, file, sourceFileLength);
        }
        return newChecksumFile;
    }

    public File getSourceFile() {
        return this.sourceFile;
    }

    public long getSourceFileLength() {
        return this.sourceFileLength;
    }

    public long getPartLength() {
        return this.partLength;
    }

    public String getAlgorithm() {
        return this.usedAlgorithm;
    }

    public String toString() {
        StringBuffer sb = new StringBuffer();

        for (int i = 0; i < this.getParts(); i++)
            sb.append(i + ": " + this.getChecksum(i)
                    + "\t start: " + this.getStartOffset(i)
                    + "\t end: " + this.getEndOffset(i) + "\n");
        sb.append("\n" + this.getSourceFile() + " ("
                + this.getSourceFileLength() + " bytes) \n"
                + this.getParts() + " parts ("
                + this.getPartLength() + " bytes) "
                + "using " + this.getAlgorithm());

        return sb.toString();
    }

    private void writeObject(ObjectOutputStream out) throws IOException {
        // TODO: write as the first object an Integer which tells the version of the file, so that importing old versions would be possible
        out.writeObject(checksums);
        out.writeObject(usedAlgorithm);
        out.writeLong(partLength);
        out.writeObject(sourceFile);
        out.writeLong(sourceFileLength);
    }

    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
        checksums = (Checksums) in.readObject();
        usedAlgorithm = (String) in.readObject();
        partLength = in.readLong();
        sourceFile = (File) in.readObject();
        sourceFileLength = in.readLong();
    }
}
