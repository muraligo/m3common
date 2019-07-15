package com.m3.common.monitor.prometheus;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.m3.common.monitor.M3MonitorMetric;

import io.prometheus.client.Counter;

class M3PrometheusCounter implements M3MonitorMetric {
    private final String _metric_type = "COUNTER";
    private final String _name;
    private final String _help;
    private final List<String> _label_names = new ArrayList<String>();
    private final Counter _underlying_metric;

    private M3PrometheusCounter(String nm, String desc, String[] labels) {
        _name = nm;
        _help = desc;
        List<String> tmplst = Arrays.asList(labels);
        _label_names.addAll(tmplst);
        tmplst.clear();
        _underlying_metric = Counter.build().name(nm).help(desc).labelNames(labels).register();
    }

    @Override
    public String metricType() { return _metric_type; }

    @Override
    public String name() { return _name; }

    @Override
    public String help() { return _help; }

	@Override
    public List<String> labelNames() { return _label_names; }

    Counter prometheusMetric() { return _underlying_metric; }

    static CBuilder builder() { return new CBuilder(); }

    static class CBuilder {
        private String _name;
        private String _help;
        private String[] _labels;

        CBuilder name(String nm) { _name = nm; return this; }
        CBuilder help(String desc) { _help = desc; return this; }
        CBuilder labels(String... labels) { _labels = labels; return this; }
        M3PrometheusCounter build() { return new M3PrometheusCounter(_name, _help, _labels); }
    }
}
