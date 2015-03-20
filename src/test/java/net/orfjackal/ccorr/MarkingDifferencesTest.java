// Copyright © 2003-2006, 2010, Esko Luontola <www.orfjackal.net>
// This software is released under the GNU General Public License, version 2 or later.
// The license text is at http://www.gnu.org/licenses/gpl.html

package net.orfjackal.ccorr;

import org.junit.*;

import java.io.IOException;

import static net.orfjackal.ccorr.TestDataUtil.*;

/**
 * @author Esko Luontola
 */
public class MarkingDifferencesTest extends Assert {

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

    @Test
    public void differences_can_be_marked_manually() throws IOException {
        Comparison c = new Comparison();
        c.addFile(util.createChecksumFile(PART_LENGTH * 2, START_OFFSET_0));
        c.addFile(util.createChecksumFile(PART_LENGTH * 2));
        c.doCompare();

        assertEquals(Mark.UNDEFINED, c.getMark(DIFF_0, 0));
        assertEquals(Mark.UNDEFINED, c.getMark(DIFF_0, 1));

        c.setMark(DIFF_0, 0, Mark.BAD);
        assertEquals(Mark.BAD, c.getMark(DIFF_0, 0));
        assertEquals(Mark.UNDEFINED, c.getMark(DIFF_0, 1));

        c.nextMark(DIFF_0, 1);
        assertEquals(Mark.GOOD, c.getMark(DIFF_0, 1));
        c.nextMark(DIFF_0, 1);
        assertEquals(Mark.BAD, c.getMark(DIFF_0, 1));
        c.nextMark(DIFF_0, 1);
        assertEquals(Mark.UNSURE, c.getMark(DIFF_0, 1));
        c.nextMark(DIFF_0, 1);
        assertEquals(Mark.UNDEFINED, c.getMark(DIFF_0, 1));
    }

    @Test
    public void when_files_with_known_checksums_are_added_then_old_marks_are_mirrored_in_new_files() throws IOException {
        Comparison c = new Comparison();
        c.addFile(util.createChecksumFile(PART_LENGTH * 2, START_OFFSET_0));
        c.addFile(util.createChecksumFile(PART_LENGTH * 2, START_OFFSET_1));
        c.doCompare();

        c.setMark(DIFF_0, 0, Mark.BAD);
        c.setMark(DIFF_0, 1, Mark.GOOD);
        c.setMark(DIFF_1, 0, Mark.GOOD);
        c.setMark(DIFF_1, 1, Mark.BAD);

        c.addFile(util.createChecksumFile(PART_LENGTH * 2, START_OFFSET_0));
        c.doCompare();

        assertEquals(Mark.BAD, c.getMark(DIFF_0, 0));
        assertEquals(Mark.GOOD, c.getMark(DIFF_0, 1));
        assertEquals(Mark.BAD, c.getMark(DIFF_0, 2));

        assertEquals(Mark.GOOD, c.getMark(DIFF_1, 0));
        assertEquals(Mark.BAD, c.getMark(DIFF_1, 1));
        assertEquals(Mark.GOOD, c.getMark(DIFF_1, 2));
    }

    @Test
    public void when_files_with_new_checksums_are_added_then_old_marks_are_not_mirrored() throws IOException {
        Comparison c = new Comparison();
        c.addFile(util.createChecksumFile(PART_LENGTH * 2, START_OFFSET_0));
        c.addFile(util.createChecksumFile(PART_LENGTH * 2, START_OFFSET_1));
        c.doCompare();

        c.setMark(DIFF_0, 0, Mark.BAD);
        c.setMark(DIFF_0, 1, Mark.GOOD);
        c.setMark(DIFF_1, 0, Mark.GOOD);
        c.setMark(DIFF_1, 1, Mark.BAD);

        c.addFile(util.createChecksumFile(PART_LENGTH * 2, START_OFFSET_0 + 1, START_OFFSET_1 + 1));
        c.doCompare();

        assertEquals(Mark.BAD, c.getMark(DIFF_0, 0));
        assertEquals(Mark.GOOD, c.getMark(DIFF_0, 1));
        assertEquals(Mark.UNDEFINED, c.getMark(DIFF_0, 2));

        assertEquals(Mark.GOOD, c.getMark(DIFF_1, 0));
        assertEquals(Mark.BAD, c.getMark(DIFF_1, 1));
        assertEquals(Mark.UNDEFINED, c.getMark(DIFF_1, 2));
    }

