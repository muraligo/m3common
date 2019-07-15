package com.m3.common.monitor;

import java.time.Instant;
import java.util.List;

public interface M3MonitorSample {
    String name();
    double value();
    Instant timestamp();
    List<String> labelNames();
    List<String> labelValues();
    M3MonitorMetric metric();
}
