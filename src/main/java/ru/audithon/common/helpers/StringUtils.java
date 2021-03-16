package ru.audithon.common.helpers;

import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.google.common.primitives.Ints;
import lombok.SneakyThrows;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import ru.audithon.common.exceptions.BusinessLogicException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.StringReader;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import static ru.audithon.common.helpers.ObjectUtils.coalesce;
import static ru.audithon.common.helpers.ObjectUtils.isNull;

public class StringUtils {

    private final static String letterOrDigit = "[0-9а-яёА-ЯЁa-zA-Z]";
    private final static String letter = "[а-яёА-ЯЁa-zA-Z]";
    private final static String digit = "[0-9]";
    private final static String building = "^(стр|строение)$";

    public static String prettify(String str) {
        if (str != null && str.trim().equals("")) {
            str = null;
        }
        if (str != null) {
            str = str.trim();
        }
        return str;
    }

    public static String prettify(String str, int maxLength) {
        str = prettify(str);

        if (str != null && str.length() > maxLength) {
            str = maxLength > 0 ? str.substring(0, maxLength - 1) : "";
        }

        return str;
    }

    public static String toUpperCaseFirst(String str) {
        if (Strings.isNullOrEmpty(str)) {
            return str;
        }

        return str.substring(0, 1).toUpperCase() + str.substring(1);
    }

    public static String processPersonNameCasing(String str) {
        String value = removeExtraSpaces(str, true);
        if (StringUtils.isNullOrWhitespace(str)) {
            return null;
        }

        if (Objects.equals(str, "-")) {
            return str;
        }

        value = value.toLowerCase();

        // разделяем по пробелам
        String[] split = value.split("\\s");
        if (split.length == 1) {
            // не составная
            value = toUpperCaseFirst(split[0]);
        } else {
            value = Arrays.stream(split).map(StringUtils::toUpperCaseFirst).collect(Collectors.joining(" "));
        }

        // разделяем по '-'
        split = value.split("-");
        if (split.length == 1) {
            // не составная
            value = toUpperCaseFirst(split[0]);
        } else {
            value = Arrays.stream(split).map(StringUtils::toUpperCaseFirst).collect(Collectors.joining("-"));
        }

        return value;
    }

    public static String processDataTextField(String str) {
        String value = removeExtraSpaces(str);
        if (value != null && value.length() > 0) {
            return value;
        }
        return null;
    }

    // Удаляет подряд идущие пробелы, заменяя их на один пробел
    public static String removeExtraSpaces(String str) {
        return removeExtraSpaces(str, false);
    }

    // Удаляет подряд идущие пробелы, заменяя их на один пробел
    public static String removeExtraSpaces(String str, boolean removeSpaceAfterHyphen) {
        if (Strings.isNullOrEmpty(str)) {
            return str;
        }

        // пробелы: "aa    bbb" -> "aa bbb"
        String value = str.trim().replaceAll("\\s{2,}", " ");

        if (removeSpaceAfterHyphen) {
            value = value.replaceAll("-\\s+", "-");
        }

        // пробелы перед тире: "aa  -bbb" -> "aa-bbb"
        return value.replaceAll("\\s+(?=-)", "");
    }

    public static String normalizeAddress(String address) {
        // Реализовывать алгоритм будем так:
        // 1) извлекаем подряд лексемы, которые у нас будут такие:
        //    1.1)последовательность букв, заканчивающаяся любыми не-буквами
        //    1.2)последовательность цифр, заканчивающаяся не цифрами
        // 2) При извлечении лексемы, все символы перед ней, которые не буквы и не цифры - пропускаем
        // 3) Промежутки между лексемами заполняем единичным пробелом
        // 4) Лексемы из списка незначимых пропускаем (игнорируем, не считаем лексемами)

        int currentPos = 0;
        String prevLexeme = null;
        String prevLexemeStrong = null;
        StringBuilder retVal = new StringBuilder();

        while (currentPos < address.length()) {

            AddressPart part = getNextAddressLexeme(currentPos, address);
            currentPos = part.position;
            String lexeme = part.value;

            boolean isExcluded = excludedLexemes.contains(lexeme);

            // Если исключённая лексема состоит из 1-й буквы (не 'й' и не 'n' (номер)) и она идёт после числовой
            // лексемы, то считаем её литерой объекта и не пропускаем её
            if (isExcluded && lexeme.length() == 1 &&
                    ((!Strings.isNullOrEmpty(prevLexeme) && !lexeme.equals("й") && !lexeme.equals("n") &&
                    prevLexeme.substring(0, 1).matches(digit) )
                            || (!Strings.isNullOrEmpty(prevLexemeStrong) && prevLexemeStrong.matches(building)))) {
                isExcluded = false;
            }

            prevLexemeStrong = lexeme;
            if (Strings.isNullOrEmpty(lexeme) || isExcluded) {
                prevLexeme = "";
                continue;
            }

            if (retVal.length() > 0) {
                retVal.append(" ");
            }

            retVal.append(lexeme);
            prevLexeme = lexeme;
        }

        return retVal.toString().replace("ё", "е");
    }

