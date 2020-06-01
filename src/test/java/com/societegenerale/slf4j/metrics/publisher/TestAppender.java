package com.societegenerale.slf4j.metrics.publisher;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.AppenderBase;

import java.util.ArrayList;
import java.util.List;

public class TestAppender extends AppenderBase<ILoggingEvent> {
    static List<ILoggingEvent> events = new ArrayList<>();

    @Override
    protected void append(ILoggingEvent e) {
        e.prepareForDeferredProcessing();
        events.add(e);
    }
}