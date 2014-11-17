package org.deltaalex.fractaldimension;

/**
 * Finds the slope of the best fitting line which interpolates a given data set.
 * <br> It does linear interpolation for linear scale coordinates <br> It does
 * power law interpolation for loglog scale coordinates <br>
 *
 * @author Alexandru Topirceanu
 */
public class Interpolation {

    public double getSlopeForLinearScale(double[] x, double[] y) {

        if (x == null || y == null || x.length == 0 || y.length == 0
                || x.length != y.length) {
            throw new IllegalArgumentException("Sets are null, empty or have different lenghts");
        }

        double m = 0;

        double avgx = getAverage(x);
        double avgy = getAverage(y);

        // according to Excel's LINEST function:
        // http://office.microsoft.com/en-us/excel-help/linest-HP005209155.aspx
        double sum1 = 0, sum2 = 0;
        for (int i = 0; i < x.length; ++i) {
            sum1 += (x[i] - avgx) * (y[i] - avgy);
            sum2 += (x[i] - avgx) * (x[i] - avgx);
        }

        m = sum1 / sum2;

        return m;
    }

    public double getSlopeForLogLogScale(double[] x, double[] y, double logBase) {

        if (x == null || y == null || x.length == 0 || y.length == 0
                || x.length != y.length) {
            throw new IllegalArgumentException("Sets are null, empty or have different lenghts");
        }

        if (logBase < 0 || logBase == 1) {
            throw new IllegalArgumentException("Logarithmic base must be >0 and !=1");
        }

        double[] _x = new double[x.length];
        double[] _y = new double[y.length];

        for (int i = 0; i < x.length; ++i) {
            _x[i] = Math.log10(x[i]) / Math.log10(logBase);
            _y[i] = Math.log10(y[i]) / Math.log10(logBase);
        }

        return getSlopeForLinearScale(_x, _y);
    }       

    private double getAverage(double[] set) {

        if (set == null || set.length == 0) {
            throw new IllegalArgumentException("Set is null of empty!");
        }

        double average = 0;

        for (double d : set) {
            average += d;
        }

        average /= set.length;

        return average;
    }
}
