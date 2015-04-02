// Copyright © 2003-2006, 2010-2015, Esko Luontola <www.orfjackal.net>
// This software is released under the GNU General Public License, version 2 or later.
// The license text is at http://www.gnu.org/licenses/gpl.html

package net.orfjackal.ccorr.comparison;

import java.util.Hashtable;
import java.util.logging.*;

public class GoodPartsMarker {
    private final static Logger logger = Logger.getLogger(GoodPartsMarker.class.getName());

    private int good;
    private int unsure;
    private int undefined;

    private final Comparison comparison;
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

    private boolean validateRange(int start, int end) {
        if (start < 0 || end >= comparison.getDifferences() || start > end) {
            logger.severe("Comparison.markGoodParts: Invalid range, aborting.");
            return false;
        }
        return true;
    }

    private void markParts(int diffFrom, int diffTo) {
        for (int difference = diffFrom; difference <= diffTo; difference++)
            markPart(difference);
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

    private void findFilesOccurrences(int difference) {
        for (int file = 0; file < comparison.getFilesCount(); file++) {
            if (markerIsAlreadySet(difference, file)) break;
            findFileOccurrences(difference, file);
        }
    }

    private void setMarkAccordingToOccurrences(int difference) {
        if (maxIndex >= 0)
            setMark(difference);
        else
            leaveUndefined();
    }

    private void setMark(int difference) {
        if (isUnsure) setUnsure(difference);
        else setGood(difference);
    }

    private void leaveUndefined() {
        undefined++;
    }

    private void setUnsure(int difference) {
        comparison.setMark(difference, maxIndex, Mark.UNSURE);
        unsure++;
    }

    private void setGood(int difference) {
        comparison.setMark(difference, maxIndex, Mark.GOOD);
        good++;
    }

    private boolean markerIsAlreadySet(int difference, int file) {
        if (comparison.getMark(difference, file) != Mark.UNDEFINED) {
            maxIndex = -1;
            return true;
        }
        return false;
    }

    private void findFileOccurrences(int difference, int file) {
        if (comparison.isGoodIndex(difference, file)) {
            String crc = comparison.getChecksum(difference, file);
            if (isPastTheEndOfFile(crc))
                return;

            addCrcOccurrence(file, crc);
        }
    }

    private void addCrcOccurrence(int file, String crc) {
        Integer counter = addOccurrence(crc);
        if (counter > max)
            crcIsRepetitionsLeader(file, counter);
        else if (counter == max)
            crcShareRepetitionLeadership();
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

    private void crcIsRepetitionsLeader(int file, Integer counter) {
        max = counter;
        maxIndex = file;
        isUnsure = false;
    }

    private void crcShareRepetitionLeadership() {
        isUnsure = true;
    }

    private boolean isPastTheEndOfFile(String crc) {
        return crc.length() == 0;
    }

    private void logStatistics() {
        logger.log(Level.INFO, "Comparison.markGoodParts: {0}", toString());
    }

    @Override
    public String toString() {
        return String.format("%d good, %d unsure and %d row(s) unchanged.", good, unsure, undefined);
    }
}