    public static String prettifyXmlString(String str) {
        if (str == null) {
            return "";
        }

        // StringEscapeUtils.escapeXml(str); - вот эта стандартная хрень эскейпит и русские символы, что приводит
        // к очень плохой читаемости результата, поэтому юзаем свой код
        return escapeXml(str);
    }

    /**
     * Encodes special characters by standard XML predefined entities.
     */
    private static String escapeXml(String str) {
        StringBuilder sb = new StringBuilder(str);
        boolean changed = false;
        for (int i = 0, j = sb.length(); i < j; i++) {
            int ch = sb.charAt(i);
            switch (ch) {
                case '&':
                    sb.insert(i + 1, "amp;");
                    j += 4;
                    changed = true;
                    break;
                case '<':
                    sb.setCharAt(i, '&');
                    sb.insert(i + 1, "lt;");
                    j += 3;
                    changed = true;
                    break;
                case '>':
                    sb.setCharAt(i, '&');
                    sb.insert(i + 1, "gt;");
                    j += 3;
                    changed = true;
                    break;
                case '\'':
                    sb.setCharAt(i, '&');
                    sb.insert(i + 1, "apos;");
                    j += 5;
                    changed = true;
                    break;
                case '"':
                    sb.setCharAt(i, '&');
                    sb.insert(i + 1, "quot;");
                    j += 5;
                    changed = true;
                    break;
            }
        }
        return changed ? sb.toString() : str;
    }

    // Выдает последнюю часть строки, отделенную от других частей пробелами
    public static String getLastToken(String addressText) {
        if (addressText == null) {
            return null;
        }

        Integer pos = addressText.lastIndexOf(" ");
        if (pos < 0) {
            return addressText;
        } else {
            return addressText.substring(pos + 1);
        }
    }

    private static class AddressPart {
        public int position;
        public String value;

        public AddressPart(int position, String value) {
            this.position = position;
            this.value = value;
        }
    }

    private static AddressPart getNextAddressLexeme(int currentPos, String address) {
        while (currentPos < address.length() &&
            !address.substring(currentPos, currentPos + 1).matches(letterOrDigit)) {
            currentPos++;
        }

        if (currentPos >= address.length()) {
            return new AddressPart(currentPos, "");
        }

        StringBuilder sb = new StringBuilder();
        if (address.substring(currentPos, currentPos + 1).matches(letter)) {
            while (currentPos < address.length() &&
                address.substring(currentPos, currentPos + 1).matches(letter)) {
                sb.append(Character.toLowerCase(address.charAt(currentPos++)));
            }
        } else {
            while (currentPos < address.length() &&
                address.substring(currentPos, currentPos + 1).matches(digit)) {
                sb.append(address.charAt(currentPos++));
            }
        }

        return new AddressPart(currentPos, sb.toString());
    }

    private static Set<String> excludedLexemes = Sets.newHashSet(
            "а",
        "окр",
        "аллея",
        "бульвар",
        "г",
        "гор",
        "город",
        "д",
        "дер",
        "днт",
        "деревня",
        "м",
        "мкр",
        "мр",
        "й",
        "к",
        "кв",
        "квартира",
        "корп",
        "корпус",
        "микрорайон",
        "п",
        "пгт",
        "пер",
        "пл",
        "площ",
        "площадь",
        "переулок",
        "пос",
        "поселок",
        "посёлок",
        "пр",
        "проезд",
        "просп",
        "проспект",
        "район",
        "с",
        "сел",
        "село",
        "снт",
        "сонт",
        "ст",
        "станица",
        "станция",
        "стр",
        "строение",
        "тер",
        "ул",
        "улица",
        "ш",
        "шоссе",
        "n");

    public static String innflToStr(long innfl) {
        return String.format("%012d", innfl);
    }

    public static Long strToInnfl(String innfl) {
        innfl = prettify(innfl);

        if (innfl == null) {
            return null;
        }
        try {
            if (innfl.length() != 12) {
                throw new NumberFormatException();
            }
            return Long.parseLong(innfl);
        } catch (NumberFormatException ex) {
            throw new NumberFormatException("Некорректный формат ИНН. ИНН должен состоять из 12 цифр");
        }
    }

    public static String snilsToStr(long snils) {
        return String.format("%03d", snils / 100000000)
            + "-" + String.format("%03d", (snils / 100000) % 1000)
            + "-" + String.format("%03d", (snils / 100) % 1000)
            + " " + String.format("%02d", snils % 100);
    }

