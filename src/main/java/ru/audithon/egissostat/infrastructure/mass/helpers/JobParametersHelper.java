package ru.audithon.egissostat.infrastructure.mass.helpers;

import com.google.common.collect.Lists;
import lombok.SneakyThrows;
import org.apache.commons.codec.digest.DigestUtils;
import ru.audithon.egissostat.jobs.JobParameters;
import ru.audithon.common.helpers.DateUtils;
import ru.audithon.common.mapper.JsonMappers;

import java.lang.reflect.Method;
import java.time.LocalDate;
import java.util.*;

import static ru.audithon.common.helpers.ObjectUtils.isNull;

public class JobParametersHelper {
    @SneakyThrows
    public static String buildParamsDigest(JobParameters parameters) {
        StringBuilder sb = new StringBuilder();
        List<Method> methods = Lists.newArrayList(parameters.getClass().getMethods());
        methods.sort(Comparator.comparing(Method::getName));
        for (Method method : methods) {
            if (shouldIncludeInDigest(method)) {
                String val = isNull(method.invoke(parameters), "NULL").toString();
                // слишком большие строки мы в дайджест не записываем - используем вместо них хэш
                if (val.length() > 50) {
                    val = "h$" + DigestUtils.md5Hex(val).toUpperCase();
                }
                sb.append(val);
                sb.append(":");
            }
        }
        return sb.toString();
    }

    private static boolean shouldIncludeInDigest(Method method) {
        return !Objects.equals(method.getName(), "getDateActual")
            && !Objects.equals(method.getName(), "getUploadedFileStream")
            && !Objects.equals(method.getName(), "getClass")
            && method.getName().startsWith("get")
            && method.getParameterCount() == 0 && !Objects.equals(method.getReturnType().getName(), "void");
    }

    public static Collection<? extends JobParameters> buildJobStartingParameters(LocalDate dateX, String parametersStr) {
        // заменяем просто тупо текстовой заменой все возможные плейсхолдеры
        String paramsStr = parametersStr;
        paramsStr = paramsStr.replaceAll("\\$cmonth", DateUtils.startOfTheMonth(dateX).toString());
        paramsStr = paramsStr.replaceAll("\\$cquarter", DateUtils.startOfTheQuarter(dateX).toString());
        paramsStr = paramsStr.replaceAll("\\$cyear", DateUtils.startOfTheYear(dateX).toString());
        paramsStr = paramsStr.replaceAll("\\$cdate", dateX.toString());

        // тут находим последовательности, чтобы подставить поочередно каждый элемент последовательности
        //пока для простоты у нас поддерживается только одна последовательность
        Integer ix1 = paramsStr.indexOf("$seq(");
        Integer ix2 = paramsStr.indexOf(")", ix1);
        if (ix1 >= 0 && ix2 >= 0) {
            String seqStr = paramsStr.substring(ix1 + 5, ix2);
            String[] seq = seqStr.split(",");
            List<JobParameters> retParams = new ArrayList<>();
            for (String seqItem : seq) {
                String unseqParamsStr = paramsStr.substring(0, ix1)
                    + (seqItem != null ? seqItem.trim() : "null")
                    + paramsStr.substring(ix2 + 1);
                JobParameters preparedParams = JsonMappers.readObject(unseqParamsStr, JobParameters.class);
                retParams.add(preparedParams);
            }
            return retParams;
        } else {
            JobParameters preparedParams = JsonMappers.readObject(paramsStr, JobParameters.class);
            return Collections.singletonList(preparedParams);
        }
    }
}
