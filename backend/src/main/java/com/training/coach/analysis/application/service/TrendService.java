package com.training.coach.analysis.application.service;

import java.util.List;
import org.springframework.stereotype.Service;

/**
 * Application service for calculating trends in metrics.
 */
@Service
public class TrendService {

    public String calculateTrend(List<Double> values) {
        if (values.size() < 2) {
            return "insufficient data";
        }
        double first = values.get(0);
        double last = values.get(values.size() - 1);
        if (last > first) {
            return "upward";
        }
        if (last < first) {
            return "downward";
        }
        return "stable";
    }
}
