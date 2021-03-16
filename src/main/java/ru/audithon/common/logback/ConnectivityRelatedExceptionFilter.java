package ru.audithon.common.logback;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.filter.Filter;
import ch.qos.logback.core.spi.FilterReply;

public abstract class ConnectivityRelatedExceptionFilter extends Filter<ILoggingEvent> {

    @Override
    public abstract FilterReply decide(ILoggingEvent event);

    protected boolean isConnectivityRelatedException(ILoggingEvent event) {
        return isExceptionOf(event, "java.io.IOException")
            || isExceptionOf(event, "java.net.ConnectException")
            || isExceptionOf(event, "java.net.UnknownHostException")
            || isExceptionOf(event, "java.net.SocketTimeoutException");
    }

    private boolean isExceptionOf(ILoggingEvent event, String className) {
        return event.getThrowableProxy() != null && (event.getThrowableProxy().getClassName().equals(className)
            || (event.getThrowableProxy().getCause() != null
            && event.getThrowableProxy().getCause().getClassName().equals(className)));
    }
}
