package ru.audithon.egissostat.domain.address;

import com.google.common.base.Strings;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Objects;

import static ru.audithon.common.helpers.ObjectUtils.isNull;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AddressFull {
    private Address address;
    private CityType cityType;
    private City city;
    private StreetType streetType;
    private Street street;
    private Region region;

    public String getShortCaption() {
        return getShortCaption(false);
    }

    public String getShortCaption(boolean includeRegionCaption) {
        Objects.requireNonNull(address);

        if (!Strings.isNullOrEmpty(address.getOther())) {
            return address.getOther();
        }

        Objects.requireNonNull(cityType);
        Objects.requireNonNull(city);
        Objects.requireNonNull(streetType);
        Objects.requireNonNull(street);

        StringBuilder result = new StringBuilder();
        if (includeRegionCaption && region != null && !Objects.equals(isNull(region.getCaption(), "").toLowerCase(),
            isNull(city.getCaption(), "").toLowerCase())) {
            result.append(region.getCaption()).append(", ");
        }

        if (!Strings.isNullOrEmpty(city.getCaption()) &&
            !Strings.isNullOrEmpty(cityType.getShortCaption())) {
            result.append(cityType.getShortCaption()).append(". ").append(city.getCaption()).append(", ");
        }

        if (!Strings.isNullOrEmpty(street.getCaption()) &&
            !Strings.isNullOrEmpty(streetType.getShortCaption())) {
            result.append(streetType.getShortCaption()).append(". ").append(street.getCaption()).append(" ");
        }

        if (!Strings.isNullOrEmpty(address.getHouse())) {
            result.append(address.getHouse()).append(" ");
        }

        if (!Strings.isNullOrEmpty(address.getBuilding())) {
            result.append("стр. ").append(address.getBuilding()).append(" ");
        }

        if (!Strings.isNullOrEmpty(address.getRoom())) {
            result.append("- ").append(address.getRoom()).append(" ");
        }

        return result.toString().trim();
    }

    public String getFullCaption() {
        Objects.requireNonNull(address);

        if (!Strings.isNullOrEmpty(address.getOther())) {
            return address.getOther();
        }

        Objects.requireNonNull(cityType);
        Objects.requireNonNull(city);
        Objects.requireNonNull(streetType);
        Objects.requireNonNull(street);

        String result = "";
        if (!Strings.isNullOrEmpty(region.getCaption()) && !Objects.equals(isNull(region.getCaption(), "").toLowerCase(),
            isNull(city.getCaption(), "").toLowerCase())) {
            result += region.getCaption() + ", ";
        }

        if (!Strings.isNullOrEmpty(city.getCaption()) &&
                !Strings.isNullOrEmpty(cityType.getShortCaption())) {
            result += cityType.getShortCaption() + ". " + city.getCaption() + ", ";
        }

        if (!Strings.isNullOrEmpty(street.getCaption()) &&
                !Strings.isNullOrEmpty(streetType.getShortCaption())) {
            result += streetType.getShortCaption() + ". " + street.getCaption() + " ";
        }

        if (!Strings.isNullOrEmpty(address.getHouse())) {
            result += "д. " + address.getHouse() + " ";
        }

        if (!Strings.isNullOrEmpty(address.getBuilding())) {
            result += "стр. " + address.getBuilding() + " ";
        }

        if (!Strings.isNullOrEmpty(address.getRoom())) {
            result += "кв. " + address.getRoom() + " ";
        }

        return result.trim();
    }

    public String getCompleteCaption(String stateCaption, String zipCode) {
        Objects.requireNonNull(address);

        if (!Strings.isNullOrEmpty(address.getOther())) {
            return address.getOther();
        }

        String result = getFullCaption();

        if (!Strings.isNullOrEmpty(stateCaption)) {
            result = stateCaption + ", " + result;
        }

        if (!Strings.isNullOrEmpty(zipCode)) {
            result = zipCode + ", " + result;
        }

        return result;
    }

    public String getCaptionForNotification(String zipCode) {
        Objects.requireNonNull(address);

        if (!Strings.isNullOrEmpty(address.getOther())) {
            return address.getOther();
        }

        if (!Strings.isNullOrEmpty(address.getOther())) {
            return address.getOther() + (Strings.isNullOrEmpty(zipCode) ? "" : ", " + zipCode);
        }

        Objects.requireNonNull(cityType);
        Objects.requireNonNull(city);
        Objects.requireNonNull(streetType);
        Objects.requireNonNull(street);

        String result = "";
        if (!Strings.isNullOrEmpty(street.getCaption()) &&
                !Strings.isNullOrEmpty(streetType.getShortCaption())) {
            result += streetType.getShortCaption() + ". " + street.getCaption() + " ";
        }

        if (!Strings.isNullOrEmpty(address.getHouse())) {
            result += "д. " + address.getHouse() + " ";
        }

        if (!Strings.isNullOrEmpty(address.getBuilding())) {
            result += "стр. " + address.getBuilding() + " ";
        }

        if (!Strings.isNullOrEmpty(address.getRoom())) {
            result += "кв. " + address.getRoom() + " ";
        }

        result += "\r\n";

        if (!Strings.isNullOrEmpty(region.getCaption()) && !Objects.equals(isNull(region.getCaption(), "").toLowerCase(),
            isNull(city.getCaption(), "").toLowerCase())) {
            result += region.getCaption() + ", ";
        }

        if (!Strings.isNullOrEmpty(city.getCaption()) &&
                !Strings.isNullOrEmpty(cityType.getShortCaption())) {
            result += cityType.getShortCaption() + ". " + city.getCaption();
        }

        if (!Strings.isNullOrEmpty(zipCode)) {
            result = result + ", " + zipCode;
        }

        return result;
    }
}