    public static String snilsToStrNoSpaces(long snils) {
        return String.format("%03d", snils / 100000000)
            + String.format("%03d", (snils / 100000) % 1000)
            + String.format("%03d", (snils / 100) % 1000)
            + String.format("%02d", snils % 100);
    }

    public static String snilsToStrOpt(Long snils) {
        if (snils == null) {
            return "";
        }
        return snilsToStr(snils);
    }

    public static Long strToSnils(String snilsStr) {
        snilsStr = prettify(snilsStr);

        if (snilsStr == null) {
            return null;
        }

        try {
            return Long.parseLong(snilsStr.replace(" ", "").replace("-", ""));
        } catch (NumberFormatException ex) {
            throw new NumberFormatException("Некорректный формат СНИЛС. Снилс должен состоять из 11 цифр без разделителей " +
                "или с разделителями ' ' или '-'");
        }
    }

    /*
     * Преобразует переданный список в строковое представление, разделенное запятыми с пробелами
     * */
    public static <T> String listToString(List<T> list) {

        StringBuilder sb = new StringBuilder();

        boolean isFirst = true;

        for (T item : list) {

            if (!isFirst) {
                sb.append(", ");
            } else {
                isFirst = false;
            }

            if (item != null) {
                sb.append(item.toString());
            } else {
                sb.append("null");
            }
        }

        return sb.toString();
    }

    /*
     * Преобразует переданный массив в строковое представление, разделенное запятыми с пробелами
     * */
    public static <T> String arrayToString(T[] array) {

        if (array == null || array.length == 0) {
            return "null";
        }

        StringBuilder sb = new StringBuilder();

        sb.append(array[0] != null ? array[0].toString() : "null");

        for (int i = 1; i < array.length; i++) {
            sb.append(",");
            sb.append(array[i] != null ? array[i].toString() : "null");
        }

        return sb.toString();
    }

    public static boolean isNullOrWhitespace(String value) {
        if (Strings.isNullOrEmpty(value)) return true;

        int len = value.length();
        int st = 0;

        while ((st < len) && (value.charAt(st) == ' ')) {
            st++;
        }
        return (st == len);
    }

    public static class StringParsingException extends RuntimeException {
        public StringParsingException(String value, Class<?> cls) {
            super(String.format("Невозможно преобразовать строку \"%s\" в значение типа %s", value, cls));
        }

        StringParsingException(String value, Class<?> cls, Throwable inner) {
            super(String.format("Невозможно преобразовать строку \"%s\" в значение типа %s", value, cls), inner);
        }
    }

    public static String extractDigits(String value) {
        return value != null ? value.replaceAll("[^0-9]", "") : null;
    }

    public static Integer extractNumber(String value) {
        try {
            return value != null ? parse(extractDigits(value), Integer.class) : null;
        } catch (StringUtils.StringParsingException ex) {
            return null;
        }
    }

    private static final List<DateTimeFormatter> supportedFormatters = Lists.newArrayList(
        DateTimeFormatter.BASIC_ISO_DATE,
        DateTimeFormatter.ISO_DATE,
        DateUtils.ruFormatterDate,
        DateUtils.ruFormatterDateShort,
        DateUtils.pfrFormatterDate
    );

    @SuppressWarnings("unchecked")
    public static <T> T parse(String value, Class<T> cls) throws StringParsingException {
        if (cls == Integer.class) {
            try {
                return (T) (Integer) Integer.parseInt(value);
            } catch (NumberFormatException ex) {
                throw new StringParsingException(value, cls, ex);
            }
        }

        if (cls == BigDecimal.class) {
            try {
                return (T) new BigDecimal(coalesce(prettify(value), ""));
            } catch (NumberFormatException ex) {
                throw new StringParsingException(value, cls, ex);
            }
        }

        if (cls == Boolean.class)
            return (T) (Boolean) Boolean.parseBoolean(value);

        if (cls == LocalDate.class) {
            int counter = 0;
            for (DateTimeFormatter supportedFormatter : supportedFormatters) {
                counter++;
                try {
                    return (T) LocalDate.parse(value, supportedFormatter);
                } catch (DateTimeParseException ex) {
                    if (supportedFormatters.size() == counter) {
                        throw new StringParsingException(value, cls, ex);
                    }
                }
            }
        }

        throw new StringParsingException(value, cls);
    }

    public static BigDecimal readBigDecimal(String val) {
        try {
            return StringUtils.parse(val, BigDecimal.class);
        } catch (StringUtils.StringParsingException ex) {
            return null;
        }
    }

    public static Integer readInteger(String val) {
        try {
            return StringUtils.parse(val, Integer.class);
        } catch (StringUtils.StringParsingException ex) {
            return null;
        }
    }

