// Copyright © 2003-2006, 2010-2015, Esko Luontola <www.orfjackal.net>
// This software is released under the GNU General Public License, version 2 or later.
// The license text is at http://www.gnu.org/licenses/gpl.html

package net.orfjackal.ccorr;

import java.util.Hashtable;
import java.util.logging.Level;
import java.util.logging.Logger;

public class GoodPartsMarker {
    private final static Logger logger = Logger.getLogger(GoodPartsMarker.class.getName());

    private static int good;
    private static int unsure;
    private static int undefined;

    Comparison comparison;
    private Hashtable<String, Integer> checksumOccurrences;
    private int max;
    private int maxIndex;
    private boolean isUnsure;

    public GoodPartsMarker(Comparison comparison) {
        this.comparison = comparison;
    }

    public boolean markGoodParts(int diffFrom, int diffTo) {
        resetStatistics();
        if (!validateRange(diffFrom, diffTo))
            return false;
        markParts(diffFrom, diffTo);
        logStatistics();
        return true;
    }

    private void resetStatistics() {
        good = 0;
        unsure = 0;
        undefined = 0;
    }

    private void markParts(int diffFrom, int diffTo) {
        for (int difference = diffFrom; difference <= diffTo; difference++) {
            markPart(difference);
        }
    }

    private void markPart(int difference) {
        resetOccurrences();
        findFilesOccurrences(difference);
        setMarkAccordingToOccurrences(difference);
    }

    private void resetOccurrences() {
        checksumOccurrences = new Hashtable<String, Integer>();
        max = 1;
        maxIndex = -1;
        isUnsure = false;
    }

    private void setMarkAccordingToOccurrences(int difference) {
        if (maxIndex >= 0) setMark(difference);
        else leaveUndefined();
    }

    private void setMark(int difference) {
        if (isUnsure) setUnsure(difference);
        else setGood(difference);
    }

    private void leaveUndefined() {
        undefined++;
    }

    private void setGood(int difference) {
        comparison.setMark(difference, maxIndex, Mark.GOOD);
        good++;
    }

    private void setUnsure(int difference) {
        comparison.setMark(difference, maxIndex, Mark.UNSURE);
        unsure++;
    }

    private void findFilesOccurrences(int difference) {
        for (int file = 0; file < comparison.getFilesCount(); file++) {
            if (markerIsAlreadySet(difference, file)) break;
            findFileOccurrences(difference, file);
        }
    }

    private void findFileOccurrences(int difference, int file) {
        if (comparison.isGoodIndex(difference, file)) {
            String crc = comparison.getChecksum(difference, file);
            if (isPastTheEndOfFile(crc)) return;

            addCrcOccurrence(file, crc);
        }
    }

    private void addCrcOccurrence(int file, String crc) {
        Integer counter = addOccurrence(crc);
        if (counter > max) {
            crcIsRepetitionsLeader(file, counter);
        } else if (counter == max) {
            crcShareRepetitionLeadership();
        }
    }

    private void crcShareRepetitionLeadership() {
        isUnsure = true;
    }

    private void crcIsRepetitionsLeader(int file, Integer counter) {
        max = counter;
        maxIndex = file;
        isUnsure = false;
    }

    private boolean markerIsAlreadySet(int difference, int file) {
        if (comparison.getMark(difference, file) != Mark.UNDEFINED) {
            maxIndex = -1;
            return true;
        }
        return false;
    }

    private Integer addOccurrence(String crc) {
        Integer counter = 0;
        if (checksumOccurrences.containsKey(crc)) {
            counter = checksumOccurrences.get(crc);
            checksumOccurrences.remove(crc);
        }
        checksumOccurrences.put(crc, ++counter);
        return counter;
    }

    private boolean isPastTheEndOfFile(String crc) {
        return crc.length() == 0;
    }

    private void logStatistics() {
        logger.log(Level.INFO, "Comparison.markGoodParts: {0}", toString());
    }

    private boolean validateRange(int start, int end) {
        if (start < 0 || end >= comparison.getDifferences() || start > end) {
            logger.severe("Comparison.markGoodParts: Invalid range, aborting.");
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return good + " good, "
                + unsure + " unsure and "
                + undefined + " row(s) unchanged.";
    }
}