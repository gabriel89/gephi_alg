package org.gephi.statistics.plugin.builder.social;

import org.gephi.statistics.plugin.social.TopiBaraGraph;
import org.gephi.statistics.spi.Statistics;
import org.gephi.statistics.spi.StatisticsBuilder;
import org.openide.util.NbBundle;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author Alexandru Topirceanu
 */
@ServiceProvider(service = StatisticsBuilder.class)
public class TopiBaraGraphBuilder implements StatisticsBuilder {

    public String getName() {
        return NbBundle.getMessage(TopiBaraGraphBuilder.class, "TopiBaraGraph.name");
    }

    public Statistics getStatistics() {
        return new TopiBaraGraph();
    }

    public Class<? extends Statistics> getStatisticsClass() {
        return TopiBaraGraph.class;
    }
}
