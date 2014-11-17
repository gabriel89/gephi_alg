package org.gephi.statistics.plugin.builder;

import org.gephi.statistics.plugin.EigenvectorCentrality;
import org.gephi.statistics.plugin.Hits;
import org.gephi.statistics.plugin.Sociability;
import org.gephi.statistics.spi.Statistics;
import org.gephi.statistics.spi.StatisticsBuilder;
import org.openide.util.NbBundle;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author pjmcswee
 */
@ServiceProvider(service = StatisticsBuilder.class)
public class SociabilityBuilder implements StatisticsBuilder {

    public String getName() {
        return NbBundle.getMessage(Sociability.class, "Sociability.name");
    }

    public Statistics getStatistics() {
        return new Sociability();
    }

    public Class<? extends Statistics> getStatisticsClass() {
        return Sociability.class;
    }
}
