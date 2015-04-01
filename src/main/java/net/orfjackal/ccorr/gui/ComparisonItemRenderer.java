// Copyright © 2003-2006, 2010, Esko Luontola <www.orfjackal.net>
// This software is released under the GNU General Public License, version 2 or later.
// The license text is at http://www.gnu.org/licenses/gpl.html

package net.orfjackal.ccorr.gui;

import net.orfjackal.ccorr.ComparisonItem;

import javax.swing.*;
import javax.swing.table.TableCellRenderer;
import java.awt.*;

/**
 * A <code>TableCellRenderer</code> for rendering a <code>ComparisonItem</code> in a <code>JTable</code>.
 *
 * @author Esko Luontola
 */
class ComparisonItemRenderer extends JLabel implements TableCellRenderer {

    public static final Color COLOR_UNDEFINED = Color.white;
    public static final Color COLOR_GOOD = new Color(150, 255, 150);
    public static final Color COLOR_BAD = new Color(255, 150, 150);
    public static final Color COLOR_UNSURE = new Color(255, 255, 150);
    public static final Color COLOR_BORDER = new Color(128, 128, 196);

    /**
     * Creates a new instance of this class.
     */
    public ComparisonItemRenderer() {
        setOpaque(true);
        setHorizontalAlignment(JLabel.CENTER);
    }

    /**
     * Returns the component that represents a ComparisonItem cell.
     */
    public Component getTableCellRendererComponent(JTable table, Object value,
                                                   boolean isSelected, boolean hasFocus, int row, int column) {

        if (value instanceof ComparisonItem) {
            ComparisonItem item = (ComparisonItem) (value);
            setFont(new Font("Courier New", Font.PLAIN, 12));

            Color color;
            switch (item.getMark()) {
                default:
                case UNDEFINED:
                    color = COLOR_UNDEFINED;
                    break;

                case GOOD:
                    color = COLOR_GOOD;
                    break;

                case BAD:
                    color = COLOR_BAD;
                    break;

                case UNSURE:
                    color = COLOR_UNSURE;
                    break;
            }
            setBackground(color);

            if (hasFocus || isSelected) {
                this.setBorder(BorderFactory.createLineBorder(COLOR_BORDER, 1));
            } else {
                this.setBorder(null);
            }

        } else {
            // TODO: special cells?
        }

        return this;
    }
}