    @Test
    public void the_program_can_guess_that_which_parts_are_good_based_on_their_popularity() throws IOException {
        Comparison c = new Comparison();
        c.addFile(util.createChecksumFile(PART_LENGTH * 2, START_OFFSET_0));
        c.addFile(util.createChecksumFile(PART_LENGTH * 2));
        c.addFile(util.createChecksumFile(PART_LENGTH * 2, START_OFFSET_1));
        c.doCompare();

        c.markGoodParts(0, c.getDifferences() - 1);

        assertEquals(Mark.UNDEFINED, c.getMark(DIFF_0, 0));
        assertEquals(Mark.GOOD, c.getMark(DIFF_0, 1));
        assertEquals(Mark.GOOD, c.getMark(DIFF_0, 2));

        assertEquals(Mark.GOOD, c.getMark(DIFF_1, 0));
        assertEquals(Mark.GOOD, c.getMark(DIFF_1, 1));
        assertEquals(Mark.UNDEFINED, c.getMark(DIFF_1, 2));
    }

    @Test
    public void guessing_will_be_unsure_and_not_change_the_marks_if_there_is_no_clear_winner() throws IOException {
        Comparison c = new Comparison();
        c.addFile(util.createChecksumFile(PART_LENGTH * 2, START_OFFSET_0));
        c.addFile(util.createChecksumFile(PART_LENGTH * 2, START_OFFSET_0));
        c.addFile(util.createChecksumFile(PART_LENGTH * 2));
        c.addFile(util.createChecksumFile(PART_LENGTH * 2));
        c.doCompare();

        c.markGoodParts(0, c.getDifferences() - 1);

        assertEquals(Mark.UNSURE, c.getMark(DIFF_0, 0));
        assertEquals(Mark.UNSURE, c.getMark(DIFF_0, 1));
        assertEquals(Mark.UNDEFINED, c.getMark(DIFF_0, 2));
        assertEquals(Mark.UNDEFINED, c.getMark(DIFF_0, 3));
    }

    @Test
    public void guessing_will_not_change_already_set_marks() throws IOException {
        Comparison c = new Comparison();
        c.addFile(util.createChecksumFile(PART_LENGTH * 2, START_OFFSET_0));
        c.addFile(util.createChecksumFile(PART_LENGTH * 2));
        c.addFile(util.createChecksumFile(PART_LENGTH * 2));
        c.doCompare();

        c.setMark(DIFF_0, 0, Mark.GOOD);
        c.setMark(DIFF_0, 1, Mark.BAD);

        c.markGoodParts(0, c.getDifferences() - 1);

        assertEquals(Mark.GOOD, c.getMark(DIFF_0, 0));
        assertEquals(Mark.BAD, c.getMark(DIFF_0, 1));
        assertEquals(Mark.BAD, c.getMark(DIFF_0, 2));
    }

    @Test
    public void marks_can_be_cleared() throws IOException {
        Comparison c = new Comparison();
        c.addFile(util.createChecksumFile(PART_LENGTH * 2, START_OFFSET_0));
        c.addFile(util.createChecksumFile(PART_LENGTH * 2, START_OFFSET_1));
        c.doCompare();

        c.setMark(DIFF_0, 0, Mark.GOOD);
        c.setMark(DIFF_1, 0, Mark.BAD);

        c.markRowUndefined(0, c.getDifferences() - 1);

        assertEquals(Mark.UNDEFINED, c.getMark(DIFF_0, 0));
        assertEquals(Mark.UNDEFINED, c.getMark(DIFF_1, 0));
    }

    // TODO: tests for the marking of multiple parts with one command (if they have previously the same mark)
    // See for example:
    // net.orfjackal.ccorr.gui.ComparisonTableModel.setMarks()
    // net.orfjackal.ccorr.gui.ComparisonPanel.markSelectedGood()
    // net.orfjackal.ccorr.gui.ComparisonPanel.markSelectedNext()
}
