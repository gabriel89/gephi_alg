package org.gephi.statistics.plugin.builder.social;

import org.gephi.statistics.plugin.social.Diffusion;
import org.gephi.statistics.spi.Statistics;
import org.gephi.statistics.spi.StatisticsBuilder;
import org.openide.util.NbBundle;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author Alexandru Topirceanu
 */
@ServiceProvider(service = StatisticsBuilder.class)
public class DiffusionBuilder implements StatisticsBuilder {

    public String getName() {
        return NbBundle.getMessage(DiffusionBuilder.class, "Diffusion.name");
    }

    public Statistics getStatistics() {
        return new Diffusion();
    }

    public Class<? extends Statistics> getStatisticsClass() {
        return Diffusion.class;
    }
}
