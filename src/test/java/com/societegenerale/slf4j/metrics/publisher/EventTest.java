package com.societegenerale.slf4j.metrics.publisher;

import ch.qos.logback.classic.spi.ILoggingEvent;
import com.google.common.collect.ImmutableMap;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.MDC;

import java.util.AbstractMap;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

public class EventTest {

    @BeforeEach
    public void setUp() {
        MDC.clear();
        TestAppender.events.clear();
    }

    @Test
    public void should_create_a_new_functional_event() throws Exception {
        //when
        Event event = Event.functional("An event");

        //then
        assertThat(event.getAttributes())
                .containsExactlyInAnyOrderEntriesOf(ImmutableMap.of(
                        "type", "FUNCTIONAL",
                        "metricName", "An event"));
    }

    @Test
    public void should_create_a_new_technical_event() throws Exception {
        //when
        Event event = Event.technical("An event");

        //then
        assertThat(event.getAttributes())
                .containsExactlyInAnyOrderEntriesOf(ImmutableMap.of(
                        "type", "TECHNICAL",
                        "metricName", "An event"));
    }

    @Test
    public void should_create_a_new_custom_event() throws Exception {
        //when
        Event event = Event.custom("An event", "custom");

        //then
        assertThat(event.getAttributes())
                .containsExactlyInAnyOrderEntriesOf(ImmutableMap.of(
                        "type", "custom",
                        "metricName", "An event"));
    }

    @Test
    public void should_throw_exception_if_type_of_custom_event_is_null() throws Exception {
        // when
        assertThatExceptionOfType(AssertionError.class).isThrownBy(() -> Event.custom("An event", null));
    }

    @Test
    public void should_throw_exception_if_type_of_custom_event_length_is_zero() throws Exception {
        // when
        assertThatExceptionOfType(AssertionError.class).isThrownBy(() -> Event.custom("An event", ""));
    }

    @Test
    public void should_add_an_attribute() throws Exception {
        //when
        Event event = Event.custom("An event", "custom")
                .addAttribute("new", "attribute");

        //then
        assertThat(event.getAttributes())
                .containsExactlyInAnyOrderEntriesOf(ImmutableMap.of(
                        "new", "attribute",
                        "type", "custom",
                        "metricName", "An event"));
    }

    @Test
    public void should_publish_event_through_logging() throws Exception {
        //given
        Event event = Event.custom("An event", "custom")
                .addAttribute("new", "attribute");

        //when
        event.publish();

        //then
        assertThat(TestAppender.events).hasSize(1);
        ILoggingEvent loggingEvent = TestAppender.events.get(0);
        assertThat(loggingEvent.getLoggerName()).isEqualTo("custom");
        assertThat(loggingEvent.getMDCPropertyMap()).containsAllEntriesOf(ImmutableMap.of(
                "new", "attribute",
                "type", "custom"));
    }


    @Test
    public void should_restore_mdc_after_publish() throws Exception {
        //given
        MDC.put("existingKey", "existingValue");
        MDC.put("existingKey2", "existingValue2");

        Event event = Event.custom("An event", "custom")
                .addAttribute("existingKey", "newValue");

        //when
        event.publish();

        //then
        assertThat(MDC.getCopyOfContextMap()).containsAllEntriesOf(ImmutableMap.of(
                "existingKey", "existingValue",
                "existingKey2", "existingValue2"));
    }

    @Test
    public void should_throw_exception_when_modifying_from_outside() throws Exception {

        Event event = Event.custom("An event", "custom")
                .addAttribute("existingKey", "newValue");

        assertThat(event.getAttributes()).contains(new AbstractMap.SimpleEntry<>("existingKey", "newValue"));

        assertThatExceptionOfType(UnsupportedOperationException.class).isThrownBy(() -> event.getAttributes().put("someKey","someValue"));

    }


    @Test
    public void should_clear_MDC_after_publish_if_no_MDC_present_before_publish() throws Exception {
        //given

        Event event = Event.custom("An event", "custom")
                .addAttribute("existingKey", "newValue");

        //when
        event.publish();

        //then
        assertThat(MDC.getCopyOfContextMap()).isNullOrEmpty();
    }
}