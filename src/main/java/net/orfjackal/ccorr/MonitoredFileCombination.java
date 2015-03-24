//// Copyright © 2003-2006, 2010-2015, Esko Luontola <www.orfjackal.net>
//// This software is released under the GNU General Public License, version 2 or later.
//// The license text is at http://www.gnu.org/licenses/gpl.html
//
//package net.orfjackal.ccorr;
//
//import javax.swing.*;
//import java.io.BufferedOutputStream;
//import java.io.File;
//import java.io.InputStream;
//import java.io.OutputStream;
//
//public class MonitoredFileCombination extends FileCombination {
//    ProgressMonitor monitor = ProgressMonitorRepository.get();
//
//    @Override
//    public boolean writeFile(File file) {
//        setupProgressMonitor();
//        boolean result = super.writeFile(file);
//        closeProgressMonitor();
//        return result;
//    }
//
//    private void setupProgressMonitor() {
//        if (monitor != null) {
//            monitor.setMinimum(0);
//            monitor.setMaximum(100);
//        }
//    }
//
//    private void closeProgressMonitor() {
//        if (monitor != null) {
//            monitor.setProgress(monitor.getMaximum());
//        }
//    }
//
//    @Override
//    protected long writeItem(long written, long fileLength, FilePart item) throws Exception {
//        return super.writeItem(output, written, fileLength, item);
//    }
//
//    @Override
//    private static long copyData(FilePart item,
//                                 InputStream input, OutputStream output,
//                                 byte[] buffer, ProgressMonitor monitor,
//                                 long progressValue, long progressMax) throws Exception {
//        int lastMonitorValue = -1;
//    }
//
//    private int updateProgressMonitor(long progressValue, long progressMax, int lastMonitorValue) throws Exception {
//        if (monitor != null) {
//            int percentage = (int) (progressValue * 100 / progressMax);
//            if (percentage != lastMonitorValue) {
//                monitor.setProgress(percentage);
//                monitor.setNote("Completed " + percentage + "%");
//                lastMonitorValue = percentage;
//                Thread.yield(); // allow the GUI some time to be updated
//            }
//
//            if (monitor.isCanceled()) {
//                cancel();
//            }
//        }
//        return lastMonitorValue;
//    }
//}
