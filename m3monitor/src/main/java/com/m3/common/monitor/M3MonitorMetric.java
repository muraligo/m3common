package com.m3.common.monitor;

import java.util.List;

public interface M3MonitorMetric {
    String metricType();
    String name();
    String help();
    List<String> labelNames();
}
