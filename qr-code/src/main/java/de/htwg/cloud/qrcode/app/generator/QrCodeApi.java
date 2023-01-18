package de.htwg.cloud.qrcode.app.generator;

import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.CompletableFuture;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@Slf4j
@RestController
@RequestMapping(QrCodeApi.QR_CODE_GENERATOR_REST_PATH)
public class QrCodeApi {

    public static final String QR_CODE_GENERATOR_REST_PATH = "/qr-code";

    private final QrCodeService service;

    public QrCodeApi(QrCodeService service) {
        this.service = service;
    }

    @PostMapping(consumes = APPLICATION_JSON_VALUE)
    public ResponseEntity<InputStreamResource> generate(@RequestBody QrCodeApiDto dto) throws IOException, URISyntaxException {
        String textToEncode = dto.text;
        if (textToEncode == null || textToEncode.isBlank()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
        }

        ByteArrayOutputStream os = service.generate(textToEncode);
        int outputSize = os.size();

        byte[] qrCodeBytes = os.toByteArray();

        // Send Data to History Microservice
        CompletableFuture<HttpResponse<String>> response = service.sendToHistoryServiceAsync(qrCodeBytes);
        response.whenComplete((stringHttpResponse, throwable) -> log.info("Response received: {}", stringHttpResponse));


        String name = "qr-code.png";
        String fileName = "filename=\"" + name + "\"";
        String fileNameAsterisk = "filename*=UTF-8''" + URLEncoder.encode(name, StandardCharsets.UTF_8);

        return ResponseEntity
                .ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; " + fileName + "; " + fileNameAsterisk)
                .header(HttpHeaders.CONTENT_LENGTH, String.valueOf(outputSize))
                .contentType(MediaType.IMAGE_PNG)
                .body(new InputStreamResource(new ByteArrayInputStream(qrCodeBytes)));
    }


    private record QrCodeApiDto(String text) {
    }
}
