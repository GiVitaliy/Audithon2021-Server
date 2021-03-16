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
public class FiasRRecord {
    private UUID roomguid;
    private UUID houseguid;
    private UUID roomid;
    private String regioncode;
    private String flatnumber;
    private Integer flattype;
    private String roomnumber;
    private Integer roomtype;
    private String cadnum;
    private String roomcadnum;
    private String postalcode;
    private LocalDate updatedate;
    private UUID previd;
    private UUID nextid;
    private Integer operstatus;
    private LocalDate startdate;
    private LocalDate enddate;
    private Integer livestatus;
    private UUID normdoc;
    private String signature;
}
