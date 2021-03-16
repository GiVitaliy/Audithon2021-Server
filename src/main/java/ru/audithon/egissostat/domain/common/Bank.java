package ru.audithon.egissostat.domain.common;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.Length;
import ru.audithon.common.helpers.StringUtils;

import javax.validation.constraints.Max;
import javax.validation.constraints.NotNull;
import java.time.LocalDate;
import java.time.LocalDateTime;

import static ru.audithon.common.helpers.ObjectUtils.isNull;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Bank {

    public static final Integer SBER_BIK = 47102651; // сбер
    public static final Integer KHMB_BIK = 47162740; // хмб - открытие
    public static final Integer POST_BIK = 44525214;  // почта банк

    @NotNull
    private Integer id;
    @NotNull
    @Length(max = 256)
    private String caption;
    @Length(max = 512)
    private String pfr62Code;
    @NotNull
    private Integer bankBik;
    @Length(max = 256)
    private String terrCode;
    @Length(max = 256)
    private String osbCode;
    @Length(max = 256)
    private String deptCode;
    @Length(max = 256)
    private String agreementNo;
    private LocalDate agreementDate;
    private Boolean isResident;
    @Max(Short.MAX_VALUE)
    private Integer exportTemplate; // one of BankExportTemplate record's id
    @Length(max = 256)
    private String bankFullCaption;
    private Integer maxPeople;
    private Integer reportId;
    @Length(max = 256)
    private String bankKs;
    @Length(max = 256)
    private String bankAccount;
    @Length(max = 256)
    private String bankKodZp;
    private Boolean usedByEpgu;
    private LocalDateTime dateDeleted;
    private String accountsMask;

    public void prettify() {
        caption = StringUtils.prettify(caption);
        pfr62Code = StringUtils.prettify(pfr62Code);
        terrCode = StringUtils.prettify(terrCode);
        osbCode = StringUtils.prettify(osbCode);
        deptCode = StringUtils.prettify(deptCode);
        agreementNo = StringUtils.prettify(agreementNo);
        bankFullCaption = StringUtils.prettify(bankFullCaption);
        bankKs = StringUtils.prettify(bankKs);
        bankAccount = StringUtils.prettify(bankAccount);
        bankKodZp = StringUtils.prettify(bankKodZp);
        accountsMask = StringUtils.prettify(accountsMask);
        usedByEpgu = isNull(usedByEpgu, false);
    }

    public static String getBikByName(String bankName) {
        if (StringUtils.isNullOrWhitespace(bankName)) {
            return null;
        }

        bankName = bankName.toUpperCase();

        if (bankName.contains("СБЕРБАНК")) {
            return Bank.SBER_BIK.toString();
        }

        if (bankName.contains("ОТКРЫТИЕ")) {
            return Bank.KHMB_BIK.toString();
        }

        if (bankName.contains("ПОЧТА")) {
            return Bank.POST_BIK.toString();
        }

        return null;
    }
}