    public static String normalizeSeries(String series, String seriesFormat) {
        Objects.requireNonNull(series, "series is null");
        Objects.requireNonNull(seriesFormat, "seriesFormat is null");

        String newSeries = series.trim();
        // случай, если через пробел, а не через '-' записана серия: добавляет между двумя компонентами "-"
        if (!newSeries.contains("-")) {
            int spacePos = newSeries.indexOf(" ");
            if (spacePos > 0) {
                newSeries = newSeries.substring(0, spacePos) + "-" + newSeries.substring(spacePos + 1);
            }
        }

        newSeries = newSeries.replaceAll("\\s+", "").toUpperCase();

        switch (seriesFormat) {
            case "9999": // Формат паспорта
            {
                return newSeries.replaceAll("[\\s-]+", "");
            }

            case "RR-ББ": // Римские - тире - буквы
            {
                String[] split = newSeries.split("-");
                if (split.length != 2) {
                    throw new BusinessLogicException("Неверный формат серии документа");
                }

                return processSeriesParts(split[0]) + "-" + split[1];
            }

            case "RR-ББ/ББ": // Римские - тире - буквы (например, серия свид-ва о рождении) или просто 2 буквы
            {
                String[] split = newSeries.split("-");
                if (split.length == 2) {
                    return processSeriesParts(split[0]) + "-" + split[1];
                }
            }
        }

        return newSeries;
    }

    public static String processSeriesParts(String seriesPart) {
        String result = seriesPart
            .replace("Y", "V")
            .replace("У", "V")
            .replace("Х", "X")
            .replace("Л", "L")
            .replace("С", "C");

        Integer seriesPartAsNumber = Ints.tryParse(result);
        if (seriesPartAsNumber == null) {
            // если не распознали как арабское число, считаем его римским
            result = result.replace("1", "I");
        } else {
            // если распознали числовую часть как арабское число, преобразуем его в римское
            switch (seriesPartAsNumber) {
                case 11:
                    result = "II";
                    break;
                case 111:
                    result = "III";
                    break;
                default:
                    result = RomanNumberConverter.toRoman(seriesPartAsNumber);
                    break;
            }

        }

        return result;
    }

    /**
     * корректно работает далеко не на всех номерах счетов
     *
     * @param bankId
     * @param str
     * @return
     */
    public static boolean isValidBankAccount(int bankId, String str) {
        return isValidBankAccount(bankId, str, true);
    }

    /**
     * корректно работает далеко не на всех номерах счетов
     *
     * @param bankId
     * @param str
     * @param checkCtrl
     * @return
     */
    public static boolean isValidBankAccount(int bankId, String str, boolean checkCtrl) {
        try {
            strToBankAccount(bankId, str, checkCtrl);
            return true;
        } catch (Exception ex) {
            return false;
        }
    }

    /**
     * корректно работает далеко не на всех номерах счетов
     *
     * @param bankId
     * @param str
     * @param checkCtrl
     * @return
     */
    public static BigInteger strToBankAccount(int bankId, String str, boolean checkCtrl) {
        BigInteger retVal;
        if (str == null)
            return null;

        if (str.length() != 20) {
            throw new BusinessLogicException(null,
                "Некорректный формат счета \"%s\". Номер счета в банке должен состоять из 20 цифр", str);
        }

        try {
            retVal = new BigInteger(str);
        } catch (NumberFormatException ex) {
            throw new BusinessLogicException(null,
                "Некорректный формат счета \"%s\". Номер счета в банке должен состоять из 20 цифр", str);
        }

        if (!checkCtrl) return retVal;

        int calcControlDigit = calculateBankAccountControlDigit(bankId, str);
        int controlDigit;
        controlDigit = Integer.parseInt(str.substring(8, 9));

        if (calcControlDigit != controlDigit)
            throw new BusinessLogicException(null,
                "Некорректный формат счета \"%s\". Не совпадает контрольная цифра.", str);

        return retVal;
    }

    private static final int SysTerrCount = 100;
    private static final int SYS_MAX_ID = Integer.MAX_VALUE / SysTerrCount; // 21474836
    private static final int SysCondCount = 1000;
    private static final int SYS_MAX_BANK_ID = SYS_MAX_ID / SysCondCount;

    private static int calculateBankAccountControlDigit(int bankId, String str) {
        int conditionalRkc = bankId % SYS_MAX_ID / SYS_MAX_BANK_ID;
        str = String.format("%03d", conditionalRkc) + str;
        if (str.length() != 23)
            throw new BusinessLogicException(null, "Некорректный номер счета \"%s\" для расчета контрольной цифры", str);
        int[] weights = new int[]{7, 1, 3, 7, 1, 3, 7, 1, 3, 7, 1, 3, 7, 1, 3, 7, 1, 3, 7, 1, 3, 7, 1};
        int sum = 0;
        for (int i = 0; i < 23; i++) {
            int cDigit = i != 11 ? Integer.parseInt(str.substring(i, i + 1)) : 0;
            sum = sum + cDigit * weights[i] % 10;
        }

        return sum % 10 * 3 % 10;
    }

