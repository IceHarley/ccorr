// Copyright © 2003-2006, 2010, Esko Luontola <www.orfjackal.net>
// This software is released under the GNU General Public License, version 2 or later.
// The license text is at http://www.gnu.org/licenses/gpl.html

package net.orfjackal.ccorr;

import javax.swing.*;
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

    /**
     * The minimum buffer length to be used for reading and writing files.
     */
    public static final int MIN_BUFFER_LENGTH = 10 * 1024;          // 10 KB

    /**
     * The maximum buffer length to be used for reading and writing files.
     */
    public static final int MAX_BUFFER_LENGTH = 10 * 1024 * 1024;    // 10 MB

    /**
     * An instance of this class. The settings that need to be saved between program sessions are stored in an object so
     * that they could be easilly saved and loaded by using {@link ObjectSaver ObjectSaver}.
     */
    private static Settings settings = new Settings();

    /**
     * Location of the settings file. It is in the user's home directory with the name <code>.ccorr.cfg</code>.
     */
    private static File settingsFile = new File(System.getProperty("user.home")
            + System.getProperty("file.separator") + ".ccorr.cfg");

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
    private int readBufferLength = 1024 * 1024;      // 1 MB

    /**
     * Buffer length for <code>OutpuStream</code>s
     */
    private int writeBufferLength = 1024 * 1024;     // 1 MB

    /**
     * Whether mark mirroring is enabled.
     */
    private boolean markMirroringEnabled = true;

    /**
     * Current working directory for file dialogs.
     */
    private File currentDirectory = new File("./");

    /**
     * The size and location of the {@link net.orfjackal.ccorr.gui.MainWindow MainWindow} for future sessions.
     */
    private Rectangle windowBounds = new Rectangle(300, 300, 500, 420);

    /**
     * The <code>ProgressMonitor</code> for the next long task.
     */
    private static ProgressMonitor progressMonitor = null;

    static {
        Settings.loadSettings();
    }

    private Settings() {
    }

    /**
     * Saves the settings to a file.
     *
     * @see #settingsFile
     */
    public static void saveSettings() {
        if (ObjectSaver.saveToFile(settingsFile, settings)) {
            Log.print("Settings: Saved");
        } else {
            Log.print("Settings: Saving Failed");
        }
    }

    /**
     * Loads the settings from a file.
     *
     * @see #settingsFile
     */
    public static void loadSettings() {
        Object obj = ObjectSaver.loadFromFile(settingsFile);
        if (obj instanceof Settings) {
            settings = (Settings) (obj);
            Log.print("Settings: Loaded");
        } else {
            Log.print("Settings: Loading Failed"); // no problem, will be using defaults
        }
    }

    /**
     * Sets the default algorithm for the checksums.
     *
     * @param algorithm name of the algorithm
     * @see CRCAlgorithmRepository#getSupportedAlgorithms()
     */
    public static void setDefaultAlgorithm(String algorithm) {
        if (algorithm != null) {
            settings.defaultAlgorithm = algorithm;
        }
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
        if (length < FileDivider.MIN_PART_SIZE) {
            length = FileDivider.MIN_PART_SIZE;
        } else if (length > FileDivider.MAX_PART_SIZE) {
            length = FileDivider.MAX_PART_SIZE;
        }
        settings.defaultPartLength = length;
    }

    /**
     * Returns the default part length for <code>ChecksumFile</code>.
     */
    public static long getDefaultPartLength() {
        return settings.defaultPartLength;
    }

    /**
     * Sets the buffer length for <code>InputStreams</code>. If the value is less than <code>MIN_BUFFER_LENGTH</code> or
     * greater than <code>MAX_BUFFER_LENGTH</code>, the nearest allowed value will be used.
     */
    public static void setReadBufferLength(int length) {
        if (length < MIN_BUFFER_LENGTH) {
            length = MIN_BUFFER_LENGTH;
        } else if (length > MAX_BUFFER_LENGTH) {
            length = MAX_BUFFER_LENGTH;
        }
        settings.readBufferLength = length;
    }

    /**
     * Returns the buffer length for <code>InputStreams</code>.
     */
    public static int getReadBufferLength() {
        return settings.readBufferLength;
    }

    /**
     * Sets the buffer length for <code>OutputStreams</code>. If the value is less than <code>MIN_BUFFER_LENGTH</code>
     * or greater than <code>MAX_BUFFER_LENGTH</code>, the nearest allowed value will be used.
     */
    public static void setWriteBufferLength(int length) {
        if (length < MIN_BUFFER_LENGTH) {
            length = MIN_BUFFER_LENGTH;
        } else if (length > MAX_BUFFER_LENGTH) {
            length = MAX_BUFFER_LENGTH;
        }
        settings.writeBufferLength = length;
    }

    /**
     * Returns the buffer length for <code>OutputStreams</code>.
     */
    public static int getWriteBufferLength() {
        return settings.writeBufferLength;
    }

    /**
     * Sets if mark mirroring is enabled.
     *
     * @param enabled true to enable, false to disable
     */
    public static void setMarkMirroringEnabled(boolean enabled) {
        settings.markMirroringEnabled = enabled;
    }

    /**
     * Returns whether mark mirroring is enabled.
     *
     * @return true if enabled, otherwise false
     */
    public static boolean isMarkMirroringEnabled() {
        return settings.markMirroringEnabled;
    }

    /**
     * Sets the current working directory.
     *
     * @param file a file in the directory that will be set as current
     */
    public static void setCurrentDirectory(File file) {
        if (file != null) {
            settings.currentDirectory = file.getAbsoluteFile().getParentFile();
        }
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
        if (bounds != null) {
            settings.windowBounds = bounds;
        }
    }

    /**
     * Returns the saved size and location of the {@link net.orfjackal.ccorr.gui.MainWindow MainWindow}.
     */
    public static Rectangle getWindowBounds() {
        return settings.windowBounds;
    }

    /**
     * Sets the <code>ProgressMonitor</code> to be used by the next long task.
     *
     * @param monitor an unused <code>ProgressMonitor</code> for the next one who calls <code>getProgressMonitor</code>,
     *                or null to remove it
     */
    public static void setProgressMonitor(ProgressMonitor monitor) {
        Settings.progressMonitor = monitor;
    }

    /**
     * Returns the <code>ProgressMonitor</code> that was set with <code>setProgressMonitor</code> after which a new one
     * must be set.
     *
     * @return the <code>ProgressMonitor</code> that was set, or null if one has not been set since
     *         <code>getProgressMonitor</code> was called the last time
     */
    public static ProgressMonitor getProgressMonitor() {
        ProgressMonitor result = Settings.progressMonitor;
        Settings.progressMonitor = null;
        return result;
    }
}