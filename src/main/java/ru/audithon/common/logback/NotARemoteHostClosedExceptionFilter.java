package ru.audithon.common.logback;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.filter.Filter;
import ch.qos.logback.core.spi.FilterReply;

public class NotARemoteHostClosedExceptionFilter extends ConnectivityRelatedExceptionFilter {

    @Override
    public FilterReply decide(ILoggingEvent event) {
        if (isConnectivityRelatedException(event)) {
            return FilterReply.DENY;
        } else {
            return FilterReply.ACCEPT;
        }
    }
}