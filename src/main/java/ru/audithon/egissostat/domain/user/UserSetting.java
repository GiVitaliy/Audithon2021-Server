package ru.audithon.egissostat.domain.user;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotNull;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserSetting {
    @NotNull
    private Integer uniqueUserId;

    private UserSettingUi userSettingUi;
}
