package ru.audithon.common.helpers;

import lombok.Getter;
import org.springframework.web.util.UriUtils;
import ru.audithon.common.exceptions.BusinessLogicException;

import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.Charset;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import static ru.audithon.common.helpers.ObjectUtils.isNull;

public class InMemoryHtmlTableLog {

    private static DateTimeFormatter logStrDateFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss");

    @Getter
    private boolean isActive;
    private String appHostUri;
    private StringBuilder log = new StringBuilder();
    @Getter
    private boolean isFinished;

    public InMemoryHtmlTableLog(String appHostUri) {
        this(appHostUri, true);
    }

    public InMemoryHtmlTableLog(String appHostUri, boolean isActive) {
        this.appHostUri = appHostUri;
        this.isActive = isActive;
        this.isFinished = false;

        log.append("<!DOCTYPE html>\n" +
                "<html>" +
                "<head>\n" +
                "<meta charset=\"utf-8\">\n" +
                "<title>Журнал</title>\n" +
                "<style>\n" +
                ".wrapper {" +
                "  max-width: 100%;\n" +
                "  word-break: break-all;\n" +
                "}\n" +
                "table, th, td {\n" +
                "  border: 1px solid black;\n" +
                "  padding: 5px;\n" +
                "}\n" +
                "table {\n" +
                "  border-collapse: collapse;\n" +
                "  width: 100%;" +
                "}\n" +
                "th {\n" +
                "  font-weight: bold\n" +
                "  white-space: nowrap;\n" +
                "}\n" +
                "td:first-child {\n" +
                "  white-space: nowrap;\n" +
                "}\n" +
                "td:nth-child(2) {\n" +
                "  width: 60px;\n" +
                "  white-space: nowrap;\n" +
                "}\n" +
                "</style>\n" +
                "</head>\n" +
                "<body>\n" +
                "<div class=\"wrapper\">\n" +
                "<table>\n" +
                "<tr>\n" +
                "<th>Время</th>\n" +
                "<th>Заголовок</th>\n" +
                "<th>Детали</th>\n" +
                "</tr>");
    }

    public void stopRecording() {
        if (!isActive) return;
        if (isFinished) return;

        isFinished = true;
        log.append("\n</table>");
        log.append("\n</div>");
        log.append("\n</body>");
        log.append("\n</html>");
    }

    public byte[] getUtf8LogBytes() {
        return getLogString().getBytes(Charset.forName("UTF-8"));
    }

    private String getLogString() {
        if (!isActive) return "";

        if (!isFinished) stopRecording();

        return log.toString();
    }

    public String makeInstitutionLink(String institutionCaption, Integer institutionId) throws MalformedURLException {
        return makeAppLink(
                String.format("/#/institution-edit/%s", institutionId),
                String.format("%s (%s)", isNull(institutionCaption, "-"), institutionId));
    }

    public String makePersonLink(String personName, Integer personId) throws MalformedURLException {
        return makeAppLink(
                String.format("/#/person-edit/%s", personId),
                String.format("%s (%s)", isNull(personName, "-"), personId));
    }

    public String makeRequestLink(String personName, Integer personId, Integer requestId) throws MalformedURLException {
        return makeAppLink(
                String.format("/#/person-edit/%s/requests/%s", personId, requestId),
                String.format("%s (%s/%s)", isNull(personName, "-"), personId, requestId));
    }

    public String makePayDocLink(String caption, Integer paymentDocumentId) throws MalformedURLException {
        return makeAppLink(
                String.format("/#/paydoc-edit/%s", paymentDocumentId),
                String.format("%s (%s)", isNull(caption, "-"), paymentDocumentId));
    }

    public String makeAppLink(String hostRelativeUri, String text) throws MalformedURLException {
        return String.format("<a href=\'%s\'>%s</a>", new URL(new URL(appHostUri), hostRelativeUri), text);
    }

    public void addLogRecord(String caption, String details) {
        if (!isActive) return;
        if (isFinished) return;

        log.append(String.format("\r\n<tr><td>%s</td><td>%s</td><td>%s</td></tr>",
                LocalDateTime.now().format(logStrDateFormatter),
                caption,
                details));
    }

    public void addLogRecord(String caption, String detailsFormat, Object... parameters) {
        addLogRecord(caption, String.format(detailsFormat, parameters));
    }
}
