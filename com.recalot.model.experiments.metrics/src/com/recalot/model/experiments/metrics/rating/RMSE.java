package com.recalot.model.experiments.metrics.rating;

import com.recalot.common.interfaces.model.experiment.RatingMetric;

/**
 * Implements root-mean-square error (RMSE)
 *
 * @author Matthäus Schmedding (info@recalot.com)
 */
public class RMSE extends RatingMetric {

    private int count;
    private double sum;

    public RMSE() {
        count = 0;
        sum = 0;
    }

    @Override
    public void addRating(double actual, double retrieved) {
        count++;
        sum += Math.pow(Math.abs(actual -  retrieved), 2);
    }

    @Override
    public double getResult() {
        return count > 0 ? Math.sqrt(sum / count) : Double.MAX_VALUE;
    }

    @Override
    public String getDescription() {
        return null;
    }
}