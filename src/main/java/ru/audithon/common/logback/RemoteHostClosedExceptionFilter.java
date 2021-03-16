package ru.audithon.common.logback;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.filter.Filter;
import ch.qos.logback.core.spi.FilterReply;

public class RemoteHostClosedExceptionFilter extends ConnectivityRelatedExceptionFilter {

    @Override
    public FilterReply decide(ILoggingEvent event) {
        if (isConnectivityRelatedException(event)) {
            return FilterReply.ACCEPT;
        } else {
            return FilterReply.DENY;
        }
    }
}