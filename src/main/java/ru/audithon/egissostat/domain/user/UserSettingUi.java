package ru.audithon.egissostat.domain.user;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserSettingUi {

    private Map<String, Map<String, Boolean>> layout;

    private Map<String, Map<String, Boolean>> editPerson;

    private List<Integer> jobs;

    private Map<String, Map<String, Boolean>> dashboard;

    private Boolean visibleObserverIpra;

    private Boolean visibleAdministrationBlock;

    private Integer headerColor;

    private String headerAppTitle;

    private Integer mainInstitutionId;

    private Integer institutionRegionId;
}
