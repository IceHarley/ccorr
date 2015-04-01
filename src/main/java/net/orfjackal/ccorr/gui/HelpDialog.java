// Copyright © 2003-2006, 2010, Esko Luontola <www.orfjackal.net>
// This software is released under the GNU General Public License, version 2 or later.
// The license text is at http://www.gnu.org/licenses/gpl.html

package net.orfjackal.ccorr.gui;

import net.orfjackal.ccorr.Settings;

import javax.swing.*;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;

/**
 * Help dialog.
 *
 * @author Esko Luontola
 */
public class HelpDialog extends JDialog implements HyperlinkListener {

    private static int HELP_WIDTH = 500;

    private static HelpDialog openedDialog;

    private JEditorPane help;

    public HelpDialog(Frame parent) {
        super(parent);

        setTitle(Settings.APP_NAME_SHORT + " Help");
        setModal(false);
        setResizable(true);

        getContentPane().setLayout(new BorderLayout());
        getContentPane().add(createHelp(), "Center");
        getContentPane().add(createButtons(), "South");

        pack();
        positionWindows(parent, this);
        setVisible(true);

        if (openedDialog != null) {
            openedDialog.close();
        }
        openedDialog = this;
    }

    private void positionWindows(Window parent, Window child) {
        Rectangle p = parent.getBounds();
        Rectangle c = child.getBounds();
        Rectangle max = GraphicsEnvironment.getLocalGraphicsEnvironment().getMaximumWindowBounds();

        // right from parent
        child.setLocation(
                p.x + p.width,
                p.y
        );
        child.setSize(
                HELP_WIDTH,
                p.height
        );

        // check if outside the screen
        if (c.x + c.width >= max.width) {
            int moveX = max.width - (c.x + c.width);
            parent.setLocation(p.x + moveX, p.y);
            child.setLocation(c.x + moveX, p.y);
        }
    }

    private JComponent createHelp() {
        try {
            help = new JEditorPane(ClassLoader.getSystemResource("help/index.html"));
        } catch (IOException e) {
            e.printStackTrace();
            return new JLabel("Error in opening " + ClassLoader.getSystemResource("help/index.html"));
        }
        help.setEditable(false);
        help.addHyperlinkListener(this);

        return new JScrollPane(help);
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
        openedDialog = null;
    }

    public void hyperlinkUpdate(HyperlinkEvent e) {
        if (e.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
            try {
                help.setPage(e.getURL());
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
    }
}