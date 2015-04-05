// Copyright © 2003-2006, 2010-2015, Esko Luontola <www.orfjackal.net>
// This software is released under the GNU General Public License, version 2 or later.
// The license text is at http://www.gnu.org/licenses/gpl.html

package net.orfjackal.ccorr.combination;

import net.orfjackal.ccorr.main.DIConfiguration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.stereotype.Component;

@Component
public class GoodCombinationWriterFactory {
    private GoodCombinationWriter writer;

    @Autowired
    public GoodCombinationWriterFactory(GoodCombinationWriter writer) {
        this.writer = writer;
    }

    public GoodCombinationWriter initialize(GoodCombination gc) {
        writer.setParts(gc);
        return writer;
    }

    public static GoodCombinationWriter make(GoodCombination gc) {
        AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext(DIConfiguration.class);
        GoodCombinationWriterFactory factory = context.getBean(GoodCombinationWriterFactory.class);
        GoodCombinationWriter writer = factory.initialize(gc);
        context.close();
        return writer;
    }
}
