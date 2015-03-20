// Copyright © 2003-2006, 2010-2015, Esko Luontola <www.orfjackal.net>
// This software is released under the GNU General Public License, version 2 or later.
// The license text is at http://www.gnu.org/licenses/gpl.html

package net.orfjackal.ccorr;

import javax.swing.*;

public class ProgressMonitorRepository {
    /**
     * The <code>ProgressMonitor</code> for the next long task.
     */
    public static ProgressMonitor progressMonitor = null;

    /**
     * Sets the <code>ProgressMonitor</code> to be used by the next long task.
     *
     * @param monitor an unused <code>ProgressMonitor</code> for the next one who calls <code>get</code>,
     *                or null to remove it
     */
    public static void set(ProgressMonitor monitor) {
        progressMonitor = monitor;
    }

    /**
     * Returns the <code>ProgressMonitor</code> that was set with <code>set</code> after which a new one
     * must be set.
     *
     * @return the <code>ProgressMonitor</code> that was set, or null if one has not been set since
     *         <code>get</code> was called the last time
     */
    public static ProgressMonitor get() {
        ProgressMonitor result = progressMonitor;
        progressMonitor = null;
        return result;
    }
}
