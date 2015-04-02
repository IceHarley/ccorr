// Copyright © 2003-2006, 2010-2015, Esko Luontola <www.orfjackal.net>
// This software is released under the GNU General Public License, version 2 or later.
// The license text is at http://www.gnu.org/licenses/gpl.html

package net.orfjackal.ccorr.settings;

import net.orfjackal.ccorr.serialization.ObjectSaver;

import java.io.File;
import java.util.logging.Logger;

public class SettingsLoader {
    private final static Logger logger = Logger.getLogger(SettingsLoader.class.getName());
    private static final File settingsFile = new File(System.getProperty("user.home")
            + System.getProperty("file.separator") + ".ccorr.cfg");

    public static void saveSettings() {
        logger.info(ObjectSaver.saveToFile(settingsFile, Settings.settings) ? "Settings: Saved" : "Settings: Saving Failed");
    }

    public static Settings loadSettings() {
        Settings settings;
        Object obj = ObjectSaver.loadFromFile(settingsFile);
        if (obj instanceof Settings) {
            settings = (Settings) (obj);
            logger.info("Settings: Loaded");
        } else {
            settings = new Settings();
            logger.warning("Settings: Loading Failed. Defaults will be used");
        }
        return settings;
    }
}
