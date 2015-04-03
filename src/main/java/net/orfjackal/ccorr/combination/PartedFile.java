// Copyright © 2003-2006, 2010-2015, Esko Luontola <www.orfjackal.net>
// This software is released under the GNU General Public License, version 2 or later.
// The license text is at http://www.gnu.org/licenses/gpl.html

package net.orfjackal.ccorr.combination;

import java.io.File;

public interface PartedFile {
    long getStartOffsetOfPart(int part);

    long getEndOffset(int part);

    File getSourceFile();

    long getSourceFileLength();
}
