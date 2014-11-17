package org.gephi.statistics.plugin.builder.social;

import org.gephi.statistics.plugin.social.Socialize;
import org.gephi.statistics.spi.Statistics;
import org.gephi.statistics.spi.StatisticsBuilder;
import org.openide.util.NbBundle;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author Alexandru Topirceanu
 */
@ServiceProvider(service = StatisticsBuilder.class)
public class SocializeBuilder implements StatisticsBuilder {

    public String getName() {
        return NbBundle.getMessage(SocializeBuilder.class, "Socialize.name");
    }

    public Statistics getStatistics() {
        return new Socialize();
    }

    public Class<? extends Statistics> getStatisticsClass() {
        return Socialize.class;
    }
}
