package com.m3.common.oauth2.api;

import java.io.Serializable;
import java.util.List;

public interface Service extends Comparable<Service>, Serializable {
    String identifier();
    String name();
    String description();
    List<String> scopes();
}
