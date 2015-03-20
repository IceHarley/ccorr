// Copyright © 2003-2006, 2010, Esko Luontola <www.orfjackal.net>
// This software is released under the GNU General Public License, version 2 or later.
// The license text is at http://www.gnu.org/licenses/gpl.html

package net.orfjackal.ccorr;

import javax.swing.*;
import java.io.*;
import java.util.Vector;

/**
 * A class for combining many files in to one. After creating a new instance of this class, the <code>addItem</code>
 * method should be used to determine which bytes should be read from which files. The files are read in the order that
 * the items are added. The file this class represents can then be written or its checksum can be calculated without
 * writing a physical file. A <code>FileCombination</code> is usually created by a <code>Comparison</code>.
 *
 * @author Esko Luontola
 */
public class FileCombination {

    /**
     * Size of the buffer for transferring data between input and output.
     */
    public static final int BUFFER_LENGTH = 1024 * 1024;

    /**
     * The files of the items. The <code>Vector</code>'s all items are <code>instanceof File</code>.
     */
    private Vector<File> file;

    /**
     * The start offsets of the items. The <code>Vector</code>'s all items are <code>instanceof Long</code>.
     */
    private Vector<Long> startOffset;

    /**
     * The end offsets of the items. The <code>Vector</code>'s all items are <code>instanceof Long</code>.
     */
    private Vector<Long> endOffset;

    /**
     * Creates a new <code>FileCombination</code> with no items.
     */
    public FileCombination() {
        this.file = new Vector<File>();
        this.startOffset = new Vector<Long>();
        this.endOffset = new Vector<Long>();
    }

    /**
     * Adds an item to this <code>FileCombination</code>.
     *
     * @param file  the file from which to read the bytes
     * @param start the first byte's index to be read
     * @param end   the last byte's index to be read
     */
    public void addItem(File file, long start, long end) {
        if (file != null && start >= 0 && end > start) {
            Log.print("FileCombination.addItem:\t" + start + "-" + end + "\t(" + file + ")");
            this.file.add(file);
            this.startOffset.add(start);
            this.endOffset.add(end);
        } else {
            Log.print("FAILED: FileCombination.addItem:\t" + start + "-" + end + "\t(" + file + ")");
        }
    }

    /**
     * Returns the number of items.
     */
    public int getItems() {
        return this.file.size();
    }

    /**
     * Returns the file related to the given item.
     *
     * @param item the index of the item
     * @return the file, or null if parameter invalid
     */
    public File getFile(int item) {
        if (item < 0 || item >= this.file.size()) {
            return null;
        } else {
            return this.file.elementAt(item);
        }
    }

    /**
     * Returns the offset of the related item's first byte.
     *
     * @param item the index of the item
     * @return the index of the item's first byte, or -1 if parameter invalid
     */
    public long getStart(int item) {
        if (item < 0 || item >= this.startOffset.size()) {
            return -1;
        } else {
            return this.startOffset.elementAt(item);
        }
    }

    /**
     * Returns the offset of the related item's last byte.
     *
     * @param item the index of the item
     * @return the index of the item's last byte, or -1 if parameter invalid
     */
    public long getEnd(int item) {
        if (item < 0 || item >= this.endOffset.size()) {
            return -1;
        } else {
            return this.endOffset.elementAt(item);
        }
    }

    /**
     * Returns the length of the output file.
     *
     * @return the total length of all items
     */
    public long getLength() {
        long length = 0;
        for (int i = 0; i < this.getItems(); i++) {
            length += this.getEnd(i) - this.getStart(i) + 1;
        }
        length--;
        return length;
    }

