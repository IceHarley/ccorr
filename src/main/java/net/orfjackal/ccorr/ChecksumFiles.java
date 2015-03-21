// Copyright © 2003-2006, 2010-2015, Esko Luontola <www.orfjackal.net>
// This software is released under the GNU General Public License, version 2 or later.
// The license text is at http://www.gnu.org/licenses/gpl.html

package net.orfjackal.ccorr;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
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

    private void writeObject(ObjectOutputStream out) throws IOException {
        out.writeObject(files.toArray(new ChecksumFile[files.size()]));
    }

    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
        files = Arrays.asList((ChecksumFile[]) in.readObject());
    }

    public int size() {
        return files.size();
    }

    public ChecksumFile get(int index) {
        return files.get(index);
    }

    public void add(ChecksumFile file) {
        files.add(file);
    }

    @Override
    public Iterator<ChecksumFile> iterator() {
        return files.iterator();
    }

    public int indexOf(ChecksumFile file) {
        return files.indexOf(file);
    }

    public boolean remove(ChecksumFile file) {
        return files.remove(file);
    }

    public boolean arePartsEquals(int file1, int file2, int part) {
        String checksum1 = files.get(file1).getChecksum(part);
        String checksum2 = files.get(file2).getChecksum(part);
        return checksum1.equals(checksum2);
    }
}
