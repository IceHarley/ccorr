// Copyright © 2003-2006, 2010, Esko Luontola <www.orfjackal.net>
// This software is released under the GNU General Public License, version 2 or later.
// The license text is at http://www.gnu.org/licenses/gpl.html

package net.orfjackal.ccorr.combination;

import net.orfjackal.ccorr.comparison.Mark;
import net.orfjackal.ccorr.gui.ProgressMonitorRepository;
import net.orfjackal.ccorr.utils.TestDataUtil;
import net.orfjackal.ccorr.checksum.ChecksumFileFactory;
import net.orfjackal.ccorr.comparison.Comparison;
import org.junit.*;

import javax.swing.*;
import java.io.*;

import static net.orfjackal.ccorr.utils.TestDataUtil.*;
import static org.mockito.Mockito.*;

/**
 * @author Esko Luontola
 */
public class CombiningGoodPartsTest extends Assert {

    private static final int DIFF_0 = 0;
    private static final int DIFF_1 = 1;

    private TestDataUtil util = new TestDataUtil();
    private GoodCombination goodCombination;

    @Before
    public void initUtil() throws IOException {
        util.create();
        goodCombination = null;
    }

    @After
    public void disposeUtil() {
        try {
            if (goodCombination != null)
                goodCombination.closeStreams();
        } catch (IOException e) {
            e.printStackTrace();
        }
        util.dispose();
    }

    private void getGoodCombination(Comparison c) {
        goodCombination = Comparison.createGoodCombination(c);
    }

    private Comparison comparisonWithTwoDiffs() throws IOException {
        Comparison c = new Comparison();
        c.addFile(util.createChecksumFile(PART_LENGTH * 2, START_OFFSET_0));
        c.addFile(util.createChecksumFile(PART_LENGTH * 2, START_OFFSET_1));
        c.doCompare();
        assertEquals(2, c.getDifferences());
        return c;
    }

    private Comparison comparisonWithTwoDiffsMarkedGood() throws IOException {
        Comparison c = comparisonWithTwoDiffs();
        c.setMark(DIFF_0, 1, Mark.GOOD);
        c.setMark(DIFF_1, 0, Mark.GOOD);
        return c;
    }

    @Test
    public void when_there_are_no_files_then_a_good_combination_cannot_be_created() {
        Comparison c = new Comparison();
        c.doCompare();

        getGoodCombination(c);
        assertEquals(GoodCombination.NOT_EXISTS, goodCombination);
    }

    @Test
    public void when_there_is_only_one_file_then_a_good_combination_cannot_be_created() throws IOException {
        // TODO: maybe this behaviour should be changed, so that it's possible to extract a good combination
        Comparison c = new Comparison();
        c.addFile(util.createChecksumFile(PART_LENGTH * 2));
        c.doCompare();

        getGoodCombination(c);
        assertEquals(GoodCombination.NOT_EXISTS, goodCombination);
    }

    @Test
    public void when_no_differences_are_marked_good_then_a_good_combination_cannot_be_created() throws IOException {
        Comparison c = comparisonWithTwoDiffs();

        getGoodCombination(c);
        assertEquals(GoodCombination.NOT_EXISTS, goodCombination);
    }

    @Test
    public void when_not_all_differences_are_marked_good_then_a_good_combination_cannot_be_created() throws IOException {
        Comparison c = comparisonWithTwoDiffs();

        c.setMark(DIFF_0, 0, Mark.BAD);
        c.setMark(DIFF_0, 1, Mark.GOOD);

        getGoodCombination(c);
        assertEquals(GoodCombination.NOT_EXISTS, goodCombination);
    }

    @Test
    public void when_each_difference_has_one_part_marked_good_then_a_good_combination_can_be_created() throws IOException {
        Comparison c = comparisonWithTwoDiffsMarkedGood();

        getGoodCombination(c);
        assertNotSame(GoodCombination.NOT_EXISTS, goodCombination);
    }

    @Test
    public void when_a_good_combination_is_written_then_it_contains_all_good_parts() throws IOException {
        Comparison c = comparisonWithTwoDiffsMarkedGood();

        File file = util.uniqueFile();
        BufferedOutputStream result = StreamFactory.openOutputStream(file);
        getGoodCombination(c);
        new GoodCombinationWriter(goodCombination).writeCombination(result);

        c.addFile(ChecksumFileFactory.createChecksumFile(file, PART_LENGTH, ALGORITHM));
        c.doCompare();
        assertEquals(Mark.GOOD, c.getMark(DIFF_0, 2));
        assertEquals(Mark.GOOD, c.getMark(DIFF_1, 2));
    }

    @Test
    public void when_a_good_combination_is_written_then_the_progress_monitor_is_notified() throws IOException {
        Comparison c = comparisonWithTwoDiffsMarkedGood();

        ProgressMonitor monitor = spy(new ProgressMonitor(null, null, null, 0, 0));
        ProgressMonitorRepository.set(monitor);

        getGoodCombination(c);
        new MonitoredGoodCombinationWriter(goodCombination).writeCombination(new ByteArrayOutputStream());

        verify(monitor).setMinimum(0);
        verify(monitor).setMaximum(100);

        verify(monitor).setProgress(50);
        verify(monitor).setNote("Completed 50%");
        verify(monitor, atLeastOnce()).setProgress(100);
    }
}
