package ru.audithon.egissostat.infrastructure;

import com.google.common.base.Strings;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.UncategorizedSQLException;
import org.springframework.stereotype.Service;
import ru.audithon.common.exceptions.BusinessLogicException;
import ru.audithon.common.exceptions.BusinessRuleException;

import javax.validation.constraints.NotNull;
import java.lang.reflect.UndeclaredThrowableException;
import java.sql.SQLException;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class ExceptionTranslator {
    private final MessageTranslator messageTranslator;
    private static final Pattern pgDetailsPattern = Pattern.compile("(\\n[ ]*Где: PL/pgSQL)", Pattern.CASE_INSENSITIVE);
    private static final Pattern pgDetailsErrorPattern = Pattern.compile("(ERROR:[ ]*)");

    @Autowired
    public ExceptionTranslator(MessageTranslator messageTranslator) {
        this.messageTranslator = messageTranslator;
    }

    public String translateToUser(@NotNull Throwable throwable) {
        Objects.requireNonNull(throwable);

        Throwable ex = throwable;
        if (throwable instanceof UndeclaredThrowableException && throwable.getCause() != null) {
            ex = throwable.getCause();
        }

        if (throwable instanceof UncategorizedSQLException && throwable.getCause() != null
                && throwable.getCause() instanceof SQLException) {
            ex = throwable.getCause();
        }

        if (ex instanceof SQLException) {
            String message = !Strings.isNullOrEmpty(ex.getMessage()) ? ex.getMessage() : ex.getClass().getName();
            //пытаемся вытащить из строки типа "ERROR: текст ошибки\n Где: PL/pgSQL function ..." сам текст ошибки
            Matcher pgCommentIx = pgDetailsPattern.matcher(message);
            if (pgCommentIx.find()) {
                message = message.substring(0, pgCommentIx.start());
                pgCommentIx = pgDetailsErrorPattern.matcher(message);
                if (pgCommentIx.find()) {
                    message = message.substring(pgCommentIx.end());
                }
            }

            return message;
        }

        if (ex instanceof BusinessRuleException) {
            return String.format("Ошибка проверки бизнес-правил: \"%s\"",
                    messageTranslator.translate(((BusinessRuleException)ex).getData()));
        }

        if (ex instanceof BusinessLogicException) {
            return ((BusinessLogicException)ex).getMessageKey() != null
                    ? messageTranslator.translate(((BusinessLogicException)ex).getMessageKey(), null)
                    : ex.getMessage();
        }

        return !Strings.isNullOrEmpty(ex.getMessage()) ? ex.getMessage() : ex.getClass().getName();
    }

}
