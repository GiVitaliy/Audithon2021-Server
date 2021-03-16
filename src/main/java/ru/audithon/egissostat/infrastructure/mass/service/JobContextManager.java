package ru.audithon.egissostat.infrastructure.mass.service;

public class JobContextManager {
    private final static ThreadLocal<JobContextData> localContextData = new InheritableThreadLocal<>();

    public static void writeContext(JobContextData contextData) {
        localContextData.set(contextData);
    }

    public static JobContextData readContext() {
        return localContextData.get();
    }
}
