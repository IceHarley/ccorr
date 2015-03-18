// Copyright © 2003-2006, 2010-2015, Esko Luontola <www.orfjackal.net>
// This software is released under the GNU General Public License, version 2 or later.
// The license text is at http://www.gnu.org/licenses/gpl.html

package net.orfjackal.ccorr;

import javax.swing.*;

public class MonitoredChecksumsCalculator extends FileChecksumsCalculator {
    private ProgressMonitor monitor;
    private int lastMonitorValue;
    private int currentPart;

    public MonitoredChecksumsCalculator(FileDivider fileDivider, CRC crc) {
        super(fileDivider, crc);
        this.monitor = Settings.getProgressMonitor();
    }

    @Override
    public ChecksumFile calculateChecksums() {
        setupProgressMonitor();
        ChecksumFile checksumFile = super.calculateChecksums();
        closeProcessMonitor();
        return checksumFile;
    }

    private void setupProgressMonitor() {
        if (monitor != null) {
            monitor.setMinimum(0);
            monitor.setMaximum(100);
            lastMonitorValue = -1;
            currentPart = 0;
        }
    }

    private void closeProcessMonitor() {
        if (monitor != null) {
            monitor.setProgress(monitor.getMaximum());
        }
    }

    @Override
    protected String calculateChecksum(Buffer buffer) {
        try {
            lastMonitorValue = updateProgressMonitor(monitor, lastMonitorValue, currentPart * 100 / getPartsCount());
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        currentPart++;
        return super.calculateChecksum(buffer);
    }

    private int updateProgressMonitor(ProgressMonitor monitor, int lastMonitorValue, int percentage) throws Exception {
        if (monitor != null) {
            if (percentage != lastMonitorValue) {
                monitor.setProgress(percentage);
                monitor.setNote("Completed " + percentage + "%");
                lastMonitorValue = percentage;
                Thread.yield(); // allow the GUI some time to be updated
            }

            if (monitor.isCanceled()) {
                Log.print("updateChecksums: Cancelled by user");
                throw new Exception("Cancelled by user");
            }
        }
        return lastMonitorValue;
    }
}
