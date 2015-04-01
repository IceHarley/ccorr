// Copyright © 2003-2006, 2010, Esko Luontola <www.orfjackal.net>
// This software is released under the GNU General Public License, version 2 or later.
// The license text is at http://www.gnu.org/licenses/gpl.html

package net.orfjackal.ccorr.gui;

import net.orfjackal.ccorr.settings.Settings;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * About dialog.
 *
 * @author Esko Luontola
 */
public class AboutDialog extends JDialog {

    public AboutDialog(Frame parent) {
        super(parent);

        setTitle("About " + Settings.APP_NAME_SHORT);
        setModal(true);
        setResizable(false);

        getContentPane().setLayout(new BorderLayout());
        getContentPane().add(createAbout(), "Center");
        getContentPane().add(createButtons(), "South");

        pack();
        positionWindows(parent, this);
        setVisible(true);
    }

    private void positionWindows(Window parent, Window child) {
        Rectangle p = parent.getBounds();
        Rectangle c = child.getBounds();

        // center on parent
        child.setLocation(
                p.x + (p.width / 2) - (c.width / 2),
                p.y + (p.height / 2) - (c.height / 2)
        );
    }

    private JComponent createAbout() {
        JPanel p = new JPanel();

        JTextArea licence = new JTextArea(shortGPL);
        licence.setEditable(false);
        licence.setBackground(p.getBackground());
        p.add(licence);

        return p;
    }

    private JComponent createButtons() {
        FlowLayout layout = new FlowLayout();
        layout.setAlignment(FlowLayout.RIGHT);
        JPanel p = new JPanel(layout);

        JButton button = new JButton("Close");
        button.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                close();
            }
        });
        p.add(button);

        return p;
    }

    private void close() {
        setVisible(false);
        dispose();
    }

    private static final String shortGPL
            = Settings.APP_NAME + " " + Settings.VERSION_NUMBER + "\n" +
            Settings.WEBSITE + "\n" +
            "\n" +
            Settings.COPYRIGHT + "\n" +
            "\n" +
            "This program is free software; you can redistribute it and/or modify\n" +
            "it under the terms of the GNU General Public License as published by\n" +
            "the Free Software Foundation; either version 2 of the License, or\n" +
            "(at your option) any later version.\n" +
            "\n" +
            "This program is distributed in the hope that it will be useful,\n" +
            "but WITHOUT ANY WARRANTY; without even the implied warranty of\n" +
            "MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the\n" +
            "GNU General Public License for more details.\n" +
            "\n" +
            "You should have received a copy of the GNU General Public License\n" +
            "along with this program; if not, write to the Free Software\n" +
            "Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA\n";

}