// Copyright © 2003-2006, 2010, Esko Luontola <www.orfjackal.net>
// This software is released under the GNU General Public License, version 2 or later.
// The license text is at http://www.gnu.org/licenses/gpl.html

package net.orfjackal.ccorr.settings;

import net.orfjackal.ccorr.checksum.*;

import java.awt.*;
import java.io.*;

/**
 * Stores the global settings used by CCorr. The settings are loaded when this class is initiated and they are saved at
 * request. The settings are stored in the user's home directory as <code>.ccorr.cfg</code>.
 *
 * @author Esko Luontola
 */
public class Settings implements Serializable {
    private static final long serialVersionUID = 1L;
    public static final String APP_NAME = "Corruption Corrector";

    public static final String APP_NAME_SHORT = "CCorr";
    public static final String VERSION_NUMBER = "1.03";
    public static final String COPYRIGHT = "Copyright (C) 2003-2006  Esko Luontola, www.orfjackal.net";
    public static final String WEBSITE = "http://ccorr.sourceforge.net";

    public static final int MAX_PART_SIZE = 10 * 1024 * 1024;    // 10 MB
    public static final int MIN_PART_SIZE = 1024;           // 1 KB

    static Settings settings;

    static {
        Settings.settings = SettingsLoader.loadSettings();
    }

    /**
     * Default checksums algorithm's name.
     */
    private String defaultAlgorithm = "CRC-32";

    /**
     * Default part length for {@link ChecksumFile ChecksumFile}.
     */
    private long defaultPartLength = 16 * 1024;      // 16 KB

    /**
     * Buffer length for <code>InputStream</code>s
     */
    private final int readBufferLength = 1024 * 1024;      // 1 MB

    /**
     * Buffer length for <code>OutputStream</code>s
     */
    private final int writeBufferLength = 1024 * 1024;     // 1 MB

    /**
     * Current working directory for file dialogs.
     */
    private File currentDirectory = new File("./");

    /**
     * The size and location of the {@link net.orfjackal.ccorr.gui.MainWindow MainWindow} for future sessions.
     */
    private Rectangle windowBounds = new Rectangle(300, 300, 500, 420);

    Settings() {
    }

    /**
     * Sets the default algorithm for the checksums.
     *
     * @param algorithm name of the algorithm
     */
    public static void setDefaultAlgorithm(String algorithm) {
        if (algorithm != null)
            settings.defaultAlgorithm = algorithm;
    }

    /**
     * Returns the default algorithm for the checksums.
     *
     * @return the name of the algorithm
     */
    public static String getDefaultAlgorithm() {
        return settings.defaultAlgorithm;
    }

    /**
     * Sets the default part length for <code>ChecksumFile</code>. If the value is less than
     * <code>ChecksumFile.MIN_PART_SIZE</code> or greater than <code>ChecksumFile.MAX_PART_SIZE</code>, the nearest
     * allowed value will be used.
     */
    public static void setDefaultPartLength(long length) {
        if (length < MIN_PART_SIZE)
            length = MIN_PART_SIZE;
        else if (length > MAX_PART_SIZE)
            length = MAX_PART_SIZE;
        settings.defaultPartLength = length;
    }

    /**
     * Returns the default part length for <code>ChecksumFile</code>.
     */
    public static long getDefaultPartLength() {
        return settings.defaultPartLength;
    }

    /**
     * Returns the buffer length for <code>InputStreams</code>.
     */
    public static int getReadBufferLength() {
        return settings.readBufferLength;
    }

    /**
     * Returns the buffer length for <code>OutputStreams</code>.
     */
    public static int getWriteBufferLength() {
        return settings.writeBufferLength;
    }

    /**
     * Sets the current working directory.
     *
     * @param file a file in the directory that will be set as current
     */
    public static void setCurrentDirectory(File file) {
        if (file != null)
            settings.currentDirectory = file.getAbsoluteFile().getParentFile();
    }

    /**
     * Returns the current working directory.
     */
    public static File getCurrentDirectory() {
        return settings.currentDirectory;
    }

    /**
     * Sets the size and location of the {@link net.orfjackal.ccorr.gui.MainWindow MainWindow} for future sessions.
     */
    public static void setWindowBounds(Rectangle bounds) {
        if (bounds != null)
            settings.windowBounds = bounds;
    }

    /**
     * Returns the saved size and location of the {@link net.orfjackal.ccorr.gui.MainWindow MainWindow}.
     */
    public static Rectangle getWindowBounds() {
        return settings.windowBounds;
    }

}