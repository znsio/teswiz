package com.znsio.teswiz.aspect;

import com.znsio.teswiz.businessLayer.aspectfixture.AspectFixtureBL;
import com.znsio.teswiz.runner.AspectLoggingProbe;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.Logger;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.appender.AbstractAppender;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class AspectLoggingWeavingTest {
    private static final String WOVEN_LOGGER_NAME = AspectJMethodLoggers.class.getName();

    private CapturingAppender capturingAppender;
    private Logger wovenLogger;

    @BeforeEach
    void attachCapturingAppender() {
        capturingAppender = new CapturingAppender();
        capturingAppender.start();
        LoggerContext context = (LoggerContext) LogManager.getContext(false);
        wovenLogger = context.getLogger(WOVEN_LOGGER_NAME);
        wovenLogger.addAppender(capturingAppender);
        wovenLogger.setLevel(Level.DEBUG);
    }

    @AfterEach
    void detachCapturingAppender() {
        wovenLogger.removeAppender(capturingAppender);
        capturingAppender.stop();
    }

    @Test
    void frameworkInternalRunnerMethodIsLoggedAtDebugByAspectLogging() {
        new AspectLoggingProbe().doSomething();

        assertThat(capturingAppender.messagesAtLevel(Level.DEBUG))
                .anyMatch(message -> message.contains("AspectLoggingProbe") && message.contains("doSomething"));
    }

    @Test
    void consumerAuthoredBusinessLayerMethodIsLoggedAtInfoByConsumerLayerAspectLogging() {
        new AspectFixtureBL().doSomething();

        assertThat(capturingAppender.messagesAtLevel(Level.INFO))
                .anyMatch(message -> message.contains("AspectFixtureBL") && message.contains("doSomething"));
    }

    @Test
    void methodOutsideBothAspectScopesIsNotWoven() {
        AspectJMethodLoggers.generateAfterMethodAspectJLogger("SomeClass", "someMethod");

        assertThat(capturingAppender.capturedMessages()).isEmpty();
    }

    private static class CapturingAppender extends AbstractAppender {
        private final List<LogEvent> events = new ArrayList<>();

        CapturingAppender() {
            super("CapturingAppender", null, null, false, null);
        }

        @Override
        public void append(LogEvent event) {
            events.add(event.toImmutable());
        }

        List<String> capturedMessages() {
            return events.stream().map(event -> event.getMessage().getFormattedMessage()).toList();
        }

        List<String> messagesAtLevel(Level level) {
            return events.stream()
                    .filter(event -> event.getLevel().equals(level))
                    .map(event -> event.getMessage().getFormattedMessage())
                    .toList();
        }
    }
}
