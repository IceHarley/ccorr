// Copyright © 2003-2006, 2010-2015, Esko Luontola <www.orfjackal.net>
// This software is released under the GNU General Public License, version 2 or later.
// The license text is at http://www.gnu.org/licenses/gpl.html

package net.orfjackal.ccorr.gui;

import javax.swing.*;

public class ProgressMonitorRepository {
    public static ProgressMonitor progressMonitor = null;

    public static void set(ProgressMonitor monitor) {
        progressMonitor = monitor;
    }

    public static ProgressMonitor get() {
        ProgressMonitor result = progressMonitor;
        progressMonitor = null;
        return result;
    }
}
