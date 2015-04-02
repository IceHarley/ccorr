// Copyright © 2003-2006, 2010-2015, Esko Luontola <www.orfjackal.net>
// This software is released under the GNU General Public License, version 2 or later.
// The license text is at http://www.gnu.org/licenses/gpl.html

package net.orfjackal.ccorr.checksum;

import java.io.Serializable;
import java.util.*;

public class ChecksumFiles implements Serializable, Iterable<ChecksumFile> {
    private List<ChecksumFile> files;

    public ChecksumFiles() {
        this.files = new ArrayList<ChecksumFile>();
    }

    public int findShorterFileParts(int index1, int index2) {
        int shortest = files.get(index1).getParts();
        if (files.get(index2).getParts() < shortest)
            shortest = files.get(index2).getParts();
        return shortest;
    }

    public boolean add(ChecksumFile file) {
        if (isValidFileToAdd(file) && !isAlreadyAdded(file)) {
            files.add(file);
            return true;
        }
        return false;
    }

    boolean isValidFileToAdd(ChecksumFile file) {
        return (file != null && hasSamePartLength(file))
                && hasSameAlgorithm(file);
    }

    boolean hasSamePartLength(ChecksumFile file) {
        return getPartLength() <= 0 || file.getPartLength() == getPartLength();
    }

    public long getPartLength() {
        if (size() == 0)
            return -1;
        return get(0).getPartLength();
    }

    boolean hasSameAlgorithm(ChecksumFile file) {
        return (getAlgorithm() == null || file.getAlgorithm().equals(getAlgorithm()));
    }

    public String getAlgorithm() {
        if (size() == 0)
            return null;
        return get(0).getAlgorithm();
    }

    public boolean remove(int index) {
        return isValidFileIndex(index) && files.remove(index) != null;
    }

    public boolean isValidFileIndex(int file) {
        return file >= 0 && file < size();
    }

    public ChecksumFile get(int index) {
        return files.get(index);
    }

    public int size() {
        return files.size();
    }

    public boolean arePartsEquals(int part, int file1, int file2) {
        String checksum1 = files.get(file1).getChecksum(part);
        String checksum2 = files.get(file2).getChecksum(part);
        return checksum1.equals(checksum2);
    }

    private boolean isAlreadyAdded(ChecksumFile file) {
        for (ChecksumFile f : this)
            if (file == f) return true;
        return false;
    }

    public boolean isPartPresentInBothFiles(int part, int index1, int index2) {
        return files.get(index1).partPresentInFile(part) && files.get(index2).partPresentInFile(part);
    }

    @Override
    public Iterator<ChecksumFile> iterator() {
        return files.iterator();
    }
}
