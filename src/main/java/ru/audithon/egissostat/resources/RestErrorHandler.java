package ru.audithon.egissostat.resources;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.http.HttpStatus;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import ru.audithon.common.exceptions.BusinessLogicException;
import ru.audithon.common.exceptions.FileNameTooLongException;
import ru.audithon.common.notification.NotificationSeverity;
import ru.audithon.egissostat.infrastructure.MessageTranslator;

import java.util.List;
import java.util.stream.Collectors;

@ControllerAdvice
public class RestErrorHandler {

    private static final Logger logger = LoggerFactory.getLogger(RestErrorHandler.class);
    private final MessageTranslator messageTranslator;

    @Autowired
    public RestErrorHandler(MessageTranslator messageTranslator) {
        this.messageTranslator = messageTranslator;
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ResponseBody
    public ApiResultDto processRestValidationError(MethodArgumentNotValidException ex) {
        BindingResult result = ex.getBindingResult();
        List<FieldError> fieldErrors = result.getFieldErrors();

        return new ApiResultDto(NotificationSeverity.Error,
                "Ошибка валидации аргументов",
                fieldErrors.stream()
                        .map(fe -> new FieldErrorDto(fe.getField(), fe.getDefaultMessage()))
                        .collect(Collectors.toList()));
    }

    @ExceptionHandler(BusinessLogicException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ResponseBody
    public ApiResultDto processBusinessLogicException(BusinessLogicException ex) {
        String message = ex.getMessageKey() != null
                ? messageTranslator.translate(ex.getMessageKey(), null)
                : ex.getMessage();
        logger.debug(message, ex);
        return new ApiResultDto(NotificationSeverity.Error, message,null);
    }

    @ExceptionHandler(OptimisticLockingFailureException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    @ResponseBody
    public ApiResultDto processOptimisticLockingFailureException(OptimisticLockingFailureException ex) {
        return new ApiResultDto(NotificationSeverity.Error,
                "Ошибка совместного доступа к данным",
                "CONFLICT-ACCESS");
    }

    @ExceptionHandler(FileNameTooLongException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ResponseBody
    public ApiResultDto processPaymentDocumentNotCreatedException(FileNameTooLongException ex) {
        String message = ex.getMessage();
        logger.debug(message, ex);
        return new ApiResultDto(NotificationSeverity.Warning, message,null);
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    @ResponseBody
    public ApiResultDto processConstraintFailureException(DataIntegrityViolationException ex) {
        String message = ex.getMessage();
        logger.error(message, ex);
        return new ApiResultDto(NotificationSeverity.Error,
                "Ошибка ограничения данных. Скорее всего, не удалось удалить используемую в системе запись, или сработала проверка уникальности.",
                "CONFLICT-ACCESS");
    }
}
