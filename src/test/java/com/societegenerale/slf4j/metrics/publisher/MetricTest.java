package com.societegenerale.slf4j.metrics.publisher;

import ch.qos.logback.classic.spi.ILoggingEvent;
import com.google.common.collect.ImmutableMap;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.MDC;

import java.util.AbstractMap;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

public class MetricTest {

    @BeforeEach
    public void setUp() {
        MDC.clear();
        TestAppender.events.clear();
    }

    @Test
    public void should_create_a_new_functional_event() throws Exception {
        //when
        Metric metric = Metric.functional("An event");

        //then
        assertThat(metric.getAttributes())
                .containsExactlyInAnyOrderEntriesOf(ImmutableMap.of(
                        "metricType", "FUNCTIONAL",
                        "metricName", "An event"));
    }

    @Test
    public void should_create_a_new_technical_event() throws Exception {
        //when
        Metric metric = Metric.technical("An event");

        //then
        assertThat(metric.getAttributes())
                .containsExactlyInAnyOrderEntriesOf(ImmutableMap.of(
                        "metricType", "TECHNICAL",
                        "metricName", "An event"));
    }

    @Test
    public void should_create_a_new_custom_event() throws Exception {
        //when
        Metric metric = Metric.custom("An event", "custom");

        //then
        assertThat(metric.getAttributes())
                .containsExactlyInAnyOrderEntriesOf(ImmutableMap.of(
                        "metricType", "custom",
                        "metricName", "An event"));
    }

    @Test
    public void should_throw_exception_if_type_of_custom_event_is_null() throws Exception {
        // when
        assertThatExceptionOfType(AssertionError.class).isThrownBy(() -> Metric.custom("An event", null));
    }

    @Test
    public void should_throw_exception_if_type_of_custom_event_length_is_zero() throws Exception {
        // when
        assertThatExceptionOfType(AssertionError.class).isThrownBy(() -> Metric.custom("An event", ""));
    }

    @Test
    public void should_add_an_attribute() throws Exception {
        //when
        Metric metric = Metric.custom("An event", "custom")
                .addAttribute("new", "attribute");

        //then
        assertThat(metric.getAttributes())
                .containsExactlyInAnyOrderEntriesOf(ImmutableMap.of(
                        "new", "attribute",
                        "metricType", "custom",
                        "metricName", "An event"));
    }

    @Test
    public void should_publish_event_through_logging() throws Exception {
        //given
        Metric metric = Metric.custom("An event", "custom")
                .addAttribute("new", "attribute");

        //when
        metric.publish();

        //then
        assertThat(TestAppender.events).hasSize(1);
        ILoggingEvent loggingEvent = TestAppender.events.get(0);
        assertThat(loggingEvent.getLoggerName()).isEqualTo("custom");
        assertThat(loggingEvent.getMDCPropertyMap()).containsAllEntriesOf(ImmutableMap.of(
                "new", "attribute",
                "metricType", "custom"));
    }

    @Test
    public void should_also_output_attributes_and_values_as_a_string() throws Exception {
        //given
        Metric metric = Metric.custom("An event", "custom")
                .addAttribute("duration", "123");

        //when
        metric.publish();

        //then
        assertThat(TestAppender.events).hasSize(1);
        ILoggingEvent loggingEvent = TestAppender.events.get(0);
        assertThat(loggingEvent.toString()).endsWith("\"duration=123\";\"metricType=custom\";\"metricName=An event\"");
    }


    @Test
    public void should_restore_mdc_after_publish() throws Exception {
        //given
        MDC.put("existingKey", "existingValue");
        MDC.put("existingKey2", "existingValue2");

        Metric metric = Metric.custom("An event", "custom")
                .addAttribute("existingKey", "newValue");

        //when
        metric.publish();

        //then
        assertThat(MDC.getCopyOfContextMap()).containsAllEntriesOf(ImmutableMap.of(
                "existingKey", "existingValue",
                "existingKey2", "existingValue2"));
    }

    @Test
    public void should_ignore_attributes_with_null_keys() throws Exception {

        Metric metric = Metric.technical("An event")
                .addAttribute(null, "someValue");

        metric.publish();

        assertThat(MDC.getCopyOfContextMap()).isNullOrEmpty();
    }

    @Test
    public void should_throw_exception_when_modifying_from_outside() throws Exception {

        Metric metric = Metric.custom("An event", "custom")
                .addAttribute("existingKey", "newValue");

        assertThat(metric.getAttributes()).contains(new AbstractMap.SimpleEntry<>("existingKey", "newValue"));

        assertThatExceptionOfType(UnsupportedOperationException.class).isThrownBy(() -> metric.getAttributes().put("someKey","someValue"));

    }


    @Test
    public void should_clear_MDC_after_publish_if_no_MDC_present_before_publish() throws Exception {
        //given

        Metric metric = Metric.custom("An event", "custom")
                .addAttribute("existingKey", "newValue");

        //when
        metric.publish();

        //then
        assertThat(MDC.getCopyOfContextMap()).isNullOrEmpty();
    }
}
