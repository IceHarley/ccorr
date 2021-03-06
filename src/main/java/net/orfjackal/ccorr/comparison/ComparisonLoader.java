// Copyright © 2003-2006, 2010-2015, Esko Luontola <www.orfjackal.net>
// This software is released under the GNU General Public License, version 2 or later.
// The license text is at http://www.gnu.org/licenses/gpl.html

package net.orfjackal.ccorr.comparison;

import net.orfjackal.ccorr.serialization.ObjectSaver;

import java.io.*;

public class ComparisonLoader implements Serializable {
    private final Comparison comparison;
    private File savedAsFile;

    public ComparisonLoader(Comparison comparison) {
        this.comparison = comparison;
    }

    public ComparisonLoader(File file) {
        this.comparison = loadFromFile(file);
    }

    public boolean saveToFile(File file) {
        boolean successful = ObjectSaver.saveToFile(file, comparison);
        if (successful)
            savedAsFile = file;
        return successful;
    }

    public Comparison getComparison() {
        return comparison;
    }

    private Comparison loadFromFile(File file) {
        Comparison result;
        try {
            result = (Comparison) (ObjectSaver.loadFromFile(file));
            savedAsFile = file;
        }
        catch (Exception e) {
            e.printStackTrace();
            result = null;
        }
        return result;
    }

    public File getSavedAsFile() {
        return this.savedAsFile;
    }
}
