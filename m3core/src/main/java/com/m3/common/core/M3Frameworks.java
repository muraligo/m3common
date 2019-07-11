package com.m3.common.core;

import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class M3Frameworks {

    public enum Framework {
        MONITOR_PROMETHEUS, 
        AAA_OAUTH;

        private final ConcurrentMap<String, Object> _properties = new ConcurrentHashMap<String, Object>();

        private Framework() {
        }

        public void addProperty(String name, Object value) {
            _properties.put(name, value);
        }

        public Map<String, Object> getProperties() {
            return Collections.unmodifiableMap(_properties);
        }
    }
}
