// Copyright © 2003-2006, 2010-2015, Esko Luontola <www.orfjackal.net>
// This software is released under the GNU General Public License, version 2 or later.
// The license text is at http://www.gnu.org/licenses/gpl.html

package net.orfjackal.ccorr.combination;

public class GoodCombinationPart {
    private Integer streamIndex;
    private Long startOffset;
    private Long endOffset;

    public GoodCombinationPart(Integer streamIndex, Long startOffset, Long endOffset) {
        this.streamIndex = streamIndex;
        this.startOffset = startOffset;
        this.endOffset = endOffset;
    }

    public Integer getStreamIndex() {
        return streamIndex;
    }

    public Long getStartOffset() {
        return startOffset;
    }

    public Long getEndOffset() {
        return endOffset;
    }

    boolean isValid() {
        return streamIndex >= 0 && startOffset >= 0 && endOffset > startOffset;
    }

    @Override
    public String toString() {
        return String.format("[StreamIndex: %d startOffset: %d endOffset: %d]", streamIndex, startOffset, endOffset);
    }
}