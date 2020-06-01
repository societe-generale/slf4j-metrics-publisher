package com.societegenerale.slf4j.metrics.publisher;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import java.util.*;

public class Metric {
    private static final String FUNCTIONAL_TYPE = "FUNCTIONAL";
    private static final String TECHNICAL_TYPE = "TECHNICAL";
    private static final String NAME_ATTRIBUTE = "metricName";
    private static final String TYPE_ATTRIBUTE = "metricType";

    private final Map<String, String> attributes = new HashMap<>();
    private final Logger logger;

    private Metric(String name, String type) {
        attributes.put(NAME_ATTRIBUTE, name);
        attributes.put(TYPE_ATTRIBUTE, type);
        this.logger = LoggerFactory.getLogger(type);
    }

    /**
     * Convenience method to create a FUNCTIONAL metric of
     *
     * @param name Metric name
     * @return
     */
    public static Metric functional(String name) {
        return new Metric(name, FUNCTIONAL_TYPE);
    }

    /**
     * Convenience method to create a TECHNICAL metric of
     *
     * @param name Metric name
     * @return
     */
    public static Metric technical(String name) {
        return new Metric(name, TECHNICAL_TYPE);
    }

    /**
     * Creates a metric with a custom type
     *
     * @param name Metric name
     * @return
     */
    public static Metric custom(String name, String type) {
        assert type != null && type.length() > 0;
        return new Metric(name, type);
    }

    /**
     * Making that method _synchronized_ in case the client manipulates the event with several threads.
     * No impact on performance, as JVM will optimize it if it can.
     * @param name the attribute name
     * @param value the attribute value
     * @return the event itself, to allow chained calls
     */
    public synchronized Metric addAttribute(String name, String value) {
        attributes.putIfAbsent(name, value);
        return this;
    }

    /**
     * Logs the metric using SLFJ log statemet. The attributes are written into the MDC
     */
    public synchronized void publish() {
        final Map<String, String> copyOfMDC = MDC.getCopyOfContextMap();

        attributes.forEach(MDC::put);
        try {
            logger.info("publishing metric..");
        } finally {
            if (copyOfMDC != null) {
                MDC.setContextMap(copyOfMDC);
            } else {
                MDC.clear();
            }
        }
    }

    /**
     *
     * @return an immutable copy of the attributes contained in the event
     */
    public Map<String, String> getAttributes(){
        return Collections.unmodifiableMap(attributes);
    }
}