package ru.audithon.common.helpers;

import org.springframework.dao.OptimisticLockingFailureException;
import ru.audithon.common.exceptions.BusinessLogicException;

import static ru.audithon.common.helpers.ObjectUtils.coalesce;

public class ExceptionHelper {
    public static boolean isOptimisticLockException(Throwable ex) {
        return ex != null
                && (((ex instanceof BusinessLogicException)
                        && coalesce(((BusinessLogicException) ex).getMessageKey(), "").contains("optimisticLockViolation"))
                    || (ex instanceof OptimisticLockingFailureException));
    }
}
