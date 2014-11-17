package org.gephi.statistics.plugin.builder;

import org.gephi.statistics.plugin.WSNOptimizer;
import org.gephi.statistics.spi.Statistics;
import org.gephi.statistics.spi.StatisticsBuilder;
import org.openide.util.NbBundle;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author Alexandru Topirceanu
 */
@ServiceProvider(service = StatisticsBuilder.class)
public class WSNOptimizerBuilder implements StatisticsBuilder {

    public String getName() {
        return NbBundle.getMessage(WSNOptimizerBuilder.class, "WSNOptimizer.name");
    }

    public Statistics getStatistics() {
        return new WSNOptimizer();
    }

    public Class<? extends Statistics> getStatisticsClass() {
        return WSNOptimizer.class;
    }
}
