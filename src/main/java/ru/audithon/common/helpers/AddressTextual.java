package ru.audithon.common.helpers;

import com.google.common.base.Strings;
import com.google.common.collect.ListMultimap;
import com.google.common.collect.Lists;
import com.rits.cloning.Cloner;
import lombok.*;
import ru.audithon.egissostat.domain.address.City;

import java.util.*;

@Data
@NoArgsConstructor
@Builder
@AllArgsConstructor
public class AddressTextual {

    private String regionName;
    private String cityName;
    private String streetName;
    private String house;
    private String building;
    private String room;

    @Getter(AccessLevel.PRIVATE)
    @Setter(AccessLevel.PRIVATE)
    private Integer rank;

    public static List<AddressTextual> buildPossibleVariations(String addressStr, boolean preferRoom,
                                                               PrefixTree<City> citiesMap,
                                                               String regionTitleToRemove) {

        if (Strings.isNullOrEmpty(addressStr)) {
            return new ArrayList<>();
        }

        if (!Strings.isNullOrEmpty(regionTitleToRemove)) {
            addressStr = addressStr.toLowerCase().replace(regionTitleToRemove.toLowerCase(), "");
        }

        String addressNormalized = StringUtils.normalizeAddress(addressStr);
        List<String> addressTokens = Lists.newArrayList(addressNormalized.split(" "));

        // страхуемся от "кривых" данных. нормальные адреса из такого количества фрагментов не состоят.
        //если вдруг... адрес просто не распознается и логика отработает нормально, потому что это обычный сценарий
        if (addressTokens.size() > 13) {
            return new ArrayList<>();
        }

        Map<String, AddressTextual> acc = new HashMap<>();
        buildPossibleVariations(addressTokens, acc, preferRoom, citiesMap, 0);

        List<AddressTextual> retVal = new ArrayList<>(acc.values());
        retVal.sort(Comparator.comparing(AddressTextual::getRank).reversed());

        return retVal;
    }

    private static void buildPossibleVariations(List<String> addressTokens, Map<String, AddressTextual> acc,
                                                boolean preferRoom, PrefixTree<City> citiesMap, int deleteFromIx) {

        // если потенциально собранная комбинация может быть адресом - пробуем добавить её
        if (addressTokens.size() > 0 && addressTokens.size() <= 5) {

            // не добавляем вариант если название города уже точно не подойдет
            if (citiesMap.get(addressTokens.get(0)).size() > 0) {
                StringBuilder tokenId = new StringBuilder();
                for (String addressToken : addressTokens) {
                    tokenId.append(addressToken).append('$');
                }
                tryAddVariation(acc, addressTokens, preferRoom, 0, tokenId.toString());
                tryAddVariation(acc, addressTokens, preferRoom, 1, tokenId.append("&$").toString());
            }
        }

        // выходим из рекурсии если получили комбинацию из 1 элемента - названия населенного пункта
        if (addressTokens.size() <= 1) {
            return;
        }


        for (int i = 0; i < addressTokens.size(); i++) {

            List<String> addressTokens2 = new ArrayList<>(addressTokens.size() - 1);
            for (int j = 0; j < addressTokens.size(); j++) {
                if (i != j) {
                    addressTokens2.add(addressTokens.get(j));
                }
            }

            if (i >= deleteFromIx) {
                // собираем мы варианты из 2-х перестановок:
                // 1. i-й элемент удален
                buildPossibleVariations(addressTokens2, acc, preferRoom, citiesMap, i);

                // 2. i-й элемент объединен с i+1, при этом не пытаемся объединить 0-й элемент (кандидат на населенный пункт),
                //если до объединения его уже нет в префиксах населенных пунктов (т.е. после объединения он тем более не подойдет)
                if (i != 0 || citiesMap.containsPrefix(addressTokens.get(i))) {
                    if (i < addressTokens.size() - 1 && addressTokens.get(i + 1) != null) {
                        addressTokens2.set(i, addressTokens.get(i) + " " + addressTokens.get(i + 1));
                        buildPossibleVariations(addressTokens2, acc, preferRoom, citiesMap, i);
                    }
                }
            }
        }
    }

    private static void tryAddVariation(Map<String, AddressTextual> acc, List<String> levels, boolean preferRoom,
                                        int streetShift, String tokenId) {

        if (acc.containsKey(tokenId)) {
            return;
        }

        int usedTokensCount = levels.stream().map(StringUtils::spacesCount).reduce((x, y) -> x + y).orElse(0)
            + levels.size();

        AddressTextual gen = AddressTextual.builder()
            .cityName(levels.get(0))
            .streetName(streetShift == 0 ? (levels.size() > 1 ? levels.get(1) : null) : null)
            .house(levels.size() > 2 - streetShift ? levels.get(2 - streetShift) : null)
            .building(levels.size() > 3 - streetShift ? levels.get(3 - streetShift) : null)
            .room(levels.size() > 4 - streetShift ? levels.get(4 - streetShift) : null)
            .rank(levels.stream().map(x -> x == null ? 0 : 1).reduce((x, y) -> x + y).orElse(0) +
                10 * usedTokensCount)
            .build();

        if (gen.getStreetName() == null) {
            gen.setStreetName("-");
        }

        // название населенного пункта быть указано, без вариантов
        if (gen.getCityName() == null) {
            return;
        }

        // номера дома всегда с цифрой - если захватили часть без цифры или с несколькими цифрами - это неправильный адрес
        if (gen.getHouse() != null && !gen.getHouse().matches("\\d+[^\\d]*")) {
            return;
        }

        // номера квартиры всегда с цифрой - если захватили часть без цифры - это неправильный адрес
        if (gen.getRoom() != null && !gen.getRoom().matches("\\d+.*")) {
            return;
        }

        // если номер квартиры только из цифр - предпочитаем его
        if (gen.getRoom() != null && gen.getRoom().matches("\\d+")) {
            gen.setRank(gen.getRank() + 1);
        }

        // нельзя указать строение или квартиру, если не указан дом - это неправильный адрес
        if ((gen.getRoom() != null || gen.getBuilding() != null) && gen.getHouse() == null) {
            return;
        }

        // нельзя указать номер строения после буквенного обозначения
        if (gen.getBuilding() != null && gen.getBuilding().matches("[^\\s\\d]+\\s*\\d+.*")) {
            return;
        }

        if (gen.getRoom() == null && gen.getBuilding() != null && preferRoom
            && gen.getBuilding().matches("\\d+")) { // ставим возможный номер комнаты, если только он числовой
            gen.setRoom(gen.getBuilding());
            gen.setBuilding(null);
        }

        if (gen.getRoom() != null && gen.getBuilding() == null && !preferRoom) {
            gen.setBuilding(gen.getRoom());
            gen.setRoom(null);
        }

        acc.putIfAbsent(tokenId, gen);
    }
}
