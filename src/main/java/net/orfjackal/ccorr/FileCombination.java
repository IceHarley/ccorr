// Copyright © 2003-2006, 2010, Esko Luontola <www.orfjackal.net>
// This software is released under the GNU General Public License, version 2 or later.
// The license text is at http://www.gnu.org/licenses/gpl.html

package net.orfjackal.ccorr;

import javax.swing.*;
import java.io.*;

/**
 * A class for combining many files in to one. After creating a new instance of this class, the <code>addItem</code>
 * method should be used to determine which bytes should be read from which files. The files are read in the order that
 * the items are added. The files this class represents can then be written or its checksum can be calculated without
 * writing a physical files. A <code>FileCombination</code> is usually created by a <code>Comparison</code>.
 *
 * @author Esko Luontola
 */
public class FileCombination {

    public static final int BUFFER_LENGTH = 1024 * 1024;
    private FileParts items;
    private BufferedOutputStream output;
    private BufferedInputStream input;

    public FileCombination() {
        items = new FileParts();
    }

    public void addItem(File file, long startOffset, long endOffset) {
        if (isValidOffset(file, startOffset, endOffset)) {
            Log.print("FileCombination.addItem:\t" + startOffset + "-" + endOffset + "\t(" + file + ")");
            items.add(new FilePart(file, startOffset, endOffset));
        } else {
            Log.print("FAILED: FileCombination.addItem:\t" + startOffset + "-" + endOffset + "\t(" + file + ")");
        }
    }

    private boolean isValidOffset(File file, long startOffset, long endOffset) {
        return file != null && startOffset >= 0 && endOffset > startOffset;
    }

    public int getItemsCount() {
        return items.size();
    }

    public long getOutputFilesTotalLength() {
        long length = 0;
        for (int i = 0; i < this.getItemsCount(); i++) {
            length += items.get(i).getEndOffset() - items.get(i).getStartOffset() + 1;
        }
        length--;
        return length;
    }

    /**
     * Writes the output of this <code>FileCombination</code> to a files. The operation can take some minutes depending
     * on the size of the files to be processed.
     *
     * @param file the files which is written
     * @return true if successful, otherwise false
     */
    public boolean writeFile(File file) {
        boolean successful;
        try {
            long written = 0;
            long fileLength = this.getOutputFilesTotalLength();
            Log.print("writeFile: writing " + fileLength + " bytes to " + file);

            output = getBufferedOutputStream(file);

            for (FilePart item : items)
                written = writeItem(written, fileLength, item);

            output.close();
            successful = true;

        } catch (Exception e) {
            e.printStackTrace();
            Log.print("writeFile: Error (" + e + ")");
            successful = false;
        }

        if (successful) {
            Log.println("writeFile: Done");
        } else {
            Log.println("writeFile: Failed");
        }

        return successful;
    }

    private BufferedOutputStream getBufferedOutputStream(File file) throws FileNotFoundException {
        BufferedOutputStream output;
        output = new BufferedOutputStream(
                new FileOutputStream(file, false),
                Settings.getWriteBufferLength());
        return output;
    }

    protected long writeItem(long written, long fileLength, FilePart item) throws Exception {
        Log.print("writeFile: writing item " + item + " from files " + item.getFile());
        byte[] buffer = new byte[BUFFER_LENGTH];
        input = getBufferedInputStream(item.getFile());
        written += copyData(item, buffer, written, fileLength);
        input.close();
        return written;
    }

    private BufferedInputStream getBufferedInputStream(File file) throws FileNotFoundException {
        BufferedInputStream input;
        input = new BufferedInputStream(
                new FileInputStream(file),
                Settings.getReadBufferLength()
        );
        return input;
    }

    /**
     * Copies data from an <code>InputStream</code> to an <code>OutputStream</code>.
     *
     * @param item filePart item
     * @param buffer        buffer for transferring bytes from input to output
     * @param progressValue for monitor, how much has been written to output
     * @param progressMax   for monitor, the full length of the output files
     * @return how many bytes were copied
     * @throws Exception something went wrong or operation was cancelled by user
     */
    private long copyData(FilePart item,
                                 byte[] buffer,
                                 long progressValue, long progressMax) throws Exception {
        long pos = item.getStartOffset();
        if (input.skip(pos) != pos) {
            throw new Exception("unable to start reading from the right position");
        }

        long written = 0;

        boolean continueReading = true;
        while (continueReading) {
            int len = input.read(buffer);
            if (len == -1) {
                return written;
            }

            if (pos + len > item.getEndOffset()) {
                len = (int) (item.getEndOffset() - pos + 1);
                continueReading = false;
            }

            output.write(buffer, 0, len);
            pos += len;
            written += len;
            progressValue += len;
            //lastMonitorValue = updateProgressMonitor(input, output, monitor, progressValue, progressMax, lastMonitorValue);
        }

        return written;
    }

    protected void cancel() throws Exception
    {
        input.close();
        output.close();
        throw new Exception("Cancelled by user");
    }
}