    /**
     * Writes the output of this <code>FileCombination</code> to a file. The operation can take some minutes depending
     * on the size of the file to be processed. A ProgressMonitor is used if available.
     *
     * @param file the file which is written
     * @return true if successful, otherwise false
     * @see ProgressMonitorRepository#set(javax.swing.ProgressMonitor)
     */
    public boolean writeFile(File file) {
        boolean successful;
        ProgressMonitor monitor = ProgressMonitorRepository.get();

        BufferedInputStream input;
        BufferedOutputStream output;

        try {
            long written = 0;
            long fileLength = this.getLength();
            Log.print("writeFile: writing " + fileLength + " bytes to " + file);

            // setup progress monitor
            if (monitor != null) {
                monitor.setMinimum(0);
                monitor.setMaximum(100);
            }


            output = new BufferedOutputStream(
                    new FileOutputStream(file, false),
                    Settings.getWriteBufferLength());
            byte[] buffer = new byte[BUFFER_LENGTH];

            // start writing
            for (int item = 0; item < this.getItems(); item++) {
                Log.print("writeFile: writing item " + item + " from file " + this.getFile(item));
                input = new BufferedInputStream(
                        new FileInputStream(this.getFile(item)),
                        Settings.getReadBufferLength()
                );
                written += copyData(this.getStart(item), this.getEnd(item), input, output, buffer,
                        monitor, written, fileLength);
                input.close();
            }

            output.close();
            successful = true;

        } catch (Exception e) {
            e.printStackTrace();
            Log.print("writeFile: Error (" + e + ")");
            successful = false;
        }

        // close progress monitor
        if (monitor != null) {
            monitor.setProgress(monitor.getMaximum());
        }

        if (successful) {
            Log.println("writeFile: Done");
        } else {
            Log.println("writeFile: Failed");
        }

        return successful;
    }

    /**
     * Copies data from an <code>InputStream</code> to an <code>OutputStream</code>.
     *
     * @param start         input's start offset
     * @param end           input's end offset
     * @param input         the input from which to read
     * @param output        the output in which to write
     * @param buffer        buffer for transferring bytes from input to output
     * @param monitor       optional <code>ProgressMonitor</code>, can be null
     * @param progressValue for monitor, how much has been written to output
     * @param progressMax   for monitor, the full length of the output file
     * @return how many bytes were copied
     * @throws Exception something went wrong or operation was cancelled by user
     */
    private static long copyData(long start, long end,
                                 InputStream input, OutputStream output,
                                 byte[] buffer, ProgressMonitor monitor,
                                 long progressValue, long progressMax) throws Exception {
        long pos = start;
        if (input.skip(pos) != pos) {
            throw new Exception("unable to start reading from the right position");
        }

        long written = 0;
        int lastMonitorValue = -1;

        boolean continueReading = true;
        while (continueReading) {
            int len = input.read(buffer);
            if (len == -1) {
                return written;
            }

            if (pos + len > end) {
                len = (int) (end - pos + 1);
                continueReading = false;
            }

            output.write(buffer, 0, len);
            pos += len;
            written += len;

            // update progress monitor
            if (monitor != null) {
                progressValue += len;
                int percentage = (int) (progressValue * 100 / progressMax);
                if (percentage != lastMonitorValue) {
                    monitor.setProgress(percentage);
                    monitor.setNote("Completed " + percentage + "%");
                    lastMonitorValue = percentage;
                    Thread.yield(); // allow the GUI some time to be updated
                }

                if (monitor.isCanceled()) {
                    input.close();
                    output.close();
                    throw new Exception("Cancelled by user");
                }
            }
        }

        return written;
    }

    /**
     * Counts the checksum of this <code>FileCombination</code>'s output. The operation can take some minutes depending
     * on the size of the file to be processed. A ProgressMonitor is used if available (TODO: NOT IMPLEMENTED).
     *
     * @param algorithm the name of the algorithm to be used for making the checksum
     * @return the checksum, or null if operation failed
     * @see CRCAlgorithmFactory#getSupportedAlgorithms()
     * @see ProgressMonitorRepository#set(javax.swing.ProgressMonitor)
     */
    public String countChecksum(String algorithm) {
        FileCombination[] fc = {this};
        String[] result = FileCombination.countChecksums(fc, algorithm);

        if (result == null) {
            return null;
        } else {
            return result[0];
        }
    }

