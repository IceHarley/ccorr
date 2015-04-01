// Copyright © 2003-2006, 2010-2015, Esko Luontola <www.orfjackal.net>
// This software is released under the GNU General Public License, version 2 or later.
// The license text is at http://www.gnu.org/licenses/gpl.html

package net.orfjackal.ccorr;

import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

public class GoodCombination implements Iterable<GoodCombinationPart> {
    private final static Logger logger = Logger.getLogger(GoodCombination.class.getName());

    public static final GoodCombination NOT_EXISTS = new GoodCombination();

    List<GoodCombinationPart> parts;
    private Map<Integer, InputStream> streams;

    public GoodCombination() {
        parts = new ArrayList<GoodCombinationPart>();
        streams = new HashMap<Integer, InputStream>();
    }

    public void add(GoodCombinationPart part) {
        if (isValidPart(part)) {
            parts.add(part);
            logger.log(Level.INFO, "GoodCombination.add:\t{0}", part);
        }
        else {
            logger.log(Level.INFO, "FAILED: GoodCombination.addItem:\t{0}", part);
        }
    }

    private boolean isValidPart(GoodCombinationPart part) {
        return part != null && part.isValid();
    }

    public int size() {
        return parts.size();
    }

    public GoodCombinationPart get(int index) {
        return parts.get(index);
    }

    @Override
    public Iterator<GoodCombinationPart> iterator() {
        return parts.iterator();
    }

    public void addStream(int streamIndex, InputStream stream) {
        if (!containsStream(streamIndex))
            streams.put(streamIndex, stream);
    }

    public boolean containsStream(int streamIndex) {
        return streams.containsKey(streamIndex);
    }

    public InputStream getStream(Integer streamIndex) {
        return streams.get(streamIndex);
    }

    public void closeStreams() throws IOException {
        for (InputStream stream : streams.values())
            if (stream != null) {
                stream.close();
            }
        streams.clear();
    }

}
