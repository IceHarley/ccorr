// Copyright © 2003-2006, 2010-2015, Esko Luontola <www.orfjackal.net>
// This software is released under the GNU General Public License, version 2 or later.
// The license text is at http://www.gnu.org/licenses/gpl.html

package net.orfjackal.ccorr.main;

import net.orfjackal.ccorr.combination.GoodCombinationWriter;
import net.orfjackal.ccorr.gui.MonitoredGoodCombinationWriter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

@Configuration
@ComponentScan(value={"net.orfjackal.ccorr.combination"})
public class DIConfiguration {
    @Bean
    public GoodCombinationWriter getGoodCombinationWriter(){
        return new MonitoredGoodCombinationWriter();
    }
}
