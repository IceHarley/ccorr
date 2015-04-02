// Copyright © 2003-2006, 2010, Esko Luontola <www.orfjackal.net>
// This software is released under the GNU General Public License, version 2 or later.
// The license text is at http://www.gnu.org/licenses/gpl.html

package net.orfjackal.ccorr.serialization;

import java.io.*;
import java.util.logging.*;
import java.util.zip.*;

public class ObjectSaver {
    private final static Logger logger = Logger.getLogger(ObjectSaver.class.getName());

    private ObjectSaver() {
    }

    public static boolean saveToFile(File file, Serializable obj) {
        if (file == null || obj == null) {
            logger.severe("ObjectSaver.saveToFile: Aborted, null arguments");
            return false;
        }

        try {
            ObjectOutputStream output =
                    new ObjectOutputStream(
                            new GZIPOutputStream(
                                    new BufferedOutputStream(
                                            new FileOutputStream(file)
                                    )
                            )
                    );
            output.writeObject(obj);
            output.close();
        }
        catch (Exception e) {
            e.printStackTrace();
            logger.throwing("ObjectSaver", "saveToFile", e);
            return false;
        }

        logger.log(Level.INFO, "ObjectSaver.saveToFile: Done, wrote {0} to {1}", new Object[]{obj.getClass(), file});
        return true;
    }

    public static Object loadFromFile(File file) {
        if (file == null) {
            logger.severe("ObjectSaver.loadFromFile: Aborted, null arguments");
            return null;
        }

        Object result;
        try {
            ObjectInputStream input =
                    new ObjectInputStream(
                            new GZIPInputStream(
                                    new BufferedInputStream(
                                            new FileInputStream(file)
                                    )
                            )
                    );
            result = input.readObject();
            input.close();
        }
        catch (Exception e) {
            e.printStackTrace();
            logger.throwing("ObjectSaver", "loadFromFile", e);
            return null;
        }

        logger.log(Level.INFO, "ObjectSaver.saveToFile: Done, read {0} from {1}", new Object[]{result.getClass(), file});
        return result;
    }
}