    /**
     * Counts the checksums of an array of <code>FileCombination</code>s. The operation can take some minutes depending
     * on the size of the file to be processed. A ProgressMonitor is used if available (TODO: NOT IMPLEMENTED).
     *
     * @param fc        the <code>FileCombination</code>s which to process
     * @param algorithm the name of the algorithm to be used for making the checksums
     * @return an array containing the checksums, or null if operation failed
     * @see CRCAlgorithmFactory#getSupportedAlgorithms()
     * @see ProgressMonitorRepository#set(javax.swing.ProgressMonitor)
     */
    public static String[] countChecksums(FileCombination[] fc, String algorithm) {

        // TODO: support for ProgressMonitor

        if (fc == null || fc.length == 0) {
            return null;
        }
        Log.print("countChecksums: Start, " + fc.length + " combinations, using " + algorithm);

        // the parameters must meet the following requirements:
        for (int i = 1; i < fc.length; i++) {                   // 1. same number of items
            if (fc[0].getItems() != fc[i].getItems()) {
                return null;
            } else {
                for (int j = 0; j < fc[0].getItems(); j++) {    // 2. same start and end offsets
                    if (fc[0].getStart(i) != fc[i].getStart(i)
                            || fc[0].getEnd(i) != fc[i].getEnd(i)) {
                        return null;
                    }
                }
            }
        }

        // individual crc for each combination
        CRC[] crc = new CRC[fc.length];
        for (int i = 0; i < crc.length; i++) {
            crc[i] = new CRC(CRCAlgorithmFactory.getByName(algorithm));
        }

        for (int item = 0; item < fc[0].getItems(); item++) {
            Vector<File> readFiles = new Vector<File>();
            for (int i = 0; i < fc.length; i++) {
                if (!readFiles.contains(fc[i].getFile(item))) {     // if the file hasn't been read before

                    // select all checksums that can be read from the same file
                    Vector<CRC> selectedCrcs = new Vector<CRC>();
                    for (int j = 0; j < fc.length; j++) {
                        if (fc[j].getFile(item) == fc[i].getFile(item)) {
                            selectedCrcs.add(crc[j]);
                        }
                    }
                    CRC[] output = selectedCrcs.toArray(new CRC[0]);

                    try {
                        Log.print("countChecksums: reading item " + item + " from file " + i);
                        BufferedInputStream input =
                                new BufferedInputStream(
                                        new FileInputStream(fc[i].getFile(item)),
                                        Settings.getReadBufferLength()
                                );
                        byte[] buffer = new byte[BUFFER_LENGTH];
                        copyData(fc[i].getStart(item), fc[i].getEnd(item), input, output, buffer);
                        input.close();

                    } catch (Exception e) {
                        e.printStackTrace();
                        Log.println("countChecksums: Aborted, " + e.toString());
                        return null;
                    }
                    readFiles.add(fc[i].getFile(item));
                }
            }
        }

        String[] result = new String[crc.length];
        for (int i = 0; i < result.length; i++) {
            result[i] = crc[i].getHexValue();
        }

        StringBuffer checksums = new StringBuffer();
        checksums.append("countChecksums: Done");
        for (int i = 0; i < result.length; i++) {
            checksums.append("\nChecksum " + i + ": \t" + result[i]);
        }
        Log.print(checksums.toString());

        return result;
    }

    /**
     * Updates all the <code>CRC</code>s in an array with the data from an <code>InputStream</code>.
     *
     * @param start  input's start offset
     * @param end    input's end offset
     * @param input  the input from which to read
     * @param output the <code>CRC</code>s to be updated
     * @param buffer buffer for transferring bytes from input to output
     * @throws Exception something went wrong
     */
    private static void copyData(long start, long end,
                                 InputStream input, CRC[] output,
                                 byte[] buffer) throws Exception {
        long pos = start;
        if (input.skip(pos) != pos) {
            throw new Exception("unable to start reading from the right position");
        }

        boolean continueReading = true;
        while (continueReading) {
            int len = input.read(buffer);
            if (len == -1) {
                return;
            }

            if (pos + len > end) {
                len = (int) (end - pos + 1);
                continueReading = false;
            }

            for (CRC out : output) {
                out.update(buffer, 0, len);
            }
            pos += len;
        }
    }

}