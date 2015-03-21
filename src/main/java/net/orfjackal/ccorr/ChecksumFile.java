// Copyright © 2003-2006, 2010-2015, Esko Luontola <www.orfjackal.net>
// This software is released under the GNU General Public License, version 2 or later.
// The license text is at http://www.gnu.org/licenses/gpl.html

package net.orfjackal.ccorr;

import java.io.*;

public class ChecksumFile implements Serializable {

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

    boolean partPresentInFile(int part) {
        return this.getChecksum(part) != null;
    }

    public int getParts() {
        return checksums.size();
    }

    public String getChecksum(int part) {
        return checksums.get(part);
    }

    public long getStartOffset(int part) {
        return !checksums.isValidIndex(part) ? -1 : this.partLength * part;
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
        StringBuilder sb = new StringBuilder();

        for (int i = 0; i < this.getParts(); i++)
            sb.append(i).append(": ").
                append(this.getChecksum(i)).
                append("\t start: ").append(this.getStartOffset(i)).
                append("\t end: ").append(this.getEndOffset(i)).
                append("\n");
        sb.append("\n").
                append(this.getSourceFile()).
                append(" (").append(this.getSourceFileLength()).append(" bytes) \n").
                append(this.getParts()).
                append(" parts (").append(this.getPartLength()).append(" bytes) ").
                append("using ").append(this.getAlgorithm());

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
