// Copyright © 2003-2006, 2010-2015, Esko Luontola <www.orfjackal.net>
// This software is released under the GNU General Public License, version 2 or later.
// The license text is at http://www.gnu.org/licenses/gpl.html

package net.orfjackal.ccorr;

import java.io.*;

public class ChecksumFileLoader implements Serializable {
    private File savedAsFile;
    ChecksumFile checksumFile;

    public ChecksumFileLoader(ChecksumFile checksumFile) {
        this.checksumFile = checksumFile;
    }

    private ChecksumFileLoader(ChecksumFile checksumFile, File savedAsFile) {
        this.savedAsFile = savedAsFile;
        this.checksumFile = checksumFile;
    }

    public boolean saveToFile(File file) {
        boolean successful = ObjectSaver.saveToFile(file, checksumFile);
        if (successful) {
            this.savedAsFile = file;
        }
        return successful;
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

    //Serialization
    private Object writeReplace() {
        return new SerializationProxy(this);
    }

    private void readObject(ObjectInputStream stream) throws InvalidObjectException {
        throw new InvalidObjectException("Proxy required.");
    }

    private static class SerializationProxy implements Serializable {
        private static final long serialVersionUID = 1L;

        private final ChecksumFile checksumFile;
        private final File savedAsFile;

        public SerializationProxy(ChecksumFileLoader target) {
            this.checksumFile = target.checksumFile;
            this.savedAsFile = target.savedAsFile;
        }

        private Object readResolve() {
            return new ChecksumFileLoader(checksumFile, savedAsFile);
        }
    }
}
