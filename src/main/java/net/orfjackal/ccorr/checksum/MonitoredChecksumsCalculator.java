// Copyright © 2003-2006, 2010-2015, Esko Luontola <www.orfjackal.net>
// This software is released under the GNU General Public License, version 2 or later.
// The license text is at http://www.gnu.org/licenses/gpl.html

package net.orfjackal.ccorr.checksum;

import net.orfjackal.ccorr.crc.Buffer;
import net.orfjackal.ccorr.crc.CRC;
import net.orfjackal.ccorr.gui.ProgressMonitorRepository;

import javax.swing.*;
import java.util.logging.Logger;

public class MonitoredChecksumsCalculator extends FileChecksumsCalculator {
    private final static Logger logger = Logger.getLogger(MonitoredChecksumsCalculator.class.getName());

    private ProgressMonitor monitor;
    private int lastMonitorValue;
    private int currentPart;

    public MonitoredChecksumsCalculator(FileDivider fileDivider, CRC crc) {
        super(fileDivider, crc);
        this.monitor = ProgressMonitorRepository.get();
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

    @Override
    protected String calculateChecksum(Buffer buffer) {
        try {
            lastMonitorValue = updateProgressMonitor(monitor, lastMonitorValue, currentPart * 100 / getPartsCount());
        } catch (Exception e) {
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
                logger.info("updateChecksums: Cancelled by user");
                throw new Exception("Cancelled by user");
            }
        }
        return lastMonitorValue;
    }

    private void closeProcessMonitor() {
        if (monitor != null) {
            monitor.setProgress(monitor.getMaximum());
        }
    }
}
