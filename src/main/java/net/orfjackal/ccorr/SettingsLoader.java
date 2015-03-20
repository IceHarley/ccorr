// Copyright © 2003-2006, 2010-2015, Esko Luontola <www.orfjackal.net>
// This software is released under the GNU General Public License, version 2 or later.
// The license text is at http://www.gnu.org/licenses/gpl.html

package net.orfjackal.ccorr;

import java.io.File;

public class SettingsLoader {
    private static File settingsFile = new File(System.getProperty("user.home")
            + System.getProperty("file.separator") + ".ccorr.cfg");

    public static void saveSettings() {
        if (ObjectSaver.saveToFile(settingsFile, Settings.settings)) {
            Log.print("Settings: Saved");
        } else {
            Log.print("Settings: Saving Failed");
        }
    }

    public static Settings loadSettings() {
        Settings settings = null;
        Object obj = ObjectSaver.loadFromFile(settingsFile);
        if (obj instanceof Settings) {
            settings = (Settings) (obj);
            Log.print("Settings: Loaded");
        } else {
            Log.print("Settings: Loading Failed. Defaults will be used");
        }
        return settings;
    }
}
