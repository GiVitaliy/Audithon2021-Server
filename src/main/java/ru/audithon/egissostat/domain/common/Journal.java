package ru.audithon.egissostat.domain.common;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.*;

import javax.validation.constraints.Max;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
public class Journal {
    public static final int DOC_CHANGE_TYPE_ADDED = 1;
    public static final int DOC_CHANGE_TYPE_MODIFIED = 2;
    public static final int DOC_CHANGE_TYPE_DELETED = 3;
    public static final int DOC_CHANGE_TYPE_ACCESS_GRANTED = 4;
    // запись для установки персонального полного доступа пользователя к объекту
    public static final int DOC_CHANGE_TYPE_USER_ACCESS_GRANTED = 5;
    // потребуется для сборки реестра передачи дел как сигнал выделенной операции передачи
    public static final int DOC_CHANGE_TYPE_WORKING_USER_CHANGED = 6;

    public static final int OBJ_KIND_PERSON_DOC = 1;
    public static final int OBJ_KIND_PERSON_COMMON = 2;
    public static final int OBJ_KIND_REQUEST = 3;
    public static final int OBJ_KIND_INSTITUTION_COMMON = 4;
    public static final int OBJ_KIND_INSTITUTION_BRANCH = 5;
    public static final int OBJ_KIND_INSTITUTION_DEPARTMENT = 6;
    public static final int OBJ_KIND_INSTITUTION_EMPLOYEE = 7;
    public static final int OBJ_KIND_INSTITUTION_RESOURCE = 8;
    public static final int OBJ_KIND_INSTITUTION_SERVICE = 9;
    public static final int OBJ_KIND_SECURITY_ROLE = 10;
    public static final int OBJ_KIND_INSTITUTION_REQUEST = 11;
    public static final int OBJ_KIND_IPRA = 12;
    public static final int OBJ_KIND_INSTITUTION_AUDIT = 13;
    public static final int OBJ_KIND_PAYMENT_DOC = 14;
    public static final int OBJ_KIND_INSTITUTION_IPRA_SETTING = 15;
    public static final int OBJ_KIND_IPRA_MARK_LIST = 16;
    public static final int OBJ_KIND_ORDER = 17;
    public static final int OBJ_KIND_ZKH_SERVICE_WAY = 18;
    public static final int OBJ_KIND_ZKH_REG_STANDART = 19;
    public static final int OBJ_KIND_QUERY_MANAGER = 20;
    public static final int OBJ_KIND_DIRECTION_MSE = 21;
    public static final int OBJ_KIND_ADDR_DELIVERY = 22;

    public static final int OBJ_KIND_MSP_TYPE = 23;
    public static final int OBJ_KIND_MSP_MONEY_SOURCE = 24;
    public static final int OBJ_KIND_MSP_TYPE_GROUP = 25;
    public static final int OBJ_KIND_MSP_REFUND_GROUP = 26;
    public static final int OBJ_KIND_DOC_T13_SUBTYPE_RATE = 27;
    public static final int OBJ_KIND_POSTAL_OFFICE = 28;
    public static final int OBJ_KIND_BANK = 29;
    public static final int OBJ_KIND_REQUEST_TYPE = 30;
    public static final int OBJ_KIND_SMEV_MESSAGE_TYPE = 31;
    public static final int OBJ_KIND_DOC_SUBTYPE = 32;

    private Integer objKindId;
    private Integer objId1;
    private Integer objId2;
    private LocalDateTime changeTime;
    private @Max(Short.MAX_VALUE) Integer changeType;
    private Integer userId;
    private Integer institutionId;
    private String ip;
    private String changeInfo;
    private Integer targetUserId;

    @JsonIgnore
    public Journal.Key getKey() {
        return new Journal.Key(objKindId, objId1, objId2);
    }

    @Data
    @AllArgsConstructor
    public static class Key {
        private Integer objKindId;
        private Integer objId1;
        private Integer objId2;
    }
}
