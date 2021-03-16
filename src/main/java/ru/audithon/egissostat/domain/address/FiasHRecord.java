package ru.audithon.egissostat.domain.address;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FiasHRecord {
    private UUID houseguid;
    private UUID aoguid;
    private String buildnum;
    private LocalDate enddate;
    private Integer eststatus;
    private UUID houseid;
    private String housenum;
    private Integer statstatus;
    private String ifnsfl;
    private String ifnsul;
    private String okato;
    private String oktmo;
    private String postalcode;
    private LocalDate startdate;
    private String strucnum;
    private Integer strstatus;
    private String terrifnsfl;
    private String terrifnsul;
    private LocalDate updatedate;
    private UUID normdoc;
    private Integer counter;
    private String cadnum;
    private Integer divtype;
    private String signature;
}