    public static boolean isInteger(String s) {
        if (s == null || s.length() == 0 || s.length() > 9) {
            return false;
        }

        for (int i = 0; i < s.length(); i++) {
            if (!Character.isDigit(s.charAt(i))) {
                return false;
            }
        }

        return true;
    }

    // преобразует XML в иерархическое представление без тегов, где названия тегов заменяются но форме "[Название]: содержимое"
    //с отступами, как в yml
    @SneakyThrows
    public static String getUntaggedXmlText(String xmlString) {
        if (Strings.isNullOrEmpty(xmlString)) {
            return "";
        }

        xmlString = xmlString.replace("<?xml version=\"1.0\" encoding=\"windows-1251\"?>", "");

        DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();

        StringBuilder prettified = new StringBuilder();
        try {
            Document doc = dBuilder.parse(new InputSource(new StringReader(xmlString)));

            //optional, but recommended
            //read this - http://stackoverflow.com/questions/13786607/normalization-in-dom-parsing-with-java-how-does-it-work
            doc.getDocumentElement().normalize();

            printNode(doc, prettified, 0);
        } catch (SAXException ex) {
            // возвращаем необработанным, если не смогли распарсить - такое вполне может быть - ответ
            // текстом, например текстом ощибки
            return xmlString;
        }

        return prettified.toString();
    }

    private static void printNode(Node node, StringBuilder prettified, int intend) {

        String prettifiedNodeName = node.getNodeName();

        String intendStr = Strings.padStart("", intend, '\t');

        if (!prettifiedNodeName.matches(".*[а-яёА-ЯЁ]+.*")) {
            prettifiedNodeName = null;
        }

        if (prettifiedNodeName != null) {
            prettifiedNodeName = prettifiedNodeName.replace("_", " ").trim();
            if (Objects.equals(prettifiedNodeName, "")) {
                prettifiedNodeName = null;
            }
        }

        if (hasElementNodes(node)) {
            if (prettifiedNodeName != null) {
                prettified.append(intendStr);
                prettified.append(prettifiedNodeName);
                prettified.append(":\n");
            }
            if (node.hasAttributes()) {
                NamedNodeMap attrs = node.getAttributes();
                for (int i = 0; i < attrs.getLength(); i++) {
                    printNode(attrs.item(i), prettified, prettifiedNodeName != null ? intend + 1 : intend);
                }
            }

            for (int i = 0; i < node.getChildNodes().getLength(); i++) {
                printNode(node.getChildNodes().item(i), prettified, prettifiedNodeName != null ? intend + 1 : intend);
            }
        } else {
            if (prettifiedNodeName != null) {
                prettified.append(intendStr);
                prettified.append(prettifiedNodeName);
                prettified.append(": ");
                String str = isNull(trimWithControlChars(node.getTextContent()), "<<не указано>>");
                prettified.append(str);
                if (!str.endsWith("\n") && !Strings.isNullOrEmpty(str)) {
                    prettified.append("\n");
                }
            }
        }
    }

    public static String trimWithControlChars(String textContent) {
        if (textContent == null) {
            return null;
        }
        int start = 0;
        int end = textContent.length() - 1;
        while (start <= end && Character.isISOControl(textContent.charAt(start))) {
            start++;
        }
        while (start <= end && Character.isISOControl(textContent.charAt(end))) {
            end--;
        }
        if (start <= end) {
            return textContent.substring(start, end + 1);
        } else {
            return null;
        }
    }

    private static boolean hasElementNodes(Node node) {
        if (node.getNodeType() == Node.ATTRIBUTE_NODE) {
            return false; // у атрибутов нет дочерних элементов
        }

        for (int i = 0; i < node.getChildNodes().getLength(); i++) {
            if (node.getChildNodes().item(i) instanceof Element) {
                return true;
            }
        }
        return node.hasAttributes();
    }

