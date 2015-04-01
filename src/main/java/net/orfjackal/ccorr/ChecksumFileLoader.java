// Copyright © 2003-2006, 2010-2015, Esko Luontola <www.orfjackal.net>
// This software is released under the GNU General Public License, version 2 or later.
// The license text is at http://www.gnu.org/licenses/gpl.html

package net.orfjackal.ccorr;

import java.io.File;
import java.io.Serializable;

public class ChecksumFileLoader implements Serializable {
    ChecksumFile checksumFile;

    public ChecksumFileLoader(ChecksumFile checksumFile) {
        this.checksumFile = checksumFile;
    }

    public boolean saveToFile(File file) {
        return ObjectSaver.saveToFile(file, checksumFile);
    }

    public static ChecksumFile loadFromFile(File file) {
        ChecksumFile result;
        try {
            result = (ChecksumFile) (ObjectSaver.loadFromFile(file));
        } catch (Exception e) {
            e.printStackTrace();
            result = null;
        }
        return result;
    }
}
