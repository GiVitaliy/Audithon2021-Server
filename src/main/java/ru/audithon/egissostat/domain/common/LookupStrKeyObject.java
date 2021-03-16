package ru.audithon.egissostat.domain.common;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.Max;
import javax.validation.constraints.NotNull;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LookupStrKeyObject {
    private @NotNull
    String id;
    private @NotNull
    @Length(max = 256)
    String caption;
    private @NotNull
    @Length(max = 256)
    String comments;

    public LookupStrKeyObject(String id, String caption) {
        this(id, caption, null);
    }
}
