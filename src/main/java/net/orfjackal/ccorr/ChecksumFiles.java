// Copyright © 2003-2006, 2010-2015, Esko Luontola <www.orfjackal.net>
// This software is released under the GNU General Public License, version 2 or later.
// The license text is at http://www.gnu.org/licenses/gpl.html

package net.orfjackal.ccorr;

import java.io.*;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class ChecksumFiles implements Serializable, Iterable<ChecksumFile> {

    private List<ChecksumFile> files;

    public ChecksumFiles() {
        this.files = new ArrayList<ChecksumFile>();
    }

    public int findShorterFileParts(int index1, int index2) {
        int shortest = files.get(index1).getParts();
        if (files.get(index2).getParts() < shortest) {
            shortest = files.get(index2).getParts();
        }
        return shortest;
    }

    public int size() {
        return files.size();
    }

    public ChecksumFile get(int index) {
        return files.get(index);
    }

    public boolean add(ChecksumFile file) {
        if (isValidFileToAdd(file) && !isAlreadyAdded(file)) {
            files.add(file);
            return true;
        }
        return false;
    }

    @Override
    public Iterator<ChecksumFile> iterator() {
        return files.iterator();
    }

    public boolean remove(int index) {
        return isValidFileIndex(index) && files.remove(index) != null;
    }

    public boolean arePartsEquals(int file1, int file2, int part) {
        String checksum1 = files.get(file1).getChecksum(part);
        String checksum2 = files.get(file2).getChecksum(part);
        return checksum1.equals(checksum2);
    }

    public boolean isValidFileIndex(int file) {
        return file >= 0 && file < size();
    }

    public ChecksumFile getFile(int file) {
        if (!isValidFileIndex(file))
            throw new IllegalArgumentException();
        return get(file);
    }

    public long getPartLength() {
        if (size() == 0)
            return -1;
        return get(0).getPartLength();
    }

    public String getAlgorithm() {
        if (size() == 0)
            return null;
        return get(0).getAlgorithm();
    }

    boolean hasSameAlgorithm(ChecksumFile file) {
        return (getAlgorithm() == null || file.getAlgorithm().equals(getAlgorithm()));
    }

    boolean hasSamePartLength(ChecksumFile file) {
        return getPartLength() <= 0 || file.getPartLength() == getPartLength();
    }

    boolean isValidFileToAdd(ChecksumFile file) {
        return (file != null && hasSamePartLength(file))
                && hasSameAlgorithm(file);
    }

    private boolean isAlreadyAdded(ChecksumFile file) {
        for (ChecksumFile f : this)
            if (file == f) return true;
        return false;
    }
}
