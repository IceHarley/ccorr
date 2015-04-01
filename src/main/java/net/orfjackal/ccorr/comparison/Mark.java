// Copyright © 2003-2006, 2010-2015, Esko Luontola <www.orfjackal.net>
// This software is released under the GNU General Public License, version 2 or later.
// The license text is at http://www.gnu.org/licenses/gpl.html

package net.orfjackal.ccorr.comparison;

public enum Mark {
    NOT_EXISTS,
    NEXT,
    UNDEFINED,
    GOOD,
    BAD,
    UNSURE;

    public static Mark nextMark(Mark mark) {
        Mark result;
        switch (mark) {
            case UNDEFINED:
                result = GOOD;
                break;

            case GOOD:
                result = BAD;
                break;

            case BAD:
                result = UNSURE;
                break;

            case UNSURE:
            default:
                result = UNDEFINED;
                break;
        }
        return result;
    }
}