    private final static BigDecimal MONEY_CAPTION_LIMIT = BigDecimal.valueOf(1000000000000000000L);
    private final static String[] degreeCaptionsSingle = new String[]{"рубль", "тысяча", "миллион", "миллиард", "триллион", "квадриллион"};
    private final static String[] degreeCaptionsMulti = new String[]{"рублей", "тысяч", "миллионов", "миллиардов", "триллионов", "квадриллионов"};
    private final static String[] degreeCaptionsMulti2 = new String[]{"рубля", "тысячи", "миллиона", "миллиарда", "триллиона", "квадриллиона"};
    private final static Boolean[] degreeIsMale = new Boolean[]{true, false, true, true, true, true};
    private final static String[] digitsMale = new String[]{
        "", "один", "два", "три", "четыре", "пять", "шесть", "семь", "восемь", "девять", "десять", "одиннадцать",
        "двенадцать", "тринадцать", "четырнадцать", "пятнадцать", "шестнадцать", "семнадцать", "восемнадцать",
        "девятнадцать"
    };
    private final static String[] digitsFemale = new String[]{
        "", "одна", "две", "три", "четыре", "пять", "шесть", "семь", "восемь", "девять", "десять", "одиннадцать",
        "двенадцать", "тринадцать", "четырнадцать", "пятнадцать", "шестнадцать", "семнадцать", "восемнадцать",
        "девятнадцать"
    };
    private final static String[] tens = new String[]{
        "", "десять", "двадцать", "тридцать", "сорок", "пятьдесят", "шестьдесят", "семьдесят", "восемьдесят", "девяносто"
    };
    private final static String[] hundreds = new String[]{
        "", "сто", "двести", "триста", "четыреста", "пятьсот", "шестьсот", "семьсот", "восемьсот", "девятьсот"
    };

    public static String formatRuDecimalTextual(BigDecimal amount) {
        if (amount == null) return "";

        if (amount.compareTo(MONEY_CAPTION_LIMIT) >= 0) return "очень много рублей";

        BigDecimal remainderDecimal = amount.setScale(0, RoundingMode.DOWN);
        BigInteger partDegree = amount.subtract(remainderDecimal).multiply(BigDecimal.valueOf(100))
            .setScale(0, RoundingMode.HALF_UP).toBigInteger();
        BigInteger remainder = remainderDecimal.toBigInteger();


        BigInteger currentDegree = BigInteger.ZERO;
        BigInteger thousand = BigInteger.valueOf(1000);
        BigInteger hundred = BigInteger.valueOf(100);
        BigInteger ten = BigInteger.TEN;
        BigInteger two = BigInteger.valueOf(2);
        BigInteger three = BigInteger.valueOf(3);
        BigInteger four = BigInteger.valueOf(4);

        StringBuilder retVal = new StringBuilder();

        retVal.insert(0, " коп.");
        retVal.insert(0, String.format("%02d", partDegree));

        if (remainder.equals(BigInteger.ZERO))
            retVal.insert(0, "ноль рублей ");

        while (!remainder.equals(BigInteger.ZERO)) {
            BigInteger digitsDegree = remainder.divideAndRemainder(thousand)[1];
            remainder = remainder.divide(thousand);
            BigInteger cHundred = digitsDegree.divide(hundred);
            BigInteger cTen = (digitsDegree.subtract(cHundred.multiply(hundred))).divide(ten);
            BigInteger cDigit = digitsDegree.subtract(cHundred.multiply(hundred)).subtract(cTen.multiply(ten));

            if (!digitsDegree.equals(BigInteger.ZERO) || currentDegree.equals(BigInteger.ZERO)) {
                retVal.insert(0, " ");

                if (cDigit.equals(BigInteger.ONE) && !cTen.equals(BigInteger.ONE))
                    retVal.insert(0, degreeCaptionsSingle[currentDegree.intValue()]);
                else if ((cDigit.equals(two) || cDigit.equals(three) || cDigit.equals(four)) && !cTen.equals(BigInteger.ONE))
                    retVal.insert(0, degreeCaptionsMulti2[currentDegree.intValue()]);
                else
                    retVal.insert(0, degreeCaptionsMulti[currentDegree.intValue()]);

                if (cTen.multiply(ten).add(cDigit).compareTo(BigInteger.ZERO) > 0) {
                    retVal.insert(0, " ");
                    if (cTen.compareTo(two) < 0 && degreeIsMale[currentDegree.intValue()])
                        retVal.insert(0, digitsMale[cTen.multiply(ten).add(cDigit).intValue()]);
                    else if (cTen.compareTo(two) < 0 && !degreeIsMale[currentDegree.intValue()])
                        retVal.insert(0, digitsFemale[cTen.multiply(ten).add(cDigit).intValue()]);
                    else {
                        retVal.insert(0, degreeIsMale[currentDegree.intValue()] ? digitsMale[cDigit.intValue()] : digitsFemale[cDigit.intValue()]);

                        if (cTen.compareTo(BigInteger.ZERO) > 0) {
                            retVal.insert(0, " ");
                            retVal.insert(0, tens[cTen.intValue()]);
                        }
                    }
                }

                if (cHundred.compareTo(BigInteger.ZERO) > 0) {
                    retVal.insert(0, " ");
                    retVal.insert(0, hundreds[cHundred.intValue()]);
                }
            }

            currentDegree = currentDegree.add(BigInteger.ONE);
        }

        return retVal.toString();
    }

