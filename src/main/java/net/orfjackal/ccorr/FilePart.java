// Copyright © 2003-2006, 2010-2015, Esko Luontola <www.orfjackal.net>
// This software is released under the GNU General Public License, version 2 or later.
// The license text is at http://www.gnu.org/licenses/gpl.html

package net.orfjackal.ccorr;

import java.io.File;

public class FilePart {
    private File file;
    private Long startOffset;
    private Long endOffset;

    public FilePart(File file, Long startOffset, Long endOffset) {
        this.file = file;
        this.startOffset = startOffset;
        this.endOffset = endOffset;
    }

    public File getFile() {
        return file;
    }

    public Long getStartOffset() {
        return startOffset;
    }

    public Long getEndOffset() {
        return endOffset;
    }
}
