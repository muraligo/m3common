package com.m3.common.monitor.prometheus;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.URLDecoder;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.zip.GZIPOutputStream;

import org.slf4j.Logger;

import com.m3.common.monitor.M3MonitorFramework;
import com.m3.common.monitor.M3MonitorMetric;
import com.m3.common.monitor.M3MonitorSample;
import com.sun.net.httpserver.HttpContext;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;

import io.prometheus.client.CollectorRegistry;
import io.prometheus.client.exporter.common.TextFormat;

public class M3PrometheusFramework implements M3MonitorFramework {
    private static final String DEFAULT_METRICS_PATH = "/metrics";
    private static Logger _LOG;
    private static M3PrometheusFramework _INSTANCE = null;
    private static boolean _initialized = false;

    private final LocalByteArray _metrics_response;
    private final String _framework;
    private HttpContext _metricsContext;

    private M3PrometheusFramework(String kind) {
    	_framework = kind;
        _metrics_response = new LocalByteArray();
    }

    // we create a new server for each target as for each we need a new port at least
    public static M3PrometheusFramework getInstance(Logger loggr) {
        if (_INSTANCE != null) {
            _LOG = loggr;
            _INSTANCE = new M3PrometheusFramework("MONITOR.PROMETHEUS");
        }
        if (_initialized) {
            _LOG.warn("M3MonitoringFramework needs to be initialized with properties prior to use!!!");
        }
        return _INSTANCE;
    }

    @Override
    public String kind() { return _framework; }

    @Override
    public void configure(Map<String, Object> props) {
        String hostname = (String)props.get("hostname");
        String metricspath = (String)props.get("metricspath");
        Integer portval = (Integer)props.get("metricsport");
        int portnumber = -1;
        if (portval != null) {
            portnumber = portval;
        } else {
            throw new IllegalStateException("MonitorFramework.configure must have a port number at least");
        }
        HttpServer server = null;
        if (_LOG != null) { // ensures no NPE for a log message
            _LOG.debug("About to create server listening at [" + ((hostname == null) ? "" : hostname) + "]/[" + portnumber + "]/[" + metricspath + "]");
        }
        try {
            InetSocketAddress isa = (hostname != null) ? new InetSocketAddress(hostname, portnumber) : new InetSocketAddress(portnumber);
            if (_LOG != null) { // ensures no NPE for a log message
                _LOG.debug("Address is [" + isa.getAddress().getHostAddress() + "]/[" + isa.getPort() + "]");
            }
            server = HttpServer.create(isa, 0); // 2nd arg is backlog
        } catch (IOException ioe1) {
            String errmsg = "EXCEPTION creating HTTP server listening for Prometheus scrape. Exiting";
            if (_LOG != null) { // ensures no NPE for a log message
                _LOG.error(errmsg, ioe1);
            }
            throw new RuntimeException(errmsg);
        }
        if (metricspath == null) {
            metricspath = DEFAULT_METRICS_PATH;
        }
        _metricsContext = server.createContext(metricspath);
        _metricsContext.setHandler(M3PrometheusFramework::handlePrometheusMetricsScrape);
        // TODO keep a registry of handlers and ensure no clash (centralized service??)
    }

    public HttpContext context() { return _metricsContext; }

    @Override
    public M3MonitorMetric registerMetric(String typ, String nm, String desc, List<String> labels) {
        if ("COUNTER".equalsIgnoreCase(nm)) {
            String[] lblarr = labels.toArray(new String[labels.size()]);
            return M3PrometheusCounter.builder().name(nm).help(desc).labels(lblarr).build();
        }
        // TODO Gauge
        // TODO other metric types
        return null;
    }

	@Override
	public void addSample(M3MonitorSample sample) {
		// TODO Auto-generated method stub

	}

	@Override
	public M3MonitorSample getLastSample() {
		// TODO Auto-generated method stub
		return null;
	}


