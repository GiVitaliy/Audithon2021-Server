package ru.audithon.egissostat.domain.common;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
@AllArgsConstructor
public class EgissoCategoryType {
    private Integer id;
    private String caption;

    public static List<EgissoCategoryType> getConstantDictionaryContent() {
        ArrayList<EgissoCategoryType> list = new ArrayList<>();
        list.add(new EgissoCategoryType(1, "Лицо, достигшее пенсионного возраста, лицо старшего возраста"));
        list.add(new EgissoCategoryType(2, "Инвалид"));
        list.add(new EgissoCategoryType(3, "Члены семей, потерявших кормильца"));
        list.add(new EgissoCategoryType(4, "Отдельные категории граждан, работников и специалистов"));
        list.add(new EgissoCategoryType(5, "Граждане, подвергшиеся воздействию радиации в следствие радиационных и техногенных катастроф"));
        list.add(new EgissoCategoryType(6, "Граждане, получившие трудовые увечья и профессиональные заболевания"));
        list.add(new EgissoCategoryType(7, "Беременные женщины, матери и другие категории граждан, имеющие право на получение мер социальной защиты (поддержки) семьи, материнства, отцовства и детства"));
        list.add(new EgissoCategoryType(8, "Доноры крови и ее компонентов"));
        list.add(new EgissoCategoryType(9, "Граждане, обратившиеся в органы службы занятости в целях поиска подходящей работы"));
        list.add(new EgissoCategoryType(10, "Пенсионеры"));
        list.add(new EgissoCategoryType(11, "Ветераны"));
        list.add(new EgissoCategoryType(12, "Граждане, имеющие выдающиеся достижения и особые заслуги перед Российской Федерацией"));
        list.add(new EgissoCategoryType(13, "Граждане, имеющие низкий уровень дохода, малоимущие семьи"));
        list.add(new EgissoCategoryType(14, "Граждане, признанные нуждающимися в социальном обслуживании"));
        list.add(new EgissoCategoryType(15, "Военнослужащие и приравненные к ним по социальному обеспечению и члены их семей"));
        list.add(new EgissoCategoryType(16, "Отдельные категории граждан, проживающих на территории Республики Крым"));
        return list;
    }
}
