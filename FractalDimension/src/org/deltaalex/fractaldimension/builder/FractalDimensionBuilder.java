package org.deltaalex.fractaldimension.builder;

import org.deltaalex.fractaldimension.plugin.FractalDimension;
import org.gephi.statistics.spi.Statistics;
import org.gephi.statistics.spi.StatisticsBuilder;
import org.openide.util.NbBundle;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author Alexandru Topirceanu
 */
@ServiceProvider(service = StatisticsBuilder.class)
public class FractalDimensionBuilder implements StatisticsBuilder {

    public String getName() {
        return NbBundle.getMessage(FractalDimensionBuilder.class, "FractalDimension.name");
    }

    public Statistics getStatistics() {
        return new FractalDimension();
    }

    public Class<? extends Statistics> getStatisticsClass() {
        return FractalDimension.class;
    }
}
