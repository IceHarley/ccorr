// Copyright © 2003-2006, 2010, Esko Luontola <www.orfjackal.net>
// This software is released under the GNU General Public License, version 2 or later.
// The license text is at http://www.gnu.org/licenses/gpl.html

package net.orfjackal.ccorr;

import org.junit.*;

import java.io.*;

import static net.orfjackal.ccorr.TestDataUtil.*;

/**
 * @author Esko Luontola
 */
public class ComparisonTest extends Assert {

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
    public void an_empty_comparison_has_no_files() {
        Comparison c = new Comparison();
        c.doCompare();

        assertEquals(0, c.getFilesCount());
        assertEquals(0, c.getDifferences());
    }

    @Test
    public void an_empty_comparison_has_its_algorithm_and_part_length_undefined() {
        Comparison c = new Comparison();
        c.doCompare();

        assertEquals(null, c.getAlgorithm());
        assertEquals(-1, c.getPartLength());
    }

    @Test
    public void its_algorithm_and_part_length_are_decided_by_the_first_file_added() throws IOException {
        Comparison c = new Comparison();
        c.addFile(util.createChecksumFile(PART_LENGTH * 2));
        c.doCompare();

        assertEquals(ALGORITHM, c.getAlgorithm());
        assertEquals(PART_LENGTH, c.getPartLength());
    }

    @Test
    public void its_name_can_be_changed() {
        Comparison c = new Comparison();
        assertEquals("", c.getName());
        c.setName("foo");
        assertEquals("foo", c.getName());
    }

    @Test
    public void files_can_be_added_and_removed_to_and_from_the_comparison() throws IOException {
        Comparison c = new Comparison();
        ChecksumFile cf1 = util.createChecksumFile(PART_LENGTH * 2);
        ChecksumFile cf2 = util.createChecksumFile(PART_LENGTH * 2);

        c.addFile(cf1);
        c.addFile(cf2);
        assertEquals(2, c.getFilesCount());
        assertEquals(cf1, c.getFile(0));
        assertEquals(cf2, c.getFile(1));

        c.removeFile(0);
        assertEquals(1, c.getFilesCount());
        assertEquals(cf2, c.getFile(0));

        c.removeFile(0);
        assertEquals(0, c.getFilesCount());
    }

    @Test
    public void files_with_different_part_length_can_not_be_added_to_the_comparison() throws IOException {
        Comparison c = new Comparison();
        File file1 = util.createDummyFile(PART_LENGTH * 2);
        File file2 = util.createDummyFile(PART_LENGTH * 2);

        ChecksumFile cf1 = ChecksumFileFactory.createChecksumFile(file1, PART_LENGTH, ALGORITHM);
        ChecksumFile cf2 = ChecksumFileFactory.createChecksumFile(file2, PART_LENGTH * 2, ALGORITHM);

        c.addFile(cf1);
        c.addFile(cf2);

        assertEquals(1, c.getFilesCount());
        assertEquals(cf1, c.getFile(0));
    }

    @Test
    public void files_with_different_algorithm_can_not_be_added_to_the_comparison() throws IOException {
        Comparison c = new Comparison();
        File file1 = util.createDummyFile(PART_LENGTH * 2);
        File file2 = util.createDummyFile(PART_LENGTH * 2);

        ChecksumFile cf1 = ChecksumFileFactory.createChecksumFile(file1, PART_LENGTH, "CRC-32");
        ChecksumFile cf2 = ChecksumFileFactory.createChecksumFile(file2, PART_LENGTH, "MD5");

        c.addFile(cf1);
        c.addFile(cf2);

        assertEquals(1, c.getFilesCount());
        assertEquals(cf1, c.getFile(0));
    }

    @Test
    public void when_files_are_identical_then_they_have_no_differences() throws IOException {
        Comparison c = new Comparison();
        c.addFile(util.createChecksumFile(PART_LENGTH * 2));
        c.addFile(util.createChecksumFile(PART_LENGTH * 2));
        c.doCompare();

        assertEquals(0, c.getDifferences());
        assertEquals(1.0, c.similarity.get(0, 1), 0.0001);
    }

    @Test
    public void when_files_differ_then_their_differences_are_reported() throws IOException {
        Comparison c = new Comparison();
        c.addFile(util.createChecksumFile(PART_LENGTH * 5, END_OFFSET_1));
        c.addFile(util.createChecksumFile(PART_LENGTH * 5, START_OFFSET_2));
        c.doCompare();

        assertEquals(2, c.getDifferences());
        assertEquals(0.6, c.similarity.get(0, 1), 0.0001);

        String notCorrupt = "B70B4C26";

        int difference = 0;
        assertEquals(1, c.getPart(difference));
        assertEquals(START_OFFSET_1, c.getStartOffset(difference));
        assertEquals(END_OFFSET_1, c.getEndOffset(difference));
        assertEquals("9A09A3AB", c.getChecksum(difference, 0));
        assertEquals(notCorrupt, c.getChecksum(difference, 1));

        difference = 1;
        assertEquals(2, c.getPart(difference));
        assertEquals(START_OFFSET_2, c.getStartOffset(difference));
        assertEquals(END_OFFSET_2, c.getEndOffset(difference));
        assertEquals(notCorrupt, c.getChecksum(difference, 0));
        assertEquals("0761E377", c.getChecksum(difference, 1));

        the_differences_and_marks_are_printed_on_screen(c);
    }

