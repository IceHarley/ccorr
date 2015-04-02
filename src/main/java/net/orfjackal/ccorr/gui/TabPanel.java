// Copyright © 2003-2006, 2010, Esko Luontola <www.orfjackal.net>
// This software is released under the GNU General Public License, version 2 or later.
// The license text is at http://www.gnu.org/licenses/gpl.html

package net.orfjackal.ccorr.gui;

import javax.swing.*;
import java.util.logging.*;

/**
 * The common properties of a <code>JTabbedPane</code> item.
 *
 * @author Esko Luontola
 */
public abstract class TabPanel extends JPanel {
    private final static Logger logger = Logger.getLogger(TabPanel.class.getName());

    /**
     * Removes this object from the JTabbedPane. Override this method if you need to do something before the tab is
     * removed or you want to cancel closing the tab.
     *
     * @return true if this.getParent() is a JTabbedPane, otherwise false.
     */
    public boolean close() {
        if (this.getParent() instanceof JTabbedPane) {
            JTabbedPane parent = (JTabbedPane) (this.getParent());
            parent.remove(this);
            return true;
        } else {
            logger.log(Level.INFO, "TabPanel.close(): Aborted, parent of {0} is {1}", new Object[]{this, this.getParent()});
            return false;
        }
    }

}