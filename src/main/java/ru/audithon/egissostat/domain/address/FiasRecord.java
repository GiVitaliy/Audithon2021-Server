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
public class FiasRecord {

    UUID aoguid;
    UUID aoid;
    Integer aolevel;
    Integer areacode;
    Integer autocode;
    Integer centstatus;
    Integer citycode;
    String code;
    Integer currstatus;
    LocalDate enddate;
    String formalname;
    String ifnsfl;
    String ifnsul;
    UUID nextid;
    String offname;
    String okato;
    String oktmo;
    Integer operstatus;
    UUID parentguid;
    Integer placecode;
    String plaincode;
    String postalcode;
    UUID previd;
    Integer regioncode;
    String shortname;
    LocalDate startdate;
    Integer streetcode;
    String terrifnsfl;
    String terrifnsul;
    LocalDate updatedate;
    Integer ctarcode;
    Integer extrcode;
    Integer sextcode;
    Integer livestatus;
    UUID normdoc;
    Integer plancode;
    String cadnum;
    Integer divtype;
}