    public static BigDecimal stringToPassportDepartmentCode(String departmentCode) {
        try {
            departmentCode = StringUtils.prettify(departmentCode);
            if (departmentCode == null) {
                return null;
            }
            departmentCode = departmentCode.replace(" ", "");
            departmentCode = departmentCode.replace("-", "");

            return BigDecimal.valueOf(Long.parseLong(departmentCode));
        } catch (Exception ex) {
            return null;
        }
    }

    public static String passportDepartmentCodeToString(BigDecimal departmentCode) {
        if (departmentCode == null) {
            return null;
        }

        int codeInt = departmentCode.intValue();
        return String.format("%03d-%03d", codeInt / 1000, codeInt % 1000);
    }


    public static String getDocNumberFormat(int docSubtypeId) {
        switch (docSubtypeId) {
            case 1:
            case 2:
            case 3:
                return "%06d";
            default:
                return "%d";
        }
    }

    public static String formatDocNumber(Integer docSubtypeId, Long docNumber) {
        if (docNumber == null) {
            return null;
        }
        if (docSubtypeId == null) {
            return docNumber.toString();
        }
        return String.format(getDocNumberFormat(docSubtypeId), docNumber);
    }

    private static final DecimalFormat ruMoneyDecimalFormat = new DecimalFormat("0.00");
    private static final DecimalFormat ruMoneyDecimalFormat4 = new DecimalFormat("0.0000");

    public static String formatRuDecimal(BigDecimal amount) {
        if (amount == null) {
            return "";
        } else {
            return ruMoneyDecimalFormat.format(amount);
        }
    }

    public static String formatRuDecimal4(BigDecimal amount) {
        if (amount == null) {
            return "";
        } else {
            return ruMoneyDecimalFormat4.format(amount);
        }
    }

    public static String formatInvariantDecimal(BigDecimal amount) {
        if (amount == null) {
            return "";
        } else {
            return ruMoneyDecimalFormat.format(amount).replace(',', '.');
        }
    }

    public static String formatInvariantDecimal4(BigDecimal amount) {
        if (amount == null) {
            return "";
        } else {
            return ruMoneyDecimalFormat4.format(amount).replace(',', '.');
        }
    }

    public static String prettifyFileName(String fileName) {
        return fileName.replaceAll("[^а-яёА-ЯЁa-zA-Z0-9\\.\\-]", "_");
    }

    public static String sanitizeFileName(String fileName) {
        return fileName.replaceAll("[:\\\\/*?|<>]", "_");
    }

    public static Boolean suggestGenderByFio(String lastName, String firstName, String middleName) {
        Boolean result = suggestGenderByMiddleName(middleName);

        if (result == null) {
            return suggestGenderByLastName(lastName);
        }

        return result;
    }

    public static Boolean suggestGenderByMiddleName(String middleName) {
        if (isNullOrWhitespace(middleName)) {
            return null;
        }

        if (middleName.length() < 2) {
            return null;
        }

        String middleNameLower = middleName.toLowerCase();
        String suffix = middleNameLower.substring(middleNameLower.length() - 2);

        if (suffix.equals("ич")
            || suffix.equals("ов")
            || suffix.equals("ев")
            || suffix.equals("лы") // оглы, улы
            || middleNameLower.endsWith("угли")
            || middleNameLower.endsWith("уулу")
            ) {
            return true; // муж.
        }

        if (suffix.equals("на") // -вна, -чна, -ина
            || suffix.equals("ва")
            || suffix.equals("зы") // кызы, гызы, кзы
            || suffix.equals("зи") // кизи
            ) {
            return false; // жен.
        }

        return null;
    }

    public static Boolean suggestGenderByLastName(String lastName) {
        if (isNullOrWhitespace(lastName)) {
            return null;
        }

        // в базе ошибочных полов приличное количество, например 536 мужчин с отчество Александровна
        // а в сумме 7021 мужчин с отчеством на -вна,
        // так что число противоположных результатов для окончаний чаще ошибка данных, чем реальная ситуация

        if (lastName.length() < 2) {
            return null;
        }

        String suffix = lastName.substring(lastName.length() - 2).toLowerCase();
        if (suffix.equals("ев") // 121690 против 491
            || suffix.equals("ов") // 313215 против 1213
            || suffix.equals("ин") // 101512 против 818
            || suffix.equals("ый") // 3305 против 35
            || suffix.equals("ий") // 21725 против 1444
            ) {
            return true; // муж.
        }

        if (suffix.equals("ва") // 588707 против 4712
            || suffix.equals("на") // 143124 против 1593
            || suffix.equals("ая") // 36801 против 360
            ) {
            return false; // жен.
        }

        return null;
    }

    public static Boolean strToBoolGender(String gender) {
        if (StringUtils.isNullOrWhitespace(gender)) {
            return null;
        }

        char firstChar = gender.toUpperCase().charAt(0);
        if (Objects.equals(firstChar, 'М') || Objects.equals(firstChar, 'M')) {
            return true;
        }

        if (Objects.equals(firstChar, 'Ж') || Objects.equals(firstChar, 'F')) {
            return false;
        }

        return null;
    }

