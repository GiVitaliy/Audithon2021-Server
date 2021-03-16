package ru.audithon.egissostat.resources;

import com.google.common.collect.Lists;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.audithon.common.notification.NotificationMessage;
import ru.audithon.common.notification.NotificationSeverity;

import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
public class ApiResultDto {
    private List<NotificationMessage> messages;
    private Object data;

    public ApiResultDto(List<NotificationMessage> messages, Object data) {
        this.messages = messages;
        this.data = data;
    }

    public ApiResultDto(NotificationMessage message, Object data) {
        this.messages = Lists.newArrayList(message);
        this.data = data;
    }

    public ApiResultDto(NotificationSeverity severity, String message, Object data) {
        this.messages = Lists.newArrayList(new NotificationMessage(severity, message));
        this.data = data;
    }

    public static ApiResultDto success(String msg, Object data) {
        ArrayList<NotificationMessage> retVal = new ArrayList<>();
        retVal.add(new NotificationMessage(NotificationSeverity.Success, msg));
        return new ApiResultDto(retVal, data);
    }

    public static ApiResultDto success(String msg) {
        return success(msg, null);
    }
}
