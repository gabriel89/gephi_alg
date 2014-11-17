package org.gephi.statistics.plugin.builder.social;

import org.gephi.statistics.plugin.social.SocialInfluenceLayer;
import org.gephi.statistics.spi.Statistics;
import org.gephi.statistics.spi.StatisticsBuilder;
import org.openide.util.NbBundle;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author Alexandru Topirceanu
 */
@ServiceProvider(service = StatisticsBuilder.class)
public class SocialInfluenceLayerBuilder implements StatisticsBuilder {

    public String getName() {
        return NbBundle.getMessage(SocialInfluenceLayerBuilder.class, "SocialInfluenceLayer.name");
    }

    public Statistics getStatistics() {
        return new SocialInfluenceLayer();
    }

    public Class<? extends Statistics> getStatisticsClass() {
        return SocialInfluenceLayer.class;
    }
}
