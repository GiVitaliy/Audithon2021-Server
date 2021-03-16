package ru.audithon.common.notification;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Objects;

@Data
@NoArgsConstructor
public class NotificationMessage {
    private NotificationSeverity severity;
    private String message;

    public NotificationMessage(NotificationSeverity severity, String message) {
        Objects.requireNonNull(severity);
        Objects.requireNonNull(message);

        this.severity = severity;
        this.message = message;
    }
}
