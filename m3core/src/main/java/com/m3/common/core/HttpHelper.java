package com.m3.common.core;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.yaml.snakeyaml.Yaml;

import com.sun.net.httpserver.HttpExchange;

public final class HttpHelper {
	public static final String CONTENT_TYPE_FORM_URL_ENCODED = "application/ x-www-form-urlencoded";
    public static final String CONTENT_TYPE_MULTIPART_FORM = "multipart/form-data";
    public static final String HEADER_CONTENT_TYPE = "Content-Type";

    public static final String buildUrlEncodedParameterUrl(String url, Map<String, String> params) {
        StringBuilder sb = new StringBuilder(url);
        sb.append(PARAMETER_START);
        boolean notfirst = false;
        for (Map.Entry<String, String> ent : params.entrySet()) {
            String encodedName;
            String encodedValue;
            try {
                encodedName = URLEncoder.encode(ent.getKey(), "UTF-8");
                String value = ent.getValue();
                encodedValue = value != null ? URLEncoder.encode(value, "UTF-8") : "";
            } catch (UnsupportedEncodingException uee1) {
                throw new IllegalArgumentException(uee1);
            }
            if (encodedName != null && encodedValue != null) {
                if (notfirst) {
                    sb.append(PARAMETER_SEPARATOR);
                } else {
                    notfirst = false;
                }
                sb.append(encodedName);
                sb.append(NAME_VALUE_SEPARATOR);
                sb.append(encodedValue);
            }
        }
        return sb.toString();
    }

    // configPath here is assumed to be relative or absolute to the ROOT
    public static Map<String, Object> parseAndLoadYamlAbs(Logger log, String configPath) {
        Yaml yaml = new Yaml();
        log.debug("Loading config from [" + configPath + "]");
        Map<String, Object> result = null;
        InputStream inputStream = null;
        try (FileInputStream fs = new FileInputStream(configPath)) {
            if (fs != null) {
                inputStream = fs;
                result = yaml.load(inputStream);
            }
        } catch (IOException ioe) {
            log.error( "ERROR Loading config from [" + configPath + "]. Unable to locate or read file.");
        }
        return result;
    }

    // This is a bit shaky. It doesn't handle continuation
    // lines, but our client shouldn't send any.
    // Read a line from the input stream, swallowing the final
    // \r\n sequence. Stops at the first \n, doesn't complain
    // if it wasn't preceded by '\r'.
    //
    public static String readLine(InputStream r) throws IOException {
        StringBuilder b = new StringBuilder();
        int c;
        while ((c = r.read()) != -1) {
            if (c == '\n') break;
            b.appendCodePoint(c);
        }
        if (c == -1 && b.length() == 0) {
            return null;
        }
        if (b.codePointAt(b.length() -1) == '\r') {
            b.delete(b.length() -1, b.length());
        }
        return b.toString();
    }

    public static void formErrorResponse(HttpExchange exchange, HttpResponseCode responseCode, String errorMessage, 
            Logger log) {
        String finalmsg = responseCode.baseMessage() + errorMessage;
    	try {
            exchange.sendResponseHeaders(responseCode.code(), finalmsg.length());
    	} catch (IOException ioe1) {
    	    log.error(finalmsg);
    	    return;
    	}
    	try (OutputStream os = exchange.getResponseBody()) {
    	    os.write(finalmsg.getBytes());
    	} catch (IOException ioe2) {
    	    log.error(finalmsg);
    	    return;
    	}
    	// should implicitly close request Input Stream if it was opened
    }

    public static Map<String, String> parseUrlQuery(String query) throws UnsupportedEncodingException {
        Map<String, String> params = new HashMap<String, String>();
        if (query != null) {
            String[] pairs = query.split("&");
            for (String pair : pairs) {
                int idx = pair.indexOf("=");
                if (idx != -1)
                    params.put(URLDecoder.decode(pair.substring(0, idx), "UTF-8"), 
                                URLDecoder.decode(pair.substring(idx+1), "UTF-8"));
            }
        }
        return params;
    }

    public static String readAllFormDataParams(InputStream is, String boundary, Map<String, String> formParams)
            throws IOException {
        String reqdata;
        // skip till start boundary (or end)
        String bndrymarker = MULTIPART_BOUNDARY_PREFIX + boundary;
        while ((reqdata = readLine(is)) != null) {
            if (!reqdata.isBlank() && reqdata.strip().startsWith(bndrymarker)) {
                break;
            }
        }
        while ((reqdata = readAFormDataParam(is, bndrymarker, formParams)) != null) {
            if (reqdata.startsWith("ENDMULTIPART")) {
                reqdata = reqdata.substring("ENDMULTIPART ".length());
                break;
            }
        }
        return reqdata;
    }

    // read a single Form-Data parameter name and value contained within a single part of a multipart body with a boundary
    private static String readAFormDataParam(InputStream is, String boundaryMarker, Map<String, String> formParams) 
            throws IOException {
        String reqdata;
        reqdata = readLine(is);
        if (reqdata == null || reqdata.isBlank()) {
            return null;
        }
        reqdata = reqdata.strip();
        if (!reqdata.startsWith(FORM_DATA_PARM_START)) {
            // end of multipart segments
        	return "ENDMULTIPART " + reqdata;
        }
        String fieldname = reqdata.substring(FORM_DATA_PARM_PREFIX.length(), reqdata.lastIndexOf('\"'));
        if (fieldname != null) {
            StringBuilder sbfld = new StringBuilder();
            while ((reqdata = readLine(is)) != null) {
                if (!reqdata.isBlank() && reqdata.strip().startsWith(boundaryMarker)) {
                    break;
                }
                if (!reqdata.isBlank()) {
                    sbfld.append(reqdata.strip());
                }
            }
            if (sbfld.length() > 0) {
                formParams.put(fieldname, sbfld.toString());
            }
        }
        return reqdata;
    }

    private static final String PARAMETER_START = "?";
    private static final String PARAMETER_SEPARATOR = "&";
    private static final String NAME_VALUE_SEPARATOR = "=";
    private static final String MULTIPART_BOUNDARY_PREFIX = "--";
    private static final String FORM_DATA_PARM_START = "Content-Disposition";
    private static final String FORM_DATA_PARM_PREFIX = FORM_DATA_PARM_START + ": form-data; name=\"";
}
