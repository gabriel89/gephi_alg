package org.gephi.statistics.spi;

/**
 * Allows a StatisticsUI to set itself as the parent of a Statistic.
 * @author Alexander
 */
public interface StatisticsCallbackProvider {
    
    public void setStatisticsUIProvider(StatisticsUICallbackProvider parent);
}