    public static String[] splitFullName(String fullName) {
        String[] result = new String[3];
        String[] fio = fullName.split(" ");
        if (fio.length > 0) {
            result[0] = fio[0];
        }
        if (fio.length > 1) {
            result[1] = fio[1];
        }
        if (fio.length > 2) {
            result[2] = fio[2];
        }
        // всё остальное складываем в отчество
        if (fio.length > 3) {
            for (int i = 3; i < fio.length; i++) {
                result[2] += " " + fio[i];
            }
        }
        return result;
    }

    public static String joinFullName(String lastName, String firstName, String middleName) {
        return String.format("%s %s%s",
            lastName, firstName, isNullOrWhitespace(middleName) ? "" : " " + middleName);
    }

    public static String takeNumbers(String str) {
        if (str == null || str.length() == 0) {
            return str;
        }

        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < str.length(); i++) {
            char c = str.charAt(i);
            if (Character.isDigit(c)) {
                sb.append(c);
            }
        }

        return sb.toString();
    }

    public static String replaceLatinTwinToCyrillic(String str) {
        if (isNullOrWhitespace(str)) {
            return str;
        }

        return str
            .replaceAll("\\u00eb", "ё") // Latin Small Letter E with Diaeresis
            .replaceAll("\\u00cb", "Ё") // Latin Capital Letter E with Diaeresis
            .replaceAll("c", "с")
            .replaceAll("C", "С")
            .replaceAll("e", "е")
            .replaceAll("E", "Е")
            .replaceAll("B", "В")
            .replaceAll("p", "р")
            .replaceAll("P", "P")
            .replaceAll("T", "Т")
            .replaceAll("o", "о")
            .replaceAll("O", "О")
            .replaceAll("a", "а")
            .replaceAll("A", "А")
            .replaceAll("x", "х")
            .replaceAll("X", "Х")
            .replaceAll("M", "М")
            .replaceAll("H", "Н")
            .replaceAll("K", "К")
            .replaceAll("y", "у");
    }

    public static String replaceRussianYoToE(String str) {
        if (isNullOrWhitespace(str)) {
            return str;
        }

        return str.replaceAll("Ё", "Е").replaceAll("ё", "е");
    }

    public static int spacesCount(String str) {
        if (Strings.isNullOrEmpty(str))
            return 0;

        int retVal = 0;

        for(int i = 0; i < str.length(); i++) {
            if (str.charAt(i) == ' ') {
                retVal++;
            }
        }

        return retVal;
    }

    public static String limitFileNameBytes(String fileName, int limit) {
        Objects.requireNonNull(fileName, "Имя файла не задано");
        if (limit <= 0) {
            throw new IllegalArgumentException("Значение аргумента limit должно быть больше нуля");
        }

        if (fileName.getBytes().length <= limit) {
            return fileName;
        }

        String fileNameWithoutExtension = fileName;
        String extension = "";

        int dotPos = fileName.lastIndexOf('.');
        if (dotPos >= 0) {
            fileNameWithoutExtension = fileName.substring(0, dotPos);
            extension = fileName.substring(dotPos);
        }

        int i = 1;
        while (fileName.getBytes().length > limit) {
            if (i < fileNameWithoutExtension.length()) {
                fileNameWithoutExtension = fileNameWithoutExtension.substring(0, fileNameWithoutExtension.length() - 1);
            } else {
                extension = extension.substring(0, extension.length() - 1);
            }

            fileName = fileNameWithoutExtension + extension;
            i++;
        }

        return fileName;
    }

    public static String extractSeriaParts(String seria, int partNo) {
        // тут какие-то дикие требования в почтовом формате. нужно разделять 2 части серии - римские и русские буквы, причем делать это надо в том числе
        //для паспорта РФ, который разделяется на 2 группы по 2 цифры и также записывается в поля для римских и русских букв по аналогии с паспортом СССР.
        //видимо, почтовые ИС несут очень тяжелый груз обратной совместимости :-D
        if (seria == null) {
            seria = "";
        }

        seria = seria.trim();

        if (Strings.isNullOrEmpty(seria)) {
            return "";
        }

        String[] seriaParts = seria.split(" ");
        if (seriaParts.length == 1) {
            seriaParts = seria.split("-");
        }

        if (seriaParts.length == 0) {
            return "";
        } else if (seriaParts.length == 1) {
            int middle = seria.length() / 2;
            if (partNo == 1) {
                return seria.substring(0, middle);
            } else {
                return seria.substring(middle);
            }
        } else {
            return seriaParts[partNo - 1].trim().toUpperCase();
        }
    }
}
