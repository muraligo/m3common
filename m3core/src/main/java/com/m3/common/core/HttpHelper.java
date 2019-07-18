package com.m3.common.core;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Map;

public final class HttpHelper {
    private static final String PARAMETER_START = "?";
    private static final String PARAMETER_SEPARATOR = "&";
    private static final String NAME_VALUE_SEPARATOR = "=";

    public static final String buildUrlEncodedParameterUrl(String url, Map<String, String> params) {
        StringBuilder sb = new StringBuilder(url);
        sb.append(PARAMETER_START);
        boolean notfirst = false;
        for (Map.Entry<String, String> ent : params.entrySet()) {
            String encodedName;
            try {
                encodedName = URLEncoder.encode(ent.getKey(), "UTF-8");
            } catch (UnsupportedEncodingException uee1) {
                throw new IllegalArgumentException(uee1);
            }
            String encodedValue;
            try {
                String value = ent.getValue();
                encodedValue = value != null ? URLEncoder.encode(value, "UTF-8") : "";
            } catch (UnsupportedEncodingException uee2) {
                throw new IllegalArgumentException(uee2);
            }
            if (notfirst) {
                sb.append(PARAMETER_SEPARATOR);
            } else {
                notfirst = false;
            }
            sb.append(encodedName);
            sb.append(NAME_VALUE_SEPARATOR);
            sb.append(encodedValue);
        }
        return sb.toString();
    }
}
