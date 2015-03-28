// Copyright © 2003-2006, 2010-2015, Esko Luontola <www.orfjackal.net>
// This software is released under the GNU General Public License, version 2 or later.
// The license text is at http://www.gnu.org/licenses/gpl.html

package net.orfjackal.ccorr;

import javax.swing.ProgressMonitor;
import java.io.IOException;
import java.io.OutputStream;

public class MonitoredGoodCombinationWriter extends GoodCombinationWriter {
    ProgressMonitor monitor = ProgressMonitorRepository.get();
    private int lastMonitorValue;
    private long progressValue;
    private long progressMax;

    public MonitoredGoodCombinationWriter(GoodCombination parts) {
        super(parts);
    }

    @Override
    public boolean writeCombination(OutputStream output) {
        setupProgressMonitor();
        boolean result = super.writeCombination(output);
        closeProgressMonitor();
        return result;
    }

    private void setupProgressMonitor() {
        if (monitor != null) {
            monitor.setMinimum(0);
            monitor.setMaximum(100);
            progressMax = getOutputFilesTotalLength();
            lastMonitorValue = -1;
            progressValue = 0;
        }
    }

    private void closeProgressMonitor() {
        if (monitor != null) {
            monitor.setProgress(monitor.getMaximum());
        }
    }

    @Override
    protected void copyData(GoodCombinationPart item) throws IOException {
        super.copyData(item);
    }

    @Override
    protected boolean copyBuffer(int bufferLength) throws IOException {
        boolean result = super.copyBuffer(bufferLength);
        updateProgressMonitor(bufferLength);
        return result;
    }

    private void updateProgressMonitor(long progressValueIncrement) throws IOException {
        if (monitor != null) {
            progressValue += progressValueIncrement;
            int percentage = (int) (progressValue * 100 / progressMax);
            if (percentage != lastMonitorValue) {
                monitor.setProgress(percentage);
                monitor.setNote("Completed " + percentage + "%");
                lastMonitorValue = percentage;
                Thread.yield(); // allow the GUI some time to be updated
            }

            if (monitor.isCanceled()) {
                cancel();
            }
        }
    }
}
