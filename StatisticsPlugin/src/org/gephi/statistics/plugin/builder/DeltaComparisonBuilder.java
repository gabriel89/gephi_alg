package org.gephi.statistics.plugin.builder;

import org.gephi.statistics.plugin.DeltaComparison;
import org.gephi.statistics.spi.Statistics;
import org.gephi.statistics.spi.StatisticsBuilder;
import org.openide.util.NbBundle;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author Alexandru Topirceanu
 */
@ServiceProvider(service = StatisticsBuilder.class)
public class DeltaComparisonBuilder implements StatisticsBuilder {

    public String getName() {
        return NbBundle.getMessage(DeltaComparisonBuilder.class, "DeltaComparison.name");
    }

    public Statistics getStatistics() {
        return new DeltaComparison();
    }

    public Class<? extends Statistics> getStatisticsClass() {
        return DeltaComparison.class;
    }
}
