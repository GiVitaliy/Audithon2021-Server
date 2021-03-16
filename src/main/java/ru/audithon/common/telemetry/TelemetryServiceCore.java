package ru.audithon.common.telemetry;

import com.google.common.collect.Lists;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import ru.audithon.common.exceptions.BusinessLogicException;
import ru.audithon.common.helpers.DateUtils;
import ru.audithon.common.types.Tuple2;
import ru.audithon.common.types.Tuple3;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class TelemetryServiceCore {
    // минимальный период (квант) времени в милисекундах, за который собирается телеметрия
    // (все, что случилось за один квант времени, попадает в этот квант)
    private static final long TELEMETRY_QUANTUM_SEC = 20;
    // за какой период хранится история телеметрии, в часах
    private static final long TELEMETRY_KEEP_ALIVE_HOURS = 24;
    // возвращаем всегда так, чтобы у нас было TELEMETRY_RESULT_ELEMENTS элементов.
    // С дефолтовыми настройками (x=60) это 20 последних минут.
    // на 72x это соответственно будет 1440 минут или один день
    private static final int TELEMETRY_RESULT_ELEMENTS = 60;

    private LocalDateTime frameStartTime = LocalDateTime.now();
    private LocalDateTime lastWipeTime = LocalDateTime.now().minusSeconds(TELEMETRY_QUANTUM_SEC);
    private final Set<TelemetryOperationToken> runningOperations = new HashSet<>();
    private final Map<String, int[]> telemetryDuration = new HashMap<>();
    private final Map<String, int[]> telemetryQuantity = new HashMap<>();

    @Value("${application-settings.telemetry-enabled}")
    private Boolean enabled;

    public static class TelemetryOperationToken {
        @Getter
        private LocalDateTime started = LocalDateTime.now();
        private LocalDateTime telemetryGathered = LocalDateTime.now();
        @Getter
        private String operationCode;
        @Getter
        private String operationCodeGroup;
        private boolean quantityGathered = false;


        private TelemetryOperationToken(String operationCode) {
            this.operationCode = operationCode;

            int ps = operationCode.indexOf("::");
            operationCodeGroup = ps > 0 ? operationCode.substring(0, ps) : operationCode;
        }
    }

    private static TelemetryOperationToken _disabledToken = new TelemetryOperationToken("telemetry disabled in config");

    public TelemetryOperationToken enterOperation(String operationCode) {
        if (!enabled) {
            return _disabledToken;
        }

        synchronized (runningOperations) {
            TelemetryOperationToken op = new TelemetryOperationToken(operationCode);
            gatherTelemetry(op);
            runningOperations.add(op);
            return op;
        }
    }

    public void exitOperation(TelemetryOperationToken operationToken) {
        if (!enabled) {
            return;
        }

        synchronized (runningOperations) {
            gatherTelemetry(operationToken);
            runningOperations.remove(operationToken);
        }
    }

    @Scheduled(fixedDelay = TELEMETRY_QUANTUM_SEC * 1000)
    public void gatherTelemetryAll() {
        if (!enabled) {
            return;
        }
        TelemetryOperationToken optoken = enterOperation("@Scheduled::gatherTelemetryAll()");
        try {
            synchronized (runningOperations) {
                runningOperations.forEach(this::gatherTelemetry);
            }
        } finally {
            exitOperation(optoken);
        }
    }


    public Collection<TelemetryOperationToken> getRunningOperations() {
        synchronized (runningOperations) {
            return Lists.newArrayList(runningOperations);
        }
    }

    public Collection<Tuple3<String, Long, Long>> getGatheredOperations() {
        synchronized (runningOperations) {
            List<Tuple3<String, Long, Long>> operations = new ArrayList<>();
            telemetryDuration.keySet().forEach(
                key -> operations.add(new Tuple3<>(key, calcSum(telemetryDuration.get(key)), calcSum(telemetryQuantity.get(key))))
            );
            return operations;
        }
    }

    public List<Tuple3<Long, Long, String>> getOperationTelemetry(String opCode, Integer factor) {

        synchronized (runningOperations) {

            if (factor <= 0 && factor > 288) {
                throw new BusinessLogicException("Некорректно указан масштаб (factor). Корректные значения - [1..288]");
            }


            List<Tuple3<Long, Long, String>> retVal = new ArrayList<>(TELEMETRY_RESULT_ELEMENTS);
            for (int i = 0; i < TELEMETRY_RESULT_ELEMENTS; i++) {
                retVal.add(null);
            }
            int retVal_ix = retVal.size() - 1;

            int[] gatheredDuration = telemetryDuration.get(opCode);
            int[] gatheredQuantity = telemetryQuantity.get(opCode);

            final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm");

            if (gatheredDuration != null) {
                // строим график начиная от текущего кванта (на текущий момент времени) и назад, чтобы показать последние 60 значений
                //с учетом установленного масштаба (scale)
                LocalDateTime telemetryTime = LocalDateTime.now();
                int start = getTimeIndexInTelemetryUnbounded(telemetryTime) + 1;

                for (int i = gatheredDuration.length - 1; i >= 0 && retVal_ix >= 0; i -= factor) {
                    long valDuration = 0;
                    long valQuantity = 0;
                    for (int j = i; j >= i - factor + 1; j--) {
                        valDuration = valDuration + gatheredDuration[(gatheredDuration.length + j + start) % gatheredDuration.length];
                        valQuantity = valQuantity + gatheredQuantity[(gatheredQuantity.length + j + start) % gatheredQuantity.length];
                    }

                    retVal.set(retVal_ix, new Tuple3<>(valDuration, valQuantity,
                        telemetryTime.minusSeconds(TELEMETRY_QUANTUM_SEC * (gatheredDuration.length - 1 - i)).format(formatter)));
                    retVal_ix--;
                }
            }

            return Lists.newArrayList(retVal);
        }
    }

    private void gatherTelemetry(TelemetryOperationToken operationToken) {

        LocalDateTime gatherTime = LocalDateTime.now();

        // если вдруг очень много пропустили, то обнуляем счетчики, чтобы все ок работало
        if (Duration.between(lastWipeTime, gatherTime).toMillis() > TELEMETRY_KEEP_ALIVE_HOURS * 60 * 60 * 1000) {
            frameStartTime = gatherTime;
            lastWipeTime = gatherTime.minusSeconds(TELEMETRY_QUANTUM_SEC);
            telemetryDuration.clear();
            telemetryQuantity.clear();
        }

        if (Duration.between(frameStartTime, gatherTime).toMillis() > TELEMETRY_KEEP_ALIVE_HOURS * 60 * 60 * 1000) {
            frameStartTime = frameStartTime.plusHours(TELEMETRY_KEEP_ALIVE_HOURS);
        }

        wipeOldTelemetryData(gatherTime);

        Tuple2<int[], int[]> op1 = ensureOpcodeTelemetry(operationToken.operationCode);
        Tuple2<int[], int[]> op2 = ensureOpcodeTelemetry(operationToken.operationCodeGroup);
        Tuple2<int[], int[]> op3 = ensureOpcodeTelemetry("@Total");

        propagateDurationToTelemetry(operationToken, gatherTime, op1.getA(), op2.getA(), op3.getA());
        propagateQuantityToTelemetry(operationToken, op1.getB(), op2.getB(), op3.getB());
    }

    private Tuple2<int[], int[]> ensureOpcodeTelemetry(String operationCode) {

        int size = Long.valueOf(TELEMETRY_KEEP_ALIVE_HOURS * 60 * 60 / TELEMETRY_QUANTUM_SEC + 1).intValue();

        return new Tuple2<>(telemetryDuration.computeIfAbsent(operationCode, k -> new int[size]),
            telemetryQuantity.computeIfAbsent(operationCode, k -> new int[size]));
    }

    private void wipeOldTelemetryData(LocalDateTime gatherTime) {

        int i_lastwipe = getTimeIndexInTelemetryUnbounded(lastWipeTime);
        int i_gather = getTimeIndexInTelemetryUnbounded(gatherTime);

        for (int i = i_lastwipe + 1; i <= i_gather; i++) {
            for (int[] values : telemetryDuration.values()) {
                values[(values.length + i) % values.length] = 0;
            }
            for (int[] values : telemetryQuantity.values()) {
                values[(values.length + i) % values.length] = 0;
            }
        }

        lastWipeTime = gatherTime;
    }

    private void propagateDurationToTelemetry(TelemetryOperationToken operationToken, LocalDateTime gatherTime,
                                              int[]... valuesBundle) {
        LocalDateTime cTime = operationToken.telemetryGathered;
        operationToken.telemetryGathered = gatherTime;
        if (Duration.between(cTime, gatherTime).toMillis() < TELEMETRY_KEEP_ALIVE_HOURS * 60 * 60 * 1000) {

            int start = getTimeIndexInTelemetryUnbounded(cTime);
            int end = getTimeIndexInTelemetryUnbounded(gatherTime);

            for (int i = start; i <= end; i++) {
                LocalDateTime quantumStart = frameStartTime.plusSeconds(TELEMETRY_QUANTUM_SEC * i);
                LocalDateTime quantumEndExcluded = frameStartTime.plusSeconds(TELEMETRY_QUANTUM_SEC * (i + 1));

                int quantumDuration = Long.valueOf(Duration.between(
                    DateUtils.max(cTime, quantumStart),
                    DateUtils.min(quantumEndExcluded, gatherTime)
                ).toMillis()).intValue();

                for (int[] values : valuesBundle) {
                    values[(values.length + i) % values.length] += quantumDuration;
                }
            }
        }
    }

    private void propagateQuantityToTelemetry(TelemetryOperationToken operationToken, int[]... valuesBundle) {
        // единожды прибавляем 1 к кванту, в котором началась операция (начало берем, чтобы сразу зафиксировать факт
        // вызова операции, чтобы это все было видно сразу онлайн, даже если 100500 операций еще не закончено и болтаются
        // как активные)
        if (!operationToken.quantityGathered) {
            operationToken.quantityGathered = true;

            int i = getTimeIndexInTelemetryUnbounded(operationToken.started);

            for (int[] values : valuesBundle) {
                values[(values.length + i) % values.length] += 1;
            }
        }
    }

    private int getTimeIndexInTelemetryUnbounded(LocalDateTime time) {
        return Long.valueOf(Duration.between(frameStartTime, time).toMillis() / 1000 / TELEMETRY_QUANTUM_SEC).intValue();
    }

    private Long calcSum(int[] values) {
        long retVal = 0;
        for (int val : values) {
            retVal += val;
        }
        return retVal;
    }
}
