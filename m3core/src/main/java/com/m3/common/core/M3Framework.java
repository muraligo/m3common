package com.m3.common.core;

import java.util.Map;

/**
 * Extensions to this interface specify a kind of Framework along with additional 
 * methods to configure the framework.
 * 
 * Classes implementing the extended framework interfaces must ensure to implement 
 * and call the configure method here before doing anything else.
 *
 */
public interface M3Framework {
    String kind();
    void configure(Map<String, Object> props);
}
