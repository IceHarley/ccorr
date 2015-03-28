// Copyright © 2003-2006, 2010-2015, Esko Luontola <www.orfjackal.net>
// This software is released under the GNU General Public License, version 2 or later.
// The license text is at http://www.gnu.org/licenses/gpl.html

package net.orfjackal.ccorr;

import java.io.*;

public class StreamFactory {
    public static BufferedOutputStream openOutputStream(File file) {
        try {
            return tryOpenOutputStream(file);
        } catch (FileNotFoundException e) {
            Log.println(e.getMessage());
        }
        return null;
    }

    private static BufferedOutputStream tryOpenOutputStream(File file) throws FileNotFoundException {
        return new BufferedOutputStream(
                new FileOutputStream(file, false),
                Settings.getWriteBufferLength());
    }

    public static BufferedInputStream openInputStream(File file) {
        try {
            return tryOpenInputStream(file);
        } catch (FileNotFoundException e) {
            Log.println(e.getMessage());
        }
        return null;
    }

    private static BufferedInputStream tryOpenInputStream(File file) throws FileNotFoundException {
        return new BufferedInputStream(
                new FileInputStream(file),
                Settings.getReadBufferLength());
    }
}
