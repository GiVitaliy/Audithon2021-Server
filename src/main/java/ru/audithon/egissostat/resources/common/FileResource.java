package ru.audithon.egissostat.resources.common;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ru.audithon.egissostat.filestorage.FileStorage;
import ru.audithon.egissostat.filestorage.PreloadedFileData;
import ru.audithon.egissostat.resources.ApiResultDto;

import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.ArrayList;

@RestController
@RequestMapping(value = "/files",
        produces = MediaType.APPLICATION_JSON_VALUE)
public class FileResource {
    private static final Logger logger = LoggerFactory.getLogger(FileResource.class);

    private final FileStorage fileStorage;

    @Autowired
    public FileResource(FileStorage fileStorage) {
        this.fileStorage = fileStorage;
    }

    // Загрузка файлов происходит двухстадийно. С помощью этого метода, который требует аутентифицированного пользователя,
    //происходит подготовка файла к загрузке и формируется одноразовая ссылка на скачивание
    @GetMapping(path="/preload")
    public ResponseEntity<ApiResultDto> preloadFile(@RequestParam String uri) {
        return new ResponseEntity<>(
            new ApiResultDto(new ArrayList<>(), fileStorage.preloadFile(uri)),
            HttpStatus.OK
        );
    }

    // Загрузка файлов происходит двухстадийно. С помощью этого метода, не требующего аутентификацию пользователя,
    //происходит загрузка файла по ранее выданной ссылке
    @GetMapping(path = "/get")
    public ResponseEntity<Resource> getFile(@RequestParam String preloadId) {

        try {

            PreloadedFileData res = fileStorage.readPreloadedFile(preloadId);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(new MediaType("application", "octet-stream"));
            headers.setContentDispositionFormData("attachment",
                URLDecoder.decode(URLEncoder.encode(res.getPrettyFileName(), "UTF-8"), "ISO8859_1"));

            InputStreamResource resource = new InputStreamResource(res.getContent());

            return new ResponseEntity<>(resource, headers, HttpStatus.OK);
        } catch(Exception ex) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }
}
