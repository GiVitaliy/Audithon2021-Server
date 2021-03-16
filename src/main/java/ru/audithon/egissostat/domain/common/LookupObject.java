package ru.audithon.egissostat.domain.common;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.Max;
import javax.validation.constraints.NotNull;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LookupObject {

    public static final LookupObject NOT_FOUND_STUB = new LookupObject(0, "Запись не найдена в БД");

    public LookupObject(Integer id, String caption) {
        this(id, caption, null);
    }

    private @NotNull
    @Max(Short.MAX_VALUE) Integer id;
    private @NotNull @Length(max = 256) String caption;
    private LocalDate dateDeleted;

}
