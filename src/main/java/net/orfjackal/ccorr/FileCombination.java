// Copyright © 2003-2006, 2010, Esko Luontola <www.orfjackal.net>
// This software is released under the GNU General Public License, version 2 or later.
// The license text is at http://www.gnu.org/licenses/gpl.html

package net.orfjackal.ccorr;

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

    public boolean writeFile(File file) {
        boolean successful = true;
        try {
            Log.print("writeFile: writing " + this.getOutputFilesTotalLength() + " bytes to " + file);
            tryWriteFile(file);
        } catch (IOException e) {
            e.printStackTrace();
            Log.print("writeFile: Error (" + e + ")");
            successful = false;
        }
        catch (UserCancellationException e) {
            Log.print(e.getMessage());
            successful = false;
        }

        if (successful) {
            Log.println("writeFile: Done");
        } else {
            Log.println("writeFile: Failed");
        }
        return successful;
    }

    protected long getOutputFilesTotalLength() {
        long length = 0;
        for (int i = 0; i < items.size(); i++) {
            length += items.get(i).getEndOffset() - items.get(i).getStartOffset() + 1;
        }
        length--;
        return length;
    }

    private void tryWriteFile(File file) throws IOException {
        try {
            openOutputStream(file);
            writeItems();
        }
        finally {
            closeOutputStream();
        }
    }

    private void openOutputStream(File file) throws FileNotFoundException {
        output = new BufferedOutputStream(
                new FileOutputStream(file, false),
                Settings.getWriteBufferLength());
    }

    private void writeItems() throws IOException {
        for (FilePart item : items)
            writeItem(item);
    }

    protected void writeItem(FilePart item) throws IOException {
        Log.print("writeFile: writing item " + item + " from files " + item.getFile());
        try {
            openInputStream(item.getFile());
            copyData(item);
        }
        finally {
            closeInputStream();
        }
    }

    private void openInputStream(File file) throws FileNotFoundException {
        input = new BufferedInputStream(
                new FileInputStream(file),
                Settings.getReadBufferLength());
    }

    protected void copyData(FilePart item) throws IOException {
        long pos = gotoStartPosition(item);
        copyDataFromPosition(item, pos);
    }

    private void closeInputStream() throws IOException {
        if (input != null) {
            input.close();
            input = null;
        }
    }

    private void closeOutputStream() throws IOException {
        if (output != null) {
            output.close();
            output = null;
        }
    }

    private void copyDataFromPosition(FilePart item, long pos) throws IOException {
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

    private long gotoStartPosition(FilePart item) throws IOException {
        long pos = item.getStartOffset();
        if (input.skip(pos) != pos) {
            throw new IOException("unable to start reading from the right position");
        }
        return pos;
    }

    protected void cancel() throws IOException {
        closeInputStream();
        closeOutputStream();
        throw new UserCancellationException("Cancelled by user");
    }
}