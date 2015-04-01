// Copyright © 2003-2006, 2010, Esko Luontola <www.orfjackal.net>
// This software is released under the GNU General Public License, version 2 or later.
// The license text is at http://www.gnu.org/licenses/gpl.html

package net.orfjackal.ccorr.checksum;

import net.orfjackal.ccorr.gui.ProgressMonitorRepository;
import net.orfjackal.ccorr.utils.TestDataUtil;
import org.junit.*;

import javax.swing.*;
import java.io.*;

import static net.orfjackal.ccorr.utils.TestDataUtil.*;
import static org.mockito.Mockito.*;

/**
 * @author Esko Luontola
 */
public class ChecksumFileTest extends Assert {

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
    public void calculates_the_checksum_for_all_parts() throws IOException {
        int parts = 2;
        ChecksumFile cf = util.createChecksumFile(PART_LENGTH * parts);

        assertEquals(parts, cf.getParts());

        assertEquals("B70B4C26", cf.getChecksum(0));
        assertEquals(START_OFFSET_0, cf.getStartOffset(0));
        assertEquals(END_OFFSET_0, cf.getEndOffset(0));

        assertEquals("B70B4C26", cf.getChecksum(1));
        assertEquals(START_OFFSET_1, cf.getStartOffset(1));
        assertEquals(END_OFFSET_1, cf.getEndOffset(1));
    }

    @Test
    public void last_part_can_be_shorter() throws IOException {
        int parts = 2;
        int shorter = 100;
        ChecksumFile cf = util.createChecksumFile(PART_LENGTH * parts - shorter);

        assertEquals(parts, cf.getParts());
        assertEquals("B70B4C26", cf.getChecksum(0));
        assertEquals(START_OFFSET_0, cf.getStartOffset(0));
        assertEquals(END_OFFSET_0, cf.getEndOffset(0));

        assertEquals("294D3872", cf.getChecksum(1));
        assertEquals(START_OFFSET_1, cf.getStartOffset(1));
        assertEquals(END_OFFSET_1 - shorter, cf.getEndOffset(1));
    }

    @Test
    public void trying_to_get_a_part_from_too_high_index_returns_an_error_value() throws IOException {
        int parts = 2;
        int tooHighIndex = 2;
        ChecksumFile cf = util.createChecksumFile(PART_LENGTH * parts);

        assertTrue(cf.getParts() >= parts);
        assertEquals(null, cf.getChecksum(tooHighIndex));
        assertEquals(-1, cf.getStartOffset(tooHighIndex));
        assertEquals(-1, cf.getEndOffset(tooHighIndex));
    }

    @Test
    public void calculating_the_checksums_can_be_monitored_by_a_progress_bar() throws IOException {
        ProgressMonitor monitor = spy(new ProgressMonitor(null, null, null, 0, 0));

        ProgressMonitorRepository.set(monitor);
        util.createChecksumFile(PART_LENGTH * 4);

        verify(monitor).setMinimum(0);
        verify(monitor).setMaximum(100);

        verify(monitor).setProgress(0);
        verify(monitor).setNote("Completed 0%");
        verify(monitor).setProgress(25);
        verify(monitor).setNote("Completed 25%");
        verify(monitor).setProgress(50);
        verify(monitor).setNote("Completed 50%");
        verify(monitor).setProgress(75);
        verify(monitor).setNote("Completed 75%");
        verify(monitor, atLeastOnce()).setProgress(100);
    }

    @Test
    public void can_be_saved_to_a_file_and_loaded_from_it() throws IOException {
        ChecksumFile original = util.createChecksumFile(PART_LENGTH * 2);

        File tmp = util.uniqueFile();
        new ChecksumFileLoader(original).saveToFile(tmp);
        ChecksumFile loaded = ChecksumFileLoader.loadFromFile(tmp);

        assertEquals(original.getParts(), loaded.getParts());
        assertEquals(original.getChecksum(0), loaded.getChecksum(0));
        assertEquals(original.getAlgorithm(), loaded.getAlgorithm());
        assertEquals(original.getPartLength(), loaded.getPartLength());
        assertEquals(original.getSourceFile(), loaded.getSourceFile());
        assertEquals(original.getSourceFileLength(), loaded.getSourceFileLength());
    }

    @Test
    public void the_source_file_can_be_relocated_if_the_file_size_is_the_same() throws IOException {
        int length = PART_LENGTH * 2;
        ChecksumFile cf = util.createChecksumFile(length);
        File moved = util.createDummyFile(length);

        ChecksumFile cfMoved = cf.setSourceFile(moved);

        assertEquals(moved, cfMoved.getSourceFile());
    }

    @Test
    public void the_source_file_can_not_be_relocated_if_the_file_size_is_not_the_same() throws IOException {
        int length = PART_LENGTH * 2;
        ChecksumFile cf = util.createChecksumFile(length);
        File original = cf.getSourceFile();
        File moved = util.createDummyFile(length - 1);

        ChecksumFile cfMoved = cf.setSourceFile(moved);

        assertEquals(original, cfMoved.getSourceFile());
    }
}
