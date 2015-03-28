// Copyright © 2003-2006, 2010, Esko Luontola <www.orfjackal.net>
// This software is released under the GNU General Public License, version 2 or later.
// The license text is at http://www.gnu.org/licenses/gpl.html

package net.orfjackal.ccorr;

import java.io.*;

/**
 * @author Esko Luontola
 */
public class TestDataUtil {

    public static final String ALGORITHM = "CRC-32";
    public static final int PART_LENGTH = 1024;
    public static final int START_OFFSET_0 = 0;
    public static final int START_OFFSET_1 = PART_LENGTH;
    public static final int START_OFFSET_2 = PART_LENGTH * 2;
    public static final int START_OFFSET_3 = PART_LENGTH * 3;
    public static final int END_OFFSET_0 = START_OFFSET_1 - 1;
    public static final int END_OFFSET_1 = START_OFFSET_2 - 1;
    public static final int END_OFFSET_2 = START_OFFSET_3 - 1;

    private final TempDirectory temp = new TempDirectory();
    private int nextFileId = 1;

    public void create() {
        temp.create();
    }

    public void dispose() {
        temp.dispose();
    }

    public ChecksumFile createChecksumFile(long length, long... corruptedOffsets) throws IOException {
        return createChecksumFile(length, PART_LENGTH, ALGORITHM, corruptedOffsets);
    }

    public ChecksumFile createChecksumFile(long length, long partLength, String algorithm, long... corruptedOffsets) throws IOException {
        File file = createDummyFile(length);
        corruptDataAtOffsets(file, corruptedOffsets);
        return ChecksumFileFactory.createChecksumFile(file, partLength, algorithm);
    }

    public File createDummyFile(long length) throws IOException {
        File file = uniqueFile();
        writeDummyData(file, length);
        return file;
    }

    public File uniqueFile() {
        File file = new File(temp.getDirectory(), "file" + nextFileId + ".tmp");
        nextFileId++;
        return file;
    }

    private static void writeDummyData(File file, long length) throws IOException {
        OutputStream out = new BufferedOutputStream(new FileOutputStream(file), GoodCombinationWriter.BUFFER_LENGTH);
        for (long pos = 0; pos < length; pos++) {
            out.write((byte) pos);
        }
        out.close();
    }

    private static void corruptDataAtOffsets(File file, long... corruptionOffsets) throws IOException {
        RandomAccessFile raf = new RandomAccessFile(file, "rw");
        for (long offset : corruptionOffsets) {
            raf.seek(offset);
            int b = raf.read();
            raf.seek(offset);
            raf.write(~b);
        }
        raf.close();
    }
}
