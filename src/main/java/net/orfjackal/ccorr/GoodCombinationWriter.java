// Copyright © 2003-2006, 2010, Esko Luontola <www.orfjackal.net>
// This software is released under the GNU General Public License, version 2 or later.
// The license text is at http://www.gnu.org/licenses/gpl.html

package net.orfjackal.ccorr;

import java.io.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * A class for combining many files in to one. After creating a new instance of this class, the <code>addItem</code>
 * method should be used to determine which bytes should be read from which files. The files are read in the order that
 * the parts are added. The files this class represents can then be written or its checksum can be calculated without
 * writing a physical files. A <code>FileCombination</code> is usually created by a <code>Comparison</code>.
 *
 * @author Esko Luontola
 */
public class GoodCombinationWriter {
    private final static Logger logger = Logger.getLogger(GoodCombinationWriter.class.getName());

    public static final int BUFFER_LENGTH = 1024 * 1024;
    private GoodCombination parts;
    private OutputStream output;
    private InputStream input;

    public GoodCombinationWriter(GoodCombination parts) {
        this.parts = parts;
    }

    public boolean writeCombination(OutputStream output) {
        this.output = output;
        boolean successful = true;
        try {
            logger.log(Level.INFO, "writeCombination: writing {0} bytes to {1}", new Object[] {getOutputFilesTotalLength(), output });
            tryWriteFile();
        } catch (IOException e) {
            e.printStackTrace();
            logger.throwing("GoodCombinationWriter", "writeCombination", e);
            successful = false;
        }
        catch (UserCancellationException e) {
            logger.throwing("GoodCombinationWriter", "writeCombination", e);
            successful = false;
        }

        logger.info(successful ? "writeCombination: Done" : "writeCombination: Failed");
        return successful;
    }

    protected long getOutputFilesTotalLength() {
        long length = 0;
        for (int i = 0; i < parts.size(); i++) {
            length += parts.get(i).getEndOffset() - parts.get(i).getStartOffset() + 1;
        }
        length--;
        return length;
    }

    private void tryWriteFile() throws IOException {
        try {
            writeParts();
        }
        finally {
            closeOutputStream();
        }
    }

    private void writeParts() throws IOException {
        for (GoodCombinationPart item : parts)
            writeItem(item);
    }

    protected void writeItem(GoodCombinationPart part) throws IOException {
        logger.log(Level.INFO, "writeCombination: writing part {0) from stream {1}", new Object[] { part, parts.getStream(part.getStreamIndex()) });
        input = parts.getStream(part.getStreamIndex());
        copyData(part);
    }

    protected void copyData(GoodCombinationPart item) throws IOException {
        long pos = gotoStartPosition(item);
        copyDataFromPosition(item, pos);
    }

    private void closeOutputStream() throws IOException {
        if (output != null) {
            output.flush();
            output.close();
            output = null;
        }
    }

    private void copyDataFromPosition(GoodCombinationPart item, long pos) throws IOException {
        boolean filePartEndReached = false;
        while (!filePartEndReached) {
            int len = BUFFER_LENGTH;
            if (pos + len > item.getEndOffset()) {
                len = (int) (item.getEndOffset() - pos + 1);
                filePartEndReached = true;
            }
            if (!copyBuffer(len))
                return;
        }
    }

    protected boolean copyBuffer(int bufferLength) throws IOException {
        byte[] buffer = new byte[bufferLength];
        int len = input.read(buffer);
        if (len == -1) {
            return false;
        }
        output.write(buffer, 0, len);
        return true;
    }

    private long gotoStartPosition(GoodCombinationPart item) throws IOException {
        long pos = item.getStartOffset();
        if (input.skip(pos) != pos) {
            throw new IOException("unable to start reading from the right position");
        }
        return pos;
    }

    protected void cancel() throws IOException {
        parts.closeStreams();
        closeOutputStream();
        throw new UserCancellationException();
    }
}