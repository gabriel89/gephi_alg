package org.gephi.statistics.plugin.builder.social;

import org.gephi.statistics.plugin.social.ExamOptimize;
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
public class ExamOptimizeBuilder implements StatisticsBuilder {

    public String getName() {
        return NbBundle.getMessage(ExamOptimizeBuilder.class, "ExamOptimize.name");
    }

    public Statistics getStatistics() {
        return new ExamOptimize();
    }

    public Class<? extends Statistics> getStatisticsClass() {
        return ExamOptimize.class;
    }
}
