// Copyright © 2003-2006, 2010-2015, Esko Luontola <www.orfjackal.net>
// This software is released under the GNU General Public License, version 2 or later.
// The license text is at http://www.gnu.org/licenses/gpl.html

package net.orfjackal.ccorr.checksum;

import net.orfjackal.ccorr.crc.Buffer;
import net.orfjackal.ccorr.settings.Settings;

import java.io.*;

public class FileDivider {
    private File sourceFile;
    private long partLength;
    private long sourceFileLength;
    private int parts;
    private int currentPart;
    BufferedInputStream input;

    public FileDivider(File sourceFile, long partLength) {
        this.sourceFile = sourceFile;
        this.partLength = constrainPartLength(partLength);
        this.sourceFileLength = this.sourceFile.length();
    }

    private long constrainPartLength(long partLength) {
        if (partLength < Settings.MIN_PART_SIZE) {
            partLength = Settings.MIN_PART_SIZE;
        } else if (partLength > Settings.MAX_PART_SIZE) {
            partLength = Settings.MAX_PART_SIZE;
        }
        return partLength;
    }

    public void divide() throws FileNotFoundException {
        if (!this.sourceFile.exists() || !this.sourceFile.canRead())
            throw new FileNotFoundException();
        parts = calculateNumberOfParts();
        input = new BufferedInputStream(
                        new FileInputStream(this.sourceFile),
                        Settings.getReadBufferLength()
                );
        currentPart = 0;
    }

    private int calculateNumberOfParts() {
        int parts = (int) (this.sourceFileLength / this.partLength);
        if ((this.sourceFileLength % this.partLength) != 0)
            parts++;
        return parts;
    }

    public Buffer nextPart() throws IOException {
        if (currentPart == parts)
            return Buffer.EOF;
        currentPart++;
        byte[] buffer = new byte[(int) this.partLength];
        int len = input.read(buffer);
        return new Buffer(buffer, len);
    }

    public int getParts() {
        return parts;
    }

    public long getPartLength() {
        return partLength;
    }

    public File getSourceFile() {
        return sourceFile;
    }

    public void close() throws IOException {
        input.close();
    }
}