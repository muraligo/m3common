package com.m3.common.monitor;

import java.util.List;

import com.m3.common.core.M3Framework;

public interface M3MonitorFramework extends M3Framework {
    M3MonitorMetric registerMetric(String typ, String name, String description, List<String> labels);
    void addSample(M3MonitorSample sample);
    M3MonitorSample getLastSample();
}
