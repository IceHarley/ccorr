// Copyright © 2003-2006, 2010-2015, Esko Luontola <www.orfjackal.net>
// This software is released under the GNU General Public License, version 2 or later.
// The license text is at http://www.gnu.org/licenses/gpl.html

package net.orfjackal.ccorr.combination;

import net.orfjackal.ccorr.settings.*;

import java.io.*;
import java.util.logging.Logger;

public class StreamFactory {
    private final static Logger logger = Logger.getLogger(SettingsLoader.class.getName());

    public static OutputStream openOutputStream(File file) {
        try {
            return tryOpenOutputStream(file);
        }
        catch (FileNotFoundException e) {
            logger.throwing("StreamFactory", "openOutputStream", e);
        }
        return null;
    }

    private static OutputStream tryOpenOutputStream(File file) throws FileNotFoundException {
        return new BufferedOutputStream(
                new FileOutputStream(file, false),
                Settings.getWriteBufferLength());
    }

    public static InputStream openInputStream(File file) {
        try {
            return tryOpenInputStream(file);
        } catch (FileNotFoundException e) {
            logger.throwing("StreamFactory", "openInputStream", e);
        }
        return null;
    }

    private static InputStream tryOpenInputStream(File file) throws FileNotFoundException {
        return new BufferedInputStream(
                new FileInputStream(file),
                Settings.getReadBufferLength());
    }
}
