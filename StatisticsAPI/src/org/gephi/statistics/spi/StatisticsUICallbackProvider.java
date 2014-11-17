/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.gephi.statistics.spi;

/**
 * Allows a Statistic to send the results back to the StatisticsUI parent
 * 
 * @author Alexander
 */
public interface StatisticsUICallbackProvider {
    
    public void callback(Object[] objects);
}