    /**
     * @see net.orfjackal.ccorr.gui.ComparisonTableModel#getValueAt
     * @see net.orfjackal.ccorr.gui.ComparisonItemRenderer
     */
    private void the_differences_and_marks_are_printed_on_screen(Comparison c) {
        for (int difference = 0; difference <= 1; difference++) {
            for (int file = 0; file <= 1; file++) {
                ComparisonItem ci = c.getItem(difference, file);
                assertEquals(c.getChecksum(difference, file), ci.getChecksum());
                assertEquals(c.getMark(difference, file), ci.getMark());
            }
        }
    }

    @Test
    public void when_many_files_are_corrupt_at_the_same_part_then_it_is_reported_only_once() throws IOException {
        Comparison c = new Comparison();
        c.addFile(util.createChecksumFile(PART_LENGTH * 2, START_OFFSET_1 + 1));
        c.addFile(util.createChecksumFile(PART_LENGTH * 2, START_OFFSET_1 + 2));
        c.addFile(util.createChecksumFile(PART_LENGTH * 2, START_OFFSET_1 + 3));
        c.doCompare();

        assertEquals(1, c.getDifferences());
    }

    @Test
    public void when_some_files_are_shorter_but_end_at_even_offsets_then_they_are_not_necessarily_different() throws IOException {
        // TODO: maybe this behaviour should be changed, so that the length of a file is always explicitly shown to the user and selection is possible
        Comparison c = new Comparison();
        c.addFile(util.createChecksumFile(PART_LENGTH * 3));
        c.addFile(util.createChecksumFile(PART_LENGTH * 4));
        c.addFile(util.createChecksumFile(PART_LENGTH * 2));
        c.doCompare();

        assertEquals(0, c.getDifferences());
        assertEquals(1.0, c.similarity.get(0, 1), 0.0001);
    }

    @Test
    public void when_files_are_of_different_length_then_the_ends_of_short_files_are_automatically_marked_bad() throws IOException {
        Comparison c = new Comparison();
        c.addFile(util.createChecksumFile(PART_LENGTH * 2));
        c.addFile(util.createChecksumFile(PART_LENGTH * 3 - 1));
        c.addFile(util.createChecksumFile(PART_LENGTH * 3));
        c.doCompare();

        assertEquals(1, c.getDifferences());

        int difference = 0;
        assertEquals("", c.getChecksum(difference, 0));
        assertEquals("B97A6DA7", c.getChecksum(difference, 1));
        assertEquals("B70B4C26", c.getChecksum(difference, 2));
        assertEquals(Mark.BAD, c.getMark(difference, 0));
        assertEquals(Mark.UNDEFINED, c.getMark(difference, 1));
        assertEquals(Mark.UNDEFINED, c.getMark(difference, 2));
    }

    @Test
    public void can_be_saved_to_a_file_and_loaded_from_it() throws IOException {
        Comparison original = new Comparison();
        original.setName("foo");
        original.addFile(util.createChecksumFile(PART_LENGTH * 2, START_OFFSET_0));
        original.addFile(util.createChecksumFile(PART_LENGTH * 2, START_OFFSET_1));
        original.doCompare();
        original.setMark(0, 0, Mark.BAD);

        File tmp = util.uniqueFile();
        ComparisonLoader originalLoader = new ComparisonLoader(original);
        originalLoader.saveToFile(tmp);
        ComparisonLoader loadedLoader = new ComparisonLoader(tmp);
        Comparison loaded = loadedLoader.getComparison();

        assertEquals(tmp, originalLoader.getSavedAsFile());
        assertEquals(tmp, loadedLoader.getSavedAsFile());

        assertEquals(original.getName(), loaded.getName());
        assertEquals(original.getFilesCount(), loaded.getFilesCount());
        assertEquals(original.getDifferences(), loaded.getDifferences());
        assertEquals(original.getChecksum(0, 0), original.getChecksum(0, 0));
        assertEquals(original.getChecksum(0, 1), original.getChecksum(0, 1));
        assertEquals(original.getMark(0, 0), original.getMark(0, 0));
        assertEquals(original.getMark(0, 1), original.getMark(0, 1));
    }
}
