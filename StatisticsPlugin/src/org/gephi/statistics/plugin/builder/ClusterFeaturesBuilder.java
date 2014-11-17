package org.gephi.statistics.plugin.builder;

import org.gephi.statistics.plugin.ClusterFeatures;
import org.gephi.statistics.plugin.Modularity;
import org.gephi.statistics.spi.Statistics;
import org.gephi.statistics.spi.StatisticsBuilder;
import org.openide.util.NbBundle;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author Alexandru Topirceanu
 */
@ServiceProvider(service = StatisticsBuilder.class)
public class ClusterFeaturesBuilder implements StatisticsBuilder {

    public String getName() {
        return NbBundle.getMessage(ClusterFeaturesBuilder.class, "ClusterFeatures.name");
    }

    public Statistics getStatistics() {
        return new ClusterFeatures();
    }

    public Class<? extends Statistics> getStatisticsClass() {
        return ClusterFeatures.class;
    }
}
