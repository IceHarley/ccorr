// Copyright © 2003-2006, 2010, Esko Luontola <www.orfjackal.net>
// This software is released under the GNU General Public License, version 2 or later.
// The license text is at http://www.gnu.org/licenses/gpl.html

package net.orfjackal.ccorr;

import org.junit.*;

import javax.swing.*;
import java.io.*;

import static net.orfjackal.ccorr.TestDataUtil.*;
import static org.mockito.Mockito.*;

/**
 * @author Esko Luontola
 */
public class CombiningGoodPartsTest extends Assert {

    private static final int DIFF_0 = 0;
    private static final int DIFF_1 = 1;

    private TestDataUtil util = new TestDataUtil();

    @Before
    public void initUtil() throws IOException {
        util.create();
    }

    @After
    public void disposeUtil() {
        util.dispose();
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

        assertNull(c.createGoodCombination());
    }

    @Test
    public void when_there_is_only_one_file_then_a_good_combination_cannot_be_created() throws IOException {
        // TODO: maybe this behaviour should be changed, so that it's possible to create a good combination
        Comparison c = new Comparison();
        c.addFile(util.createChecksumFile(PART_LENGTH * 2));
        c.doCompare();

        assertNull(c.createGoodCombination());
    }

    @Test
    public void when_no_differences_are_marked_good_then_a_good_combination_cannot_be_created() throws IOException {
        Comparison c = comparisonWithTwoDiffs();

        assertNull(c.createGoodCombination());
    }

    @Test
    public void when_not_all_differences_are_marked_good_then_a_good_combination_cannot_be_created() throws IOException {
        Comparison c = comparisonWithTwoDiffs();

        c.setMark(DIFF_0, 0, Mark.BAD);
        c.setMark(DIFF_0, 1, Mark.GOOD);

        assertNull(c.createGoodCombination());
    }

    @Test
    public void when_each_difference_has_one_part_marked_good_then_a_good_combination_can_be_created() throws IOException {
        Comparison c = comparisonWithTwoDiffsMarkedGood();

        assertNotNull(c.createGoodCombination());
    }

    @Test
    public void when_a_good_combination_is_written_then_it_contains_all_good_parts() throws IOException {
        Comparison c = comparisonWithTwoDiffsMarkedGood();

        File result = util.uniqueFile();
        c.createGoodCombination().writeFile(result);

        c.addFile(ChecksumFileFactory.createChecksumFile(result, PART_LENGTH, ALGORITHM));
        c.doCompare();
        assertEquals(Mark.GOOD, c.getMark(DIFF_0, 2));
        assertEquals(Mark.GOOD, c.getMark(DIFF_1, 2));
    }

    @Test
    public void when_a_good_combination_is_written_then_the_progress_monitor_is_notified() throws IOException {
        Comparison c = comparisonWithTwoDiffsMarkedGood();

        ProgressMonitor monitor = spy(new ProgressMonitor(null, null, null, 0, 0));
        ProgressMonitorRepository.set(monitor);

        File result = util.uniqueFile();
        c.createGoodCombination().writeFile(result);

        verify(monitor).setMinimum(0);
        verify(monitor).setMaximum(100);

        verify(monitor).setProgress(50);
        verify(monitor).setNote("Completed 50%");
        verify(monitor, atLeastOnce()).setProgress(100);
    }
}
