// Copyright © 2003-2006, 2010, Esko Luontola <www.orfjackal.net>
// This software is released under the GNU General Public License, version 2 or later.
// The license text is at http://www.gnu.org/licenses/gpl.html

package net.orfjackal.ccorr.combination;

import java.io.*;
import java.util.logging.*;

public class GoodCombinationWriter {
    private final static Logger logger = Logger.getLogger(GoodCombinationWriter.class.getName());

    public static final int BUFFER_LENGTH = 1024 * 1024;
    private final GoodCombination parts;
    private OutputStream output;
    private InputStream input;

    public GoodCombinationWriter(GoodCombination parts) {
        this.parts = parts;
    }

    public boolean writeCombination(OutputStream output) {
        this.output = output;
        boolean successful = true;
        try {
            logger.log(Level.INFO, "writeCombination: writing {0} bytes to {1}", new Object[]{getOutputFilesTotalLength(), output});
            tryWriteFile();
        } catch (IOException e) {
            e.printStackTrace();
            logger.throwing("GoodCombinationWriter", "writeCombination", e);
            successful = false;
        } catch (UserCancellationException e) {
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
        } finally {
            closeOutputStream();
        }
    }

    private void writeParts() throws IOException {
        for (GoodCombinationPart item : parts)
            writeItem(item);
    }

    private void writeItem(GoodCombinationPart part) throws IOException {
        logger.log(Level.INFO, "writeCombination: writing part {0) from stream {1}", new Object[]{part, parts.getStream(part.getStreamIndex())});
        input = parts.getStream(part.getStreamIndex());
        copyData(part);
    }

    protected void copyData(GoodCombinationPart item) throws IOException {
        long pos = gotoStartPosition(item);
        copyDataFromPosition(item, pos);
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

    private void closeOutputStream() throws IOException {
        if (output != null) {
            output.flush();
            output.close();
            output = null;
        }
    }

    protected void cancel() throws IOException {
        parts.closeStreams();
        closeOutputStream();
        throw new UserCancellationException();
    }
}