    static void handlePrometheusMetricsScrape(HttpExchange exchange) {
        String query = exchange.getRequestURI().getRawQuery();
        ByteArrayOutputStream _response = _INSTANCE._metrics_response.get();
        _response.reset();
        OutputStreamWriter osw = new OutputStreamWriter(_response);
        boolean error = false;
        String errmsg = null;
        try {
        	// TODO for something other than Prometheus scrape we still need to get the samples but then translate them
            TextFormat.write004(osw,
                    CollectorRegistry.defaultRegistry.filteredMetricFamilySamples(parsePrometheusQuery(query)));
            osw.flush();
            osw.close();
            _response.flush();
            _response.close();
        } catch (UnsupportedEncodingException uee) {
            errmsg = "EXCEPTION decoding query parameters";
            if (_LOG != null) { // ensures no NPE for a log message
                _LOG.error(errmsg, uee);
            }
            error = true;
        } catch (IOException ioe) {
            errmsg = "EXCEPTION writing metrics to stream";
            if (_LOG != null) { // ensures no NPE for a log message
                _LOG.error(errmsg, ioe);
            }
            error = true;
        } catch (Throwable t) {
            errmsg = "EXCEPTION unknown reading and parsing request from HTTP socket";
            if (_LOG != null) { // ensures no NPE for a log message
                _LOG.error(errmsg, t);
            }
            error = true;
        }

        boolean useCompression = false;
        if (!error) {
            List<String> encodingHeaders = exchange.getRequestHeaders().get("Accept-Encoding");
            if (encodingHeaders != null) {
                for (String encodingHeader : encodingHeaders) {
                    String[] encodings = encodingHeader.split(",");
                    for (String encoding : encodings) {
                        if (encoding.trim().toLowerCase().equals("gzip")) {
                            useCompression =  true;
                            break;
                        }
                    }
                    if (useCompression) break;
                }
            }
        }

        OutputStream os = null;
        try {
            if (error) {
                byte[] respdata = errmsg.getBytes();
                exchange.sendResponseHeaders(500, respdata.length);
                os = exchange.getResponseBody();
                os.write(respdata);
            } else {
                exchange.getResponseHeaders().set("Content-Type",
                        TextFormat.CONTENT_TYPE_004);
                if (useCompression) {
                	exchange.getResponseHeaders().set("Content-Encoding", "gzip");
                	exchange.sendResponseHeaders(HttpURLConnection.HTTP_OK, 0);
                    os = new GZIPOutputStream(exchange.getResponseBody());
                    _response.writeTo(os);
                    os.close();
                } else {
                	exchange.getResponseHeaders().set("Content-Length",
                            String.valueOf(_response.size()));
                	exchange.sendResponseHeaders(HttpURLConnection.HTTP_OK, _response.size());
                	os = exchange.getResponseBody();
                    _response.writeTo(os);
                }
            }
        } catch (IOException ioe) {
            if (_LOG != null) { // ensures no NPE for a log message
                _LOG.error("EXCEPTION writing response to HTTP socket", ioe);
            }
        } finally {
            if (os != null) {
                try { os.close(); } catch (IOException ioe2) { /* ignore */ }
            }
            exchange.close();
        }
    }

    private static Set<String> parsePrometheusQuery(String query) throws UnsupportedEncodingException {
        Set<String> names = new HashSet<String>();
        if (query != null) {
            String[] pairs = query.split("&");
            for (String pair : pairs) {
                int idx = pair.indexOf("=");
                if (idx != -1 && URLDecoder.decode(pair.substring(0, idx), "UTF-8").equals("name[]")) {
                    names.add(URLDecoder.decode(pair.substring(idx + 1), "UTF-8"));
                }
            }
        }
        return names;
    }


    private static class LocalByteArray extends ThreadLocal<ByteArrayOutputStream> {
        protected ByteArrayOutputStream initialValue() {
            return new ByteArrayOutputStream(1 << 20);
        }
